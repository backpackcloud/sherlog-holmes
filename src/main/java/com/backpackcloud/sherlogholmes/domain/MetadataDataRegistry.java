package com.backpackcloud.sherlogholmes.domain;

import com.backpackcloud.cli.preferences.UserPreferences;
import com.backpackcloud.sherlogholmes.Preferences;

import java.time.Duration;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class MetadataDataRegistry implements DataRegistry {

  private final DataRegistry registry;
  private final UserPreferences preferences;

  public MetadataDataRegistry(DataRegistry registry, UserPreferences userPreferences) {
    this.registry = registry;
    this.preferences = userPreferences;
  }

  @Override
  public String name() {
    return registry.name();
  }

  @Override
  public void add(DataEntry entry) {
    registry.add(entry);
  }

  @Override
  public void apply(DataFilter filter) {
    registry.apply(filter);
  }

  @Override
  public void removeFilter() {
    registry.removeFilter();
  }

  @Override
  public DataRegistry addIndex(String attributeName) {
    return registry.addIndex(attributeName);
  }

  @Override
  public DataRegistry removeIndex(String attributeName) {
    return registry.removeIndex(attributeName);
  }

  @Override
  public Set<String> indexedAttributes() {
    return registry.indexedAttributes();
  }

  @Override
  public Optional<AttributeType> typeOf(String name) {
    return registry.typeOf(name);
  }

  @Override
  public Set<String> attributeNames() {
    return registry.attributeNames();
  }

  @Override
  public NavigableSet<DataEntry> entries() {
    return registry.entries();
  }

  @Override
  public NavigableSet<DataEntry> entries(DataFilter filter) {
    return registry.entries(filter);
  }

  @Override
  public Stream<DataEntry> stream() {
    if (preferences.isEnabled(Preferences.SHOW_METADATA)) {
      String prefix;
      if (registry.index("$source").size() > 1) {
        prefix = "{#$source}:{$line} |";
      } else {
        prefix = ":{$line} |";
      }
      return registry.stream()
        .map(entry -> entry.displayFormat(prefix + entry.displayFormat()));
    }
    return registry.stream();
  }

  @Override
  public <E> Map<E, NavigableSet<DataEntry>> index(String attributeName) {
    return registry.index(attributeName);
  }

  @Override
  public <E> Set<E> valuesFor(String attributeName) {
    return registry.valuesFor(attributeName);
  }

  @Override
  public Duration durationOf(String attribute) {
    return registry.durationOf(attribute);
  }

  @Override
  public boolean isEmpty() {
    return registry.isEmpty();
  }

  @Override
  public int size() {
    return registry.size();
  }

  @Override
  public void clear() {
    registry.clear();
  }

}
