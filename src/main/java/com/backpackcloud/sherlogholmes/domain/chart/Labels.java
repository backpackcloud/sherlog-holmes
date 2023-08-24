package com.backpackcloud.sherlogholmes.domain.chart;

import com.backpackcloud.sherlogholmes.domain.Attribute;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.TimeUnit;
import com.backpackcloud.sherlogholmes.domain.chart.labels.TemporalLabel;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;

public final class Labels {

  private Labels() {

  }

  public static List<Label> temporal(DataEntry from, DataEntry to, TimeUnit unit, String attribute) {
    List<Label> labels = new ArrayList<>();

    Temporal start = unit.truncate(from.attribute(attribute, Temporal.class)
      .flatMap(Attribute::value)
      .orElseThrow());
    Temporal end = start.plus(1, unit.chronoUnit());

    Label label;
    do {
      label = new TemporalLabel(unit, start, end, attribute);
      labels.add(label);
      start = end;
      end = start.plus(1, unit.chronoUnit());
    } while (!label.includes(to));

    return labels;
  }

}
