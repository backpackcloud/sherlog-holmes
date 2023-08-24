package com.backpackcloud.sherlogholmes.domain.chart.labels;

import com.backpackcloud.sherlogholmes.domain.Attribute;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.TimeUnit;
import com.backpackcloud.sherlogholmes.domain.chart.Label;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Objects;

public class TemporalLabel implements Label<Long> {

  private final TimeUnit unit;
  private final Temporal from;
  private final long threshold;
  private final String attribute;
  private final long millis;

  public TemporalLabel(TimeUnit unit, Temporal from, Temporal to, String attribute) {
    this.attribute = attribute;
    this.unit = unit;
    this.from = from;
    this.threshold = from.until(to, unit.chronoUnit());
    if (from instanceof ZonedDateTime zonedDateTime) {
      this.millis = zonedDateTime.toInstant().toEpochMilli();
    } else if (from instanceof LocalDateTime localDateTime) {
      this.millis = localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    } else if (from instanceof LocalDate localDate){
      this.millis = localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
    } else if (from instanceof OffsetDateTime offsetDateTime) {
      this.millis = offsetDateTime.toInstant().toEpochMilli();
    } else if (from instanceof LocalTime localTime) {
      this.millis = localTime.getLong(ChronoField.MILLI_OF_SECOND);
    } else {
      this.millis = 0L;
    }
  }

  @Override
  public Long value() {
    return this.millis;
  }

  @Override
  public boolean includes(DataEntry entry) {
    Temporal end = entry.attribute(attribute, Temporal.class)
      .flatMap(Attribute::value)
      .orElseThrow();
    return from.until(end, unit.chronoUnit()) < threshold;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TemporalLabel that = (TemporalLabel) o;
    return threshold == that.threshold
      && Objects.equals(millis, that.millis)
      && unit == that.unit
      && Objects.equals(from, that.from)
      && Objects.equals(attribute, that.attribute);
  }

  @Override
  public int hashCode() {
    return Objects.hash(millis, unit, from, threshold, attribute);
  }

}
