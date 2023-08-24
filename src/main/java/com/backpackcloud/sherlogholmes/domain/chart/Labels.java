package com.backpackcloud.sherlogholmes.domain.chart;

import com.backpackcloud.sherlogholmes.domain.Attribute;
import com.backpackcloud.sherlogholmes.domain.AttributeType;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.domain.TimeUnit;
import com.backpackcloud.sherlogholmes.domain.chart.labels.CategoryLabel;
import com.backpackcloud.sherlogholmes.domain.chart.labels.TemporalLabel;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Labels {

  private Labels() {

  }

  public static List<Label<Long>> temporal(DataEntry from, DataEntry to, TimeUnit unit, String attribute) {
    List<Label<Long>> labels = new ArrayList<>();

    Temporal start = unit.truncate(from.attribute(attribute, Temporal.class)
      .flatMap(Attribute::value)
      .orElseThrow());
    Temporal end = start.plus(1, unit.chronoUnit());

    Label<Long> label;
    do {
      label = new TemporalLabel(unit, start, end, attribute);
      labels.add(label);
      start = end;
      end = start.plus(1, unit.chronoUnit());
    } while (!label.includes(to));

    return labels;
  }

  public static List<Label<String>> category(DataRegistry registry, String category) {
    AttributeType<Object> type = registry.typeOf(category).orElse(AttributeType.TEXT);
    return registry.index(category).keySet().stream()
      .map(value -> new CategoryLabel(category, type.format(value)))
      .collect(Collectors.toList());
  }

}
