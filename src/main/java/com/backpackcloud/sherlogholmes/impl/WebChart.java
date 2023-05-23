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
import com.backpackcloud.sherlogholmes.domain.chart.Bucket;
import com.backpackcloud.sherlogholmes.domain.chart.Chart;
import com.backpackcloud.sherlogholmes.domain.chart.Series;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RegisterForReflection
public class WebChart {

  private final String id;
  private final String config;
  private final List<WebChartSeries> series;

  public WebChart(String id, String config, Chart chart, int maxSeriesSize) {
    this.id = id;
    this.config = config;
    this.series = chart.series(maxSeriesSize)
      .stream()
      .map(WebChartSeries::new)
      .collect(Collectors.toCollection(ArrayList::new));
    this.series.add(new WebChartSeries(chart.total()));
    this.series.add(new WebChartSeries(chart.average()));
  }

  @JsonProperty
  public String id() {
    return id;
  }

  @JsonProperty
  public String config() {
    return config;
  }

  @JsonProperty
  public List<WebChartSeries> series() {
    return series;
  }

  @RegisterForReflection
  public static class WebChartSeries {

    private final String name;
    private final long[][] data;
    public WebChartSeries(Series series) {
      this.name = series.name();
      this.data = new long[series.buckets().size()][2];
      int i = 0;
      for (Bucket bucket : series.buckets()) {
        this.data[i++] = new long[]{bucket.startMillis(), bucket.value()};
      }
    }

    @JsonProperty
    public String name() {
      return name;
    }

    @JsonProperty
    public long[][] data() {
      return data;
    }

  }
}