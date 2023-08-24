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

import com.backpackcloud.sherlogholmes.domain.chart_old.Bucket;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;

@RegisterForReflection
public class BucketImpl implements Bucket {

  private final String id;
  private int count;

  private final long startMillis;

  private final Temporal start;

  public BucketImpl(String id, int count, Temporal start) {
    this.id = id;
    this.count = count;
    this.start = start;
    if (start instanceof ZonedDateTime zonedDateTime) {
      this.startMillis = zonedDateTime.toInstant().toEpochMilli();
    } else if (start instanceof LocalDateTime localDateTime) {
      this.startMillis = localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    } else if (start instanceof LocalDate localDate){
      this.startMillis = localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli();
    } else if (start instanceof OffsetDateTime offsetDateTime) {
      this.startMillis = offsetDateTime.toInstant().toEpochMilli();
    } else if (start instanceof LocalTime localTime) {
      this.startMillis = localTime.getLong(ChronoField.MILLI_OF_SECOND);
    } else {
      this.startMillis = 0L;
    }
  }

  @JsonProperty("x")
  @Override
  public String id() {
    return id;
  }

  public void incrementCount(int amount) {
    this.count += amount;
  }

  @JsonProperty("y")
  @Override
  public int value() {
    return count;
  }

  @Override
  public Temporal start() {
    return start;
  }

  @Override
  public long startMillis() {
    return startMillis;
  }

}