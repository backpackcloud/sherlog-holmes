package com.backpackcloud.sherlogholmes.domain.chart.labels;

import com.backpackcloud.sherlogholmes.domain.Attribute;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.TimeUnit;
import com.backpackcloud.sherlogholmes.domain.chart.Label;

import java.time.temporal.Temporal;

public class TemporalLabel implements Label {

  private final String name;
  private final TimeUnit unit;
  private final Temporal from;
  private final long threshold;
  private final String attribute;

  public TemporalLabel(TimeUnit unit, Temporal from, Temporal to, String attribute) {
    this.attribute = attribute;
    this.name = unit.format(from);
    this.unit = unit;
    this.from = from;
    this.threshold = from.until(to, unit.chronoUnit());
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean includes(DataEntry entry) {
    Temporal end = entry.attribute(attribute, Temporal.class)
      .flatMap(Attribute::value)
      .orElseThrow();
    return from.until(end, unit.chronoUnit()) < threshold;
  }
}
