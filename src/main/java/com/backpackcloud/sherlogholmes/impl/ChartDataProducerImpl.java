/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Marcelo Guimarães
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.backpackcloud.sherlogholmes.impl;

import com.backpackcloud.cli.preferences.UserPreferences;
import com.backpackcloud.sherlogholmes.Preferences;
import com.backpackcloud.sherlogholmes.domain.Attribute;
import com.backpackcloud.sherlogholmes.domain.AttributeType;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.domain.TimeUnit;
import com.backpackcloud.sherlogholmes.domain.chart.Bucket;
import com.backpackcloud.sherlogholmes.domain.chart.Chart;
import com.backpackcloud.sherlogholmes.domain.chart.ChartDataProducer;
import com.backpackcloud.sherlogholmes.domain.chart.Series;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ApplicationScoped
public class ChartDataProducerImpl implements ChartDataProducer {

  private final DataRegistry registry;
  private final UserPreferences preferences;

  public ChartDataProducerImpl(DataRegistry registry, UserPreferences preferences) {
    this.registry = registry;
    this.preferences = preferences;
  }

  @Override
  public Chart produceData(TimeUnit unit, String attribute) {
    if (registry.isEmpty()) {
      return new ChartImpl(
        Collections.emptyList(),
        Collections.emptyList(),
        new SeriesImpl("total", Collections.emptyList()),
        new SeriesImpl("average", Collections.emptyList())
      );
    }

    String temporalAttribute = preferences.text(Preferences.TIMESTAMP_ATTRIBUTE).get();

    Map<String, Series> seriesMap = new HashMap<>();
    List<Column> columns = new ArrayList<>();

    Temporal start = unit.truncate(registry.entries()
      .first()
      .attribute(temporalAttribute, Temporal.class)
      .flatMap(Attribute::value)
      .orElseThrow());
    Temporal end = start.plus(1, unit.chronoUnit());
    DataEntry last = registry.entries().last();
    Column col;

    do {
      col = new Column(unit.format(start), temporalAttribute, start, end);
      columns.add(col);
      start = end;
      end = start.plus(1, unit.chronoUnit());
    } while (!col.includes(last));

    Supplier<List<Bucket>> bucketsSupplier = () -> {
      List<Bucket> result = new ArrayList<>();
      columns.forEach(column -> result.add(new BucketImpl(column.id, column.start, column.end, 0)));
      return result;
    };

    Function<String, Series> seriesFunction = name -> new SeriesImpl(name, bucketsSupplier.get());

    AttributeType<Object> type = registry.typeOf(attribute).orElseThrow();

    registry.index(attribute).keySet()
      .stream().map(type::format)
      .forEach(value -> seriesMap.put(value, seriesFunction.apply(value)));

    AtomicInteger columnIndex = new AtomicInteger(0);
    Column currentColumn = columns.get(columnIndex.get());

    for (DataEntry entry : registry.entries()) {
      while (!currentColumn.includes(entry)) {
        currentColumn = columns.get(columnIndex.incrementAndGet());
      }
      entry.attribute(attribute)
        .flatMap(Attribute::value)
        .map(type::format)
        .ifPresent(value ->
          ((BucketImpl) seriesMap.get(value).buckets().get(columnIndex.get())).incrementCount());
    }

    Series totals = new SeriesImpl("total", new ArrayList<>());
    Series averages = new SeriesImpl("average", new ArrayList<>());

    for (int i = 0; i < columns.size(); i++) {
      Column column = columns.get(i);
      int count = 0;
      for (Series series : seriesMap.values()) {
        Bucket bucket = series.buckets().get(i);
        count += bucket.value();
      }
      totals.buckets().add(new BucketImpl(column.id, column.start, column.end, count));
      averages.buckets().add(new BucketImpl(column.id, column.start, column.end, Math.round((float) count / seriesMap.size())));
    }

    return new ChartImpl(
      columns.stream().map(Column::id).collect(Collectors.toList()),
      seriesMap.values().stream()
        .sorted(Comparator.comparingInt(Series::total).reversed())
        .toList(),
      totals,
      averages
    );
  }

  private static class Column {

    private final String id;
    private final String temporalAttribute;
    private final Temporal start;
    private final Temporal end;
    private final long threshold;

    private Column(String id, String temporalAttribute, Temporal start, Temporal end) {
      this.id = id;
      this.temporalAttribute = temporalAttribute;
      this.start = start;
      this.end = end;
      this.threshold = Duration.between(start, end).toMillis();
    }

    public String id() {
      return id;
    }

    public boolean includes(DataEntry entry) {
      Temporal end = entry.attribute(temporalAttribute, Temporal.class)
        .flatMap(Attribute::value)
        .orElseThrow();
      return Duration.between(start, end).toMillis() < threshold;
    }

  }

  private record ChartImpl(List<String> bucketNames, List<Series> series, Series total,
                           Series average) implements Chart {

    @Override
    public List<Series> series(int maxSize) {
      if (series.size() < maxSize) {
        return series;
      }
      List<Series> result = new ArrayList<>(series.subList(0, maxSize));
      Iterator<Series> iterator = series.listIterator(maxSize);
      Series others = iterator.next();
      String seriesName = "others (" + (series.size() - maxSize) + ")";
      while (iterator.hasNext()) {
        others = others.add(seriesName, iterator.next());
      }
      result.add(others);
      return result;
    }
  }

  private record SeriesImpl(String name, List<Bucket> buckets) implements Series {

    @Override
    public Series add(String newName, Series other) {
      List<Bucket> bucketSum = new ArrayList<>(buckets.size());
      Iterator<Bucket> iteratorA = buckets.iterator();
      Iterator<Bucket> iteratorB = other.buckets().iterator();

      // assuming both series have the same buckets
      while (iteratorA.hasNext()) {
        Bucket a = iteratorA.next();
        Bucket b = iteratorB.next();
        bucketSum.add(new BucketImpl(a.id(), a.start(), a.end(), a.value() + b.value()));
      }

      return new SeriesImpl(newName, bucketSum);
    }

    @Override
    public int total() {
      return buckets.stream().map(Bucket::value).reduce(0, Integer::sum);
    }

  }

  private static class BucketImpl implements Bucket {

    private final String id;
    private final long startMillis;
    private final Temporal start;
    private final Temporal end;
    private int count;

    private BucketImpl(String id, Temporal start, Temporal end, int count) {
      this.id = id;
      this.start = start;
      this.end = end;
      this.count = count;
      if (start instanceof ZonedDateTime) {
        this.startMillis = ((ZonedDateTime) start).toInstant().toEpochMilli();
      } else if (start instanceof LocalDateTime) {
        this.startMillis = ((LocalDateTime) start).toInstant(ZoneOffset.UTC).toEpochMilli();
      } else {
        this.startMillis = 0L;
      }
    }

    @Override
    public String id() {
      return id;
    }

    public void incrementCount() {
      this.count++;
    }

    @Override
    public long startMillis() {
      return startMillis;
    }

    @Override
    public Temporal start() {
      return start;
    }

    @Override
    public Temporal end() {
      return end;
    }

    @Override
    public int value() {
      return count;
    }
  }

}
