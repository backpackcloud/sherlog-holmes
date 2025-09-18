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

import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class DataRegistry implements Registry {

  private static final String[] FULL_TIMESTAMP_ATTRIBUTE_ORDER = {"timestamp", "source", "line"};
  private static final String[] TIME_ONLY_ATTRIBUTE_ORDER = {"source", "line", "timestamp"};
  private static final String[] NO_TIMESTAMP_ATTRIBUTE_ORDER = {"source", "line"};

  private String[] attributeOrder;
  private final Comparator<DataEntry> comparator;

  private final InternalStorage total;
  private final FilterStack filterStack;
  private InternalStorage filtered;

  private Limit limit;

  public DataRegistry(FilterStack filterStack) {
    this.filterStack = filterStack;
    this.comparator = (left, right) -> {
      // ideally, the entries will have at least the source and the line attributes,
      // so the chances of this comparison to yield zero are really low
      for (String attributeName : attributeOrder) {
        if (left.hasAttribute(attributeName) && right.hasAttribute(attributeName)) {
          int result = left.attribute(attributeName)
            .map(attribute -> right.attribute(attributeName)
              .map(attribute::compareTo)
              .orElse(1))
            .orElse(0);

          if (result != 0) {
            return result;
          }
        }
      }
      // well... if it's indeed zero, let's just use the natural comparison between then
      return left.compareTo(right);
    };
    this.total = new InternalStorage();
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
      if (attributeOrder == null) {
        entry.attribute("timestamp").ifPresentOrElse(attribute ->
            attribute.value().ifPresentOrElse(value -> {
              if (value instanceof LocalTime) {
                attributeOrder = TIME_ONLY_ATTRIBUTE_ORDER;
              } else {
                attributeOrder = FULL_TIMESTAMP_ATTRIBUTE_ORDER;
              }
            }, () -> attributeOrder = NO_TIMESTAMP_ATTRIBUTE_ORDER),
          () -> attributeOrder = NO_TIMESTAMP_ATTRIBUTE_ORDER
        );
      }

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

  private Stream<DataEntry> head(SequencedCollection<DataEntry> entries, int count) {
    if (entries.isEmpty()) {
      return Stream.empty();
    }

    return entries.stream().limit(count);
  }

  public Stream<DataEntry> head(int amount, ChronoUnit unit) {
    return head(registry().entries, amount, unit);
  }

  private Stream<DataEntry> head(SequencedCollection<DataEntry> entries, int amount, ChronoUnit unit) {
    if (entries.isEmpty()) {
      return Stream.empty();
    }

    Temporal reference = entries.getFirst()
      .attribute("timestamp", Temporal.class)
      .flatMap(Attribute::value)
      .map(temporal -> temporal.plus(amount, unit))
      .orElseThrow();
    return entries.stream()
      .filter(entry ->
        entry.attribute("timestamp", Temporal.class)
          .flatMap(Attribute::value)
          .map(timestamp -> typeOf("timestamp")
            .orElseThrow()
            .compare(timestamp, reference) <= 0)
          .orElse(false));
  }

  public Stream<DataEntry> tail(int count) {
    return tail(registry().entries, count);
  }

  private Stream<DataEntry> tail(SequencedCollection<DataEntry> entries, int count) {
    if (entries.isEmpty()) {
      return Stream.empty();
    }

    int start = entries.size() - count;

    return entries.stream().skip(start);
  }

  public Stream<DataEntry> tail(int amount, ChronoUnit unit) {
    return tail(registry().entries, amount, unit);
  }

  private Stream<DataEntry> tail(SequencedCollection<DataEntry> entries, int amount, ChronoUnit unit) {
    if (entries.isEmpty()) {
      return Stream.empty();
    }

    Temporal reference = entries.getLast()
      .attribute("timestamp", Temporal.class)
      .flatMap(Attribute::value)
      .map(temporal -> temporal.minus(amount, unit))
      .orElseThrow();
    return entries.stream()
      .filter(entry ->
        entry.attribute("timestamp", Temporal.class)
          .flatMap(Attribute::value)
          .map(timestamp -> typeOf("timestamp")
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

  public Duration duration() {
    return durationOf("timestamp");
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
    attributeOrder = null;
  }

  private class InternalStorage {
    private final SequencedCollection<DataEntry> entries;
    private final Counter counter;
    private final Map<String, AttributeType> attributeTypes;

    public InternalStorage() {
      this.entries = new TreeSet<>(comparator);
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
