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
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ApplicationScoped
@RegisterForReflection
public class ChartDataProducerImpl implements ChartDataProducer {

  private final DataRegistry registry;
  private final UserPreferences preferences;

  public ChartDataProducerImpl(DataRegistry registry, UserPreferences preferences) {
    this.registry = registry;
    this.preferences = preferences;
  }

  @Override
  public Chart produceData(TimeUnit bucketUnit, String seriesAttribute) {
    return produceData(bucketUnit, seriesAttribute, null);
  }

  @Override
  public Chart produceData(TimeUnit unit, String attribute, String countAttribute) {
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
      columns.forEach(column -> result.add(new BucketImpl(column.id, 0)));
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
          ((BucketImpl) seriesMap.get(value).buckets().get(columnIndex.get()))
            .incrementCount(entry.attribute(countAttribute, Integer.class)
              .flatMap(Attribute::value)
              .orElse(1)));
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
      totals.buckets().add(new BucketImpl(column.id, count));
      averages.buckets().add(new BucketImpl(column.id, Math.round((float) count / seriesMap.size())));
    }

    return new ChartImpl(
      columns.stream().map(Column::id).collect(Collectors.toList()),
      seriesMap.values().stream()
        .sorted(Comparator.comparingLong(Series::total).reversed())
        .toList(),
      totals,
      averages
    );
  }

  private static class Column {

    private final String id;
    private final String temporalAttribute;
    private final Temporal start;
    private final long threshold;

    private Column(String id, String temporalAttribute, Temporal start, Temporal end) {
      this.id = id;
      this.temporalAttribute = temporalAttribute;
      this.start = start;
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

}
