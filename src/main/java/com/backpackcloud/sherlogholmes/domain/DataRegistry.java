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

import com.backpackcloud.cli.Registry;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface DataRegistry extends Registry {

  @Override
  default String name() {
    return "data";
  }

  void onDataChanged(Runnable action);

  void onDataAdded(Consumer<DataEntry> consumer);

  void add(DataEntry entry);

  void add(Collection<DataEntry> entries);

  void apply(DataFilter filter);

  void removeFilter();

  DataRegistry addIndex(String attributeName);

  DataRegistry removeIndex(String attributeName);

  Set<String> indexedAttributes();

  Optional<AttributeType> typeOf(String name);

  Set<String> attributeNames();

  NavigableSet<DataEntry> entries();

  Stream<DataEntry> stream();

  <E> Map<E, NavigableSet<DataEntry>> index(String attributeName);

  <E> Set<E> valuesFor(String attributeName);

  Duration durationOf(String attribute);

}
