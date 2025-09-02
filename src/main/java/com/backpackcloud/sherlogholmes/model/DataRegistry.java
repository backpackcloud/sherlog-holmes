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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class DataRegistry implements Registry {

  private final InternalStorage total = new InternalStorage();
  private final UserPreferences preferences;
  private final FilterStack filterStack;
  private InternalStorage filtered;

  private Limit limit;

  public DataRegistry(UserPreferences preferences, FilterStack filterStack) {
    this.preferences = preferences;
    this.filterStack = filterStack;
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
    if (filterStack.test(entry)) {
      total.add(entry);
      filtered().ifPresent(registry -> registry.add(entry));
    }
  }

  public void apply(DataFilter filter) {
    filtered = new InternalStorage();
    total.countedAttributes().forEach(filtered::addCounter);
    total.entries()
      .parallel()
      .filter(filter)
      .forEach(filtered::add);
  }

  public void removeFilter() {
    filtered = null;
  }

  public void addCounter(String attributeName) {
    total.addCounter(attributeName);
    filtered().ifPresent(registry -> registry.addCounter(attributeName));
  }

  public void removeCounter(String attributeName) {
    total.removeCounter(attributeName);
    filtered().ifPresent(registry -> registry.removeCounter(attributeName));
  }

  public Set<String> countedAttributes() {
    return registry().countedAttributes();
  }

  public Optional<AttributeType> typeOf(String name) {
    return registry().typeOf(name);
  }

  public Set<String> attributeNames() {
    return registry().attributeNames();
  }

  public boolean hasFilter() {
    return filtered().isPresent();
  }

  public Optional<Limit> limit() {
    return Optional.ofNullable(this.limit);
  }

  public void setLimit(Limit limit) {
    this.limit = limit;
  }

  public void clearLimit() {
    this.limit = null;
  }

  public Stream<DataEntry> entries() {
    if (limit != null) {
      if (limit.position() == Limit.Position.TAIL) {
        if (limit.type() == Limit.Type.TIME) {
          return tail(limit.amount(), limit.unit());
        } else {
          return tail(limit.amount());
        }
      } else {
        if (limit.type() == Limit.Type.TIME) {
          return head(limit.amount(), limit.unit());
        } else {
          return head(limit.amount());
        }
      }
    }
    return registry().entries();
  }

  public Stream<DataEntry> head(int count) {
    return head(registry().entries, count);
  }

  private Stream<DataEntry> head(NavigableSet<DataEntry> entries, int count) {
    if (entries.isEmpty()) {
      return Stream.empty();
    }

    return entries.stream().limit(count);
  }

  public Stream<DataEntry> head(int amount, ChronoUnit unit) {
    return head(registry().entries, amount, unit);
  }

  private Stream<DataEntry> head(NavigableSet<DataEntry> entries, int amount, ChronoUnit unit) {
    if (entries.isEmpty()) {
      return Stream.empty();
    }

    String timestampAttribute = preferences.get(Preferences.TIMESTAMP_ATTRIBUTE).value();

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
    return tail(registry().entries, count);
  }

  private Stream<DataEntry> tail(NavigableSet<DataEntry> entries, int count) {
    if (entries.isEmpty()) {
      return Stream.empty();
    }

    int start = entries.size() - count;

    return entries.stream().skip(start);
  }

  public Stream<DataEntry> tail(int amount, ChronoUnit unit) {
    return tail(registry().entries, amount, unit);
  }

  private Stream<DataEntry> tail(NavigableSet<DataEntry> entries, int amount, ChronoUnit unit) {
    if (entries.isEmpty()) {
      return Stream.empty();
    }
    String timestampAttribute = preferences.get(Preferences.TIMESTAMP_ATTRIBUTE).value();

    Temporal reference = entries.getLast()
      .attribute(timestampAttribute, Temporal.class)
      .flatMap(Attribute::value)
      .map(temporal -> temporal.minus(amount, unit))
      .orElseThrow();
    return entries.stream()
      .filter(entry ->
        entry.attribute(timestampAttribute, Temporal.class)
          .flatMap(Attribute::value)
          .map(timestamp -> typeOf(timestampAttribute)
            .orElseThrow()
            .compare(timestamp, reference) >= 0)
          .orElse(false));
  }

  public boolean hasCounter(String attributeName) {
    return registry().countedAttributes().contains(attributeName);
  }

  public Map<?, AtomicInteger> counter(String attributeName) {
    return registry().counter().counterFor(attributeName).orElse(Collections.emptyMap());
  }

  public Set<?> valuesFor(String attributeName) {
    return registry().counter().valuesFor(attributeName).orElse(Collections.emptySet());
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
    private final Counter counter;
    private final Map<String, AttributeType> attributeTypes;

    public InternalStorage() {
      this.entries = new TreeSet<>();
      this.counter = new Counter();
      this.attributeTypes = new ConcurrentHashMap<>();
    }

    public Counter counter() {
      return counter;
    }

    public synchronized void add(DataEntry entry) {
      this.entries.add(entry);

      entry.attributes()
        .forEach(attribute ->
          attributeTypes.put(attribute.name(), attribute.spec().type()));

      counter.accept(entry);
    }

    public DataRegistry addCounter(String name) {
      this.counter.add(name);
      return DataRegistry.this;
    }

    public DataRegistry removeCounter(String name) {
      this.counter.remove(name);
      return DataRegistry.this;
    }

    public Set<String> countedAttributes() {
      return this.counter.names();
    }

    public Optional<AttributeType> typeOf(String name) {
      return Optional.ofNullable(attributeTypes.get(name));
    }

    public Set<String> attributeNames() {
      return new HashSet<>(attributeTypes.keySet());
    }

    public Stream<DataEntry> entries() {
      return entries.stream();
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
      this.counter.clear();
      this.attributeTypes.clear();
    }
  }

}
