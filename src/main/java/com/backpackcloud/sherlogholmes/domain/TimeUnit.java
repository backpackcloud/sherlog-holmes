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

package com.backpackcloud.sherlogholmes.domain;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

public enum TimeUnit {

  MILLIS(ChronoField.MILLI_OF_SECOND, ChronoUnit.MILLIS, "yyyy-MM-dd'T'HH:mm:ss.SSS", "HH:mm:ss.SSS"),
  SECONDS(ChronoField.SECOND_OF_MINUTE, ChronoUnit.SECONDS, "yyyy-MM-dd'T'HH:mm:ss", "HH:mm:ss.SSS"),
  MINUTES(ChronoField.MINUTE_OF_HOUR, ChronoUnit.MINUTES, "yyyy-MM-dd'T'HH:mm", "HH:mm:ss.SSS"),
  HOURS(ChronoField.HOUR_OF_DAY, ChronoUnit.HOURS, "yyyy-MM-dd'T'HH:00", "HH:mm:ss.SSS"),
  WEEKS(ChronoField.ALIGNED_WEEK_OF_YEAR, ChronoUnit.WEEKS, "yyyy-MM 'W'W", ""),
  DAYS(ChronoField.DAY_OF_MONTH, ChronoUnit.DAYS, "yyyy-MM-dd", ""),
  MONTHS(ChronoField.MONTH_OF_YEAR, ChronoUnit.MONTHS, "yyyy-MM", ""),
  YEARS(ChronoField.YEAR, ChronoUnit.YEARS, "yyyy", "");

  private final ChronoField chronoField;
  private final ChronoUnit chronoUnit;
  private final DateTimeFormatter dateFormatter;

  private final DateTimeFormatter timeFormatter;

  TimeUnit(ChronoField chronoField, ChronoUnit chronoUnit, String dateFormat, String timeFormat) {
    this.chronoField = chronoField;
    this.chronoUnit = chronoUnit;
    this.dateFormatter = DateTimeFormatter.ofPattern(dateFormat);
    this.timeFormatter = DateTimeFormatter.ofPattern(timeFormat);
  }

  public long millis() {
    return chronoUnit.getDuration().toMillis();
  }

  public long toMillis(long value) {
    return millis() * value;
  }

  public double fromMillis(long value) {
    return (double) value / millis();
  }

  public String format(Temporal reference) {
    return reference.isSupported(ChronoField.DAY_OF_MONTH) ?
      dateFormatter.format(reference) : timeFormatter.format(reference);
  }

  public ChronoField chronoField() {
    return chronoField;
  }

  public ChronoUnit chronoUnit() {
    return chronoUnit;
  }

  public Temporal truncate(Temporal reference) {
    Temporal result = reference;
    for (TimeUnit unit : TimeUnit.values()) {
      if (reference.isSupported(unit.chronoField) && unit.ordinal() < this.ordinal()) {
        result = unit.chronoField.adjustInto(result, unit.chronoField.range().getMinimum());
      }
    }
    return result;
  }

}
