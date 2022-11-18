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
import com.backpackcloud.sherlogholmes.domain.chart.Series;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RegisterForReflection
public record ChartImpl(List<String> bucketNames, List<Series> series, Series total,
                        Series average) implements Chart {

  @JsonProperty
  @Override
  public List<Series> series() {
    return series;
  }

  @Override
  public List<Series> series(int maxSize) {
    if (series.size() <= maxSize) {
      return series;
    }
    List<Series> result = new ArrayList<>(series.subList(0, maxSize));
    Iterator<Series> iterator = series.listIterator(maxSize);
    Series others = iterator.next();
    String seriesName = "others (" + (series.size() - maxSize) + ")";
    while (iterator.hasNext()) {
      others = others.add(seriesName, iterator.next());
    }
    result.add(others);
    return result;
  }
}