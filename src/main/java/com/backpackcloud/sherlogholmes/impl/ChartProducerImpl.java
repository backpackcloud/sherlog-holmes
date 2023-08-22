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
import com.backpackcloud.sherlogholmes.domain.DataFilter;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.domain.TimeUnit;
import com.backpackcloud.sherlogholmes.domain.chart.Bucket;
import com.backpackcloud.sherlogholmes.domain.chart.Chart;
import com.backpackcloud.sherlogholmes.domain.chart.ChartProducer;
import com.backpackcloud.sherlogholmes.domain.chart.Series;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ApplicationScoped
@RegisterForReflection
public class ChartProducerImpl implements ChartProducer {

  private final DataRegistry registry;
  private final UserPreferences preferences;

  public ChartProducerImpl(DataRegistry registry, UserPreferences preferences) {
    this.registry = registry;
    this.preferences = preferences;
  }

  @Override
  public Chart produce(TimeUnit unit, DataFilter filter, String attribute, String countAttribute) {
    NavigableSet<DataEntry> entries = registry.entries(filter);

    if (entries.isEmpty()) {
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

    Temporal start = unit.truncate(entries
      .first()
      .attribute(temporalAttribute, Temporal.class)
      .flatMap(Attribute::value)
      .orElseThrow());
    Temporal end = start.plus(1, unit.chronoUnit());
    DataEntry last = entries.last();
    Column col;

    do {
      col = new Column(unit.format(start), temporalAttribute, start, end, unit.chronoUnit());
      columns.add(col);
      start = end;
      end = start.plus(1, unit.chronoUnit());
    } while (!col.includes(last));

    Supplier<List<Bucket>> bucketsSupplier = () -> {
      List<Bucket> result = new ArrayList<>();
      columns.forEach(column -> result.add(new BucketImpl(column.id, 0, column.start)));
      return result;
    };

    Function<String, Series> seriesFunction = name -> new SeriesImpl(name, bucketsSupplier.get());

    AttributeType<Object> type = registry.typeOf(attribute).orElse(AttributeType.TEXT);

    registry.index(attribute).keySet()
      .stream().map(type::format)
      .forEach(value -> seriesMap.put(value, seriesFunction.apply(value)));

    AtomicInteger columnIndex = new AtomicInteger(0);
    Column currentColumn = columns.get(columnIndex.get());

    for (DataEntry entry : entries) {
      Consumer<? super String> updateChartData = value -> {
        if (seriesMap.containsKey(value)) {
          Series series = seriesMap.get(value);
          List<Bucket> buckets = series.buckets();
          Bucket bucket = buckets.get(columnIndex.get());
          bucket.incrementCount(entry.attribute(countAttribute, Integer.class)
            .flatMap(Attribute::value)
            .orElse(1));
        }
      };

      while (!currentColumn.includes(entry)) {
        currentColumn = columns.get(columnIndex.incrementAndGet());
      }
      if (attribute.contains(":")) {
        String[] attributes = attribute.split(":");
        String[] values = new String[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
          String attr = attributes[i];
          if (!entry.hasAttribute(attr)) {
            continue;
          }
          Optional<String> value = entry.attribute(attr)
            .flatMap(Attribute::formattedValue);
          if (value.isEmpty()) {
            continue;
          }
          values[i] = value.get();
        }
        updateChartData.accept(String.join(":", values));
      } else {
        entry.attribute(attribute)
          .flatMap(Attribute::value)
          .map(type::format)
          .ifPresent(updateChartData);
      }
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
      totals.buckets().add(new BucketImpl(column.id, count, column.start));
      averages.buckets().add(new BucketImpl(column.id, Math.round((float) count / seriesMap.size()), column.start));
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
    private final ChronoUnit unit;
    private final long threshold;

    private Column(String id, String temporalAttribute, Temporal start, Temporal end, ChronoUnit unit) {
      this.id = id;
      this.temporalAttribute = temporalAttribute;
      this.start = start;
      this.unit = unit;
      this.threshold = start.until(end, unit);
    }

    public String id() {
      return id;
    }

    public boolean includes(DataEntry entry) {
      Temporal end = entry.attribute(temporalAttribute, Temporal.class)
        .flatMap(Attribute::value)
        .orElseThrow();
      return start.until(end, unit) < threshold;
    }
  }

}
