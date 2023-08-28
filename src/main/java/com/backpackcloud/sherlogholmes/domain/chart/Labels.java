package com.backpackcloud.sherlogholmes.domain.chart;

import com.backpackcloud.sherlogholmes.domain.Attribute;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.TimeUnit;
import com.backpackcloud.sherlogholmes.domain.chart.labels.CategoryLabel;
import com.backpackcloud.sherlogholmes.domain.chart.labels.TemporalLabel;

import java.time.temporal.Temporal;
import java.util.function.Function;

public final class Labels {

  private Labels() {

  }

  public static Function<DataEntry, Label<Long>> temporal(TimeUnit unit, String attribute) {
    return entry -> {
      Temporal start = unit.truncate(entry.attribute(attribute, Temporal.class)
        .flatMap(Attribute::value)
        .orElseThrow());

      Temporal end = start.plus(1, unit.chronoUnit());

      return new TemporalLabel(unit, start, end, attribute);
    };
  }

  public static Function<DataEntry, Label<String>> category(String category) {
    return entry -> entry.attribute(category)
      .flatMap(Attribute::formattedValue)
      .map(value -> new CategoryLabel(category, value))
      .orElseThrow();
  }

}
