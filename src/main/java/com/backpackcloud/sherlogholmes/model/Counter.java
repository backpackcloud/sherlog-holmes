package com.backpackcloud.sherlogholmes.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Counter implements Consumer<DataEntry> {

  private final Map<String, Map<Object, AtomicInteger>> counters = new ConcurrentHashMap<>();

  public void add(String name) {
    counters.putIfAbsent(name, new ConcurrentHashMap<>());
  }

  public void remove(String name) {
    counters.remove(name);
  }

  public Set<String> names() {
    return new HashSet<>(counters.keySet());
  }

  public void accept(DataEntry entry) {
    counters.forEach((counterName, valuesMap) -> {
      Consumer<Object> addToIndex = value -> {
        if (!valuesMap.containsKey(value)) {
          valuesMap.put(value, new AtomicInteger());
        }
        valuesMap.get(value).incrementAndGet();
      };
      if (counterName.contains(":")) {
        String[] attributes = counterName.split(":");
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
        entry.attribute(counterName)
          .ifPresent(attr -> attr.values().forEach(addToIndex));
      }
    });
  }

  public Optional<Map<?, AtomicInteger>> counterFor(String name) {
    return Optional.ofNullable(this.counters.get(name));
  }

  public Optional<Set<?>> valuesFor(String name) {
    return Optional.ofNullable(this.counters.get(name))
      .map(Map::keySet);
  }

  public void clear() {
    this.counters.forEach((key, map) -> map.clear());
  }

}
