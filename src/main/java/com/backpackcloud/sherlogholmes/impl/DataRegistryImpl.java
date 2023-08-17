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

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.sherlogholmes.domain.Attribute;
import com.backpackcloud.sherlogholmes.domain.AttributeType;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.DataFilter;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

@ApplicationScoped
public class DataRegistryImpl implements DataRegistry {

  private final DataRegistry total = new _DataRegistry();
  private DataRegistry filtered;

  private DataRegistry registry() {
    return filtered().orElse(total);
  }

  private Optional<DataRegistry> filtered() {
    return Optional.ofNullable(filtered);
  }

  @Override
  public String name() {
    return total.name();
  }

  @Override
  public void add(DataEntry entry) {
    total.add(entry);
    filtered().ifPresent(registry -> registry.add(entry));
  }

  @Override
  public void apply(DataFilter filter) {
    filtered = new _DataRegistry();
    total.indexedAttributes().forEach(filtered::addIndex);
    total.stream()
      .filter(filter)
      .forEach(filtered::add);
  }

  @Override
  public void removeFilter() {
    filtered = null;
  }

  @Override
  public DataRegistry addIndex(String attributeName) {
    total.addIndex(attributeName);
    filtered().ifPresent(registry -> registry.addIndex(attributeName));
    return this;
  }

  @Override
  public DataRegistry removeIndex(String attributeName) {
    total.removeIndex(attributeName);
    filtered().ifPresent(registry -> registry.removeIndex(attributeName));
    return this;
  }

  @Override
  public Set<String> indexedAttributes() {
    return registry().indexedAttributes();
  }

  @Override
  public Optional<AttributeType> typeOf(String name) {
    return registry().typeOf(name);
  }

  @Override
  public Set<String> attributeNames() {
    return registry().attributeNames();
  }

  @Override
  public NavigableSet<DataEntry> entries() {
    return registry().entries();
  }

  @Override
  public NavigableSet<DataEntry> entries(DataFilter filter) {
    return registry().entries(filter);
  }

  @Override
  public Stream<DataEntry> stream() {
    return registry().stream();
  }

  @Override
  public <E> Map<E, NavigableSet<DataEntry>> index(String attributeName) {
    return registry().index(attributeName);
  }

  @Override
  public <E> Set<E> valuesFor(String attributeName) {
    return registry().valuesFor(attributeName);
  }

  @Override
  public Duration durationOf(String attribute) {
    return registry().durationOf(attribute);
  }

  @Override
  public boolean isEmpty() {
    return registry().isEmpty();
  }

  @Override
  public int size() {
    return registry().size();
  }

  @Override
  public void clear() {
    total.clear();
    filtered = null;
  }

  private static class _DataRegistry implements DataRegistry {
    private final NavigableSet<DataEntry> entries;
    private final Map<String, Map<Object, NavigableSet<DataEntry>>> index;
    private final Map<String, AttributeType> attributeTypes;

    public _DataRegistry() {
      this.entries = new TreeSet<>();
      this.index = new HashMap<>();
      this.attributeTypes = new HashMap<>();
    }

    @Override
    public void add(DataEntry entry) {
      this.entries.add(entry);

      entry.attributes()
        .forEach(attribute ->
          attributeTypes.put(attribute.name(), attribute.spec().type()));

      index.forEach((attributeName, valuesMap) ->
        entry.attribute(attributeName)
          .ifPresent(attr -> attr.values().forEach(value -> {
            if (!valuesMap.containsKey(value)) {
              valuesMap.put(value, new TreeSet<>());
            }
            valuesMap.get(value).add(entry);
          })));
    }

    @Override
    public void apply(DataFilter filter) {
      throw new UnbelievableException();
    }

    @Override
    public void removeFilter() {
      throw new UnbelievableException();
    }

    @Override
    public DataRegistry addIndex(String attributeName) {
      this.index.putIfAbsent(attributeName, new HashMap<>());
      return this;
    }

    @Override
    public DataRegistry removeIndex(String attributeName) {
      this.index.remove(attributeName);
      return this;
    }

    @Override
    public Set<String> indexedAttributes() {
      return new HashSet<>(this.index.keySet());
    }

    @Override
    public Optional<AttributeType> typeOf(String name) {
      return Optional.ofNullable(attributeTypes.get(name));
    }

    @Override
    public Set<String> attributeNames() {
      return new HashSet<>(attributeTypes.keySet());
    }

    @Override
    public NavigableSet<DataEntry> entries() {
      return entries;
    }

    @Override
    public NavigableSet<DataEntry> entries(DataFilter filter) {
      if (filter == null) {
        return entries;
      }
      NavigableSet<DataEntry> filtered = new TreeSet<>();
      entries.stream().filter(filter).forEach(filtered::add);
      return filtered;
    }

    @Override
    public Stream<DataEntry> stream() {
      return entries.stream();
    }

    @Override
    public Map index(String attributeName) {
      return index.getOrDefault(attributeName, Collections.emptyMap());
    }

    @Override
    public Set valuesFor(String attributeName) {
      return index.containsKey(attributeName) ?
        index.get(attributeName).keySet() :
        Collections.emptySet();
    }

    @Override
    public boolean isEmpty() {
      return entries.isEmpty();
    }

    @Override
    public int size() {
      return entries.size();
    }

    @Override
    public Duration durationOf(String attributeName) {
      if (entries.size() >= 2) {
        DataEntry first = entries.first();
        DataEntry last = entries.last();

        return first.attribute(attributeName, Temporal.class)
          .flatMap(Attribute::value)
          .map(start -> last.attribute(attributeName, Temporal.class)
            .flatMap(Attribute::value)
            .map(end -> Duration.between(start, end))
            .orElse(Duration.ZERO))
          .orElse(Duration.ZERO);
      }
      return Duration.ZERO;
    }

    @Override
    public void clear() {
      this.entries.clear();
      this.index.clear();
      this.attributeTypes.clear();
    }
  }

}
