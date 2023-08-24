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

import com.backpackcloud.sherlogholmes.domain.chart.Chart;
import com.backpackcloud.sherlogholmes.domain.chart.DataPoint;
import com.backpackcloud.sherlogholmes.domain.chart.Series;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RegisterForReflection
public class WebChart<X, Y> {

  private final String name;
  private final List<WebChartSeries> series;

  public WebChart(String name, Chart<X, Y> chart, int maxSeriesSize) {
    this.name = name;
    this.series = chart.series(maxSeriesSize)
      .stream()
      .map(WebChartSeries::new)
      .collect(Collectors.toCollection(ArrayList::new));
  }
  @JsonProperty
  public String name() {
    return name;
  }

  @JsonProperty
  public List<WebChartSeries> series() {
    return series;
  }

  @RegisterForReflection
  public static class WebChartSeries<X, Y> {

    private final String name;
    private final Object[][] data;

    public WebChartSeries(Series<X, Y> series) {
      this.name = series.name();
      this.data = new Object[series.data().size()][2];
      int i = 0;
      for (DataPoint<?, ?> dataPoint : series.data()) {
        this.data[i++] = new Object[]{dataPoint.label().value(), dataPoint.value()};
      }
    }

    @JsonProperty
    public String name() {
      return name;
    }

    @JsonProperty
    public Object[][] data() {
      return data;
    }

  }
}