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
import com.backpackcloud.sherlogholmes.domain.chart.Series;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RegisterForReflection
public record SeriesImpl(String name, List<Bucket> buckets) implements Series {

  @JsonProperty
  @Override
  public String name() {
    return name;
  }

  @JsonProperty("data")
  @Override
  public List<Bucket> buckets() {
    return buckets;
  }

  @Override
  public Series add(String newName, Series other) {
    List<Bucket> bucketSum = new ArrayList<>(buckets.size());
    Iterator<Bucket> iteratorA = buckets.iterator();
    Iterator<Bucket> iteratorB = other.buckets().iterator();

    // assuming both series have the same buckets
    while (iteratorA.hasNext()) {
      Bucket a = iteratorA.next();
      Bucket b = iteratorB.next();
      bucketSum.add(new BucketImpl(a.id(), a.value() + b.value(), a.start()));
    }

    return new SeriesImpl(newName, bucketSum);
  }

  @Override
  public long total() {
    return buckets.stream().map(Bucket::value).reduce(0, Integer::sum);
  }

}