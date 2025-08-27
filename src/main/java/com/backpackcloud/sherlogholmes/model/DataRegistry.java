/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Marcelo "Ataxexe" Guimarães
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

package com.backpackcloud.sherlogholmes.model;

import com.backpackcloud.cli.Registry;
import com.backpackcloud.preferences.UserPreferences;
import com.backpackcloud.sherlogholmes.Preferences;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DataRegistry implements Registry {

  private final InternalStorage total = new InternalStorage();
  private final UserPreferences preferences;
  private InternalStorage filtered;

  public DataRegistry(UserPreferences preferences) {
    this.preferences = preferences;
  }

  private InternalStorage registry() {
    return filtered().orElse(total);
  }

  private Optional<InternalStorage> filtered() {
    return Optional.ofNullable(filtered);
  }

  @Override
  public String name() {
    return "data";
  }

  public void add(DataEntry entry) {
    total.add(entry);
    filtered().ifPresent(registry -> registry.add(entry));
  }

  public void apply(DataFilter filter) {
    filtered = new InternalStorage();
    total.indexedAttributes().forEach(filtered::addIndex);
    total.stream()
      .filter(filter)
      .forEach(filtered::add);
  }

  public void removeFilter() {
    filtered = null;
  }

  public void addIndex(String attributeName) {
    total.addIndex(attributeName);
    filtered().ifPresent(registry -> registry.addIndex(attributeName));
  }

  public void removeIndex(String attributeName) {
    total.removeIndex(attributeName);
    filtered().ifPresent(registry -> registry.removeIndex(attributeName));
  }

  public Set<String> indexedAttributes() {
    return registry().indexedAttributes();
  }

  public Optional<AttributeType> typeOf(String name) {
    return registry().typeOf(name);
  }

  public Set<String> attributeNames() {
    return registry().attributeNames();
  }

  public NavigableSet<DataEntry> entries() {
    return registry().entries();
  }

  public Stream<DataEntry> head(int count) {
    return stream().limit(count);
  }

  public Stream<DataEntry> head(int amount, ChronoUnit unit) {
    String timestampAttribute = preferences.get(Preferences.TIMESTAMP_ATTRIBUTE).value();

    NavigableSet<DataEntry> entries = entries();
    Temporal reference = entries.getFirst()
      .attribute(timestampAttribute, Temporal.class)
      .flatMap(Attribute::value)
      .map(temporal -> temporal.plus(amount, unit))
      .orElseThrow();
    return entries.stream()
      .filter(entry ->
        entry.attribute(timestampAttribute, Temporal.class)
          .flatMap(Attribute::value)
          .map(timestamp -> typeOf(timestampAttribute)
            .orElseThrow()
            .compare(timestamp, reference) <= 0)
          .orElse(false));
  }

  public Stream<DataEntry> tail(int count) {
    List<DataEntry> result = new ArrayList<>(entries()
      .reversed()
      .stream()
      .limit(count)
      .toList());

    Collections.reverse(result);

    return result.stream();
  }

  public Stream<DataEntry> tail(int amount, ChronoUnit unit) {
    String timestampAttribute = preferences.get(Preferences.TIMESTAMP_ATTRIBUTE).value();

    Temporal reference = entries().getLast()
      .attribute(timestampAttribute, Temporal.class)
      .flatMap(Attribute::value)
      .map(temporal -> temporal.minus(amount, unit))
      .orElseThrow();
    return entries()
      .stream()
      .filter(entry ->
        entry.attribute(timestampAttribute, Temporal.class)
          .flatMap(Attribute::value)
          .map(timestamp -> typeOf(timestampAttribute)
            .orElseThrow()
            .compare(timestamp, reference) >= 0)
          .orElse(false));
  }

  public NavigableSet<DataEntry> entries(DataFilter filter) {
    return registry().entries(filter);
  }

  public Stream<DataEntry> stream() {
    return registry().stream();
  }

  public boolean hasIndex(String attributeName) {
    return registry().indexedAttributes().contains(attributeName);
  }

  public <E> Map<E, NavigableSet<DataEntry>> index(String attributeName) {
    return registry().index(attributeName);
  }

  public <E> Set<E> valuesFor(String attributeName) {
    return registry().valuesFor(attributeName);
  }

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

  private class InternalStorage {
    private final NavigableSet<DataEntry> entries;
    private final Map<String, Map<Object, NavigableSet<DataEntry>>> index;
    private final Map<String, AttributeType> attributeTypes;

    public InternalStorage() {
      this.entries = new TreeSet<>();
      this.index = new HashMap<>();
      this.attributeTypes = new HashMap<>();
    }

    public synchronized void add(DataEntry entry) {
      this.entries.add(entry);

      entry.attributes()
        .forEach(attribute ->
          attributeTypes.put(attribute.name(), attribute.spec().type()));

      index.forEach((indexName, valuesMap) -> {
        Consumer<Object> addToIndex = value -> {
          if (!valuesMap.containsKey(value)) {
            valuesMap.put(value, new TreeSet<>());
          }
          valuesMap.get(value).add(entry);
        };
        if (indexName.contains(":")) {
          String[] attributes = indexName.split(":");
          String[] values = new String[attributes.length];
          for (int i = 0; i < attributes.length; i++) {
            String attr = attributes[i];
            if (!entry.hasAttribute(attr)) {
              return;
            }
            Optional<String> value = entry.attribute(attr)
              .flatMap(Attribute::formattedValue);
            if (value.isEmpty()) {
              return;
            }
            values[i] = value.get();
          }
          addToIndex.accept(String.join(":", values));
        } else {
          entry.attribute(indexName)
            .ifPresent(attr -> attr.values().forEach(addToIndex));
        }
      });
    }

    public DataRegistry addIndex(String attributeName) {
      this.index.putIfAbsent(attributeName, new HashMap<>());
      return DataRegistry.this;
    }

    public DataRegistry removeIndex(String attributeName) {
      this.index.remove(attributeName);
      return DataRegistry.this;
    }

    public Set<String> indexedAttributes() {
      return this.index.keySet();
    }

    public Optional<AttributeType> typeOf(String name) {
      return Optional.ofNullable(attributeTypes.get(name));
    }

    public Set<String> attributeNames() {
      return new HashSet<>(attributeTypes.keySet());
    }

    public NavigableSet<DataEntry> entries() {
      return entries;
    }

    public NavigableSet<DataEntry> entries(DataFilter filter) {
      if (filter == null) {
        return entries;
      }
      NavigableSet<DataEntry> filtered = new TreeSet<>();
      entries.stream().filter(filter).forEach(filtered::add);
      return filtered;
    }

    public Stream<DataEntry> stream() {
      return entries.stream();
    }

    public Map index(String attributeName) {
      return index.getOrDefault(attributeName, Collections.emptyMap());
    }

    public Set valuesFor(String attributeName) {
      return index.containsKey(attributeName) ?
        index.get(attributeName).keySet() :
        Collections.emptySet();
    }

    public boolean isEmpty() {
      return entries.isEmpty();
    }

    public int size() {
      return entries.size();
    }

    public Duration durationOf(String attributeName) {
      if (entries.size() >= 2) {
        DataEntry first = entries.getFirst();
        DataEntry last = entries.getLast();

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

    public void clear() {
      this.entries.clear();
      this.index.forEach((attr, map) -> map.clear());
      this.attributeTypes.clear();
    }
  }

}
