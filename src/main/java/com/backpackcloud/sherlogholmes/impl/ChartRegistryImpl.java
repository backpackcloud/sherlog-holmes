package com.backpackcloud.sherlogholmes.impl;

import com.backpackcloud.sherlogholmes.domain.chart.Chart;
import com.backpackcloud.sherlogholmes.domain.chart.ChartDefinition;
import com.backpackcloud.sherlogholmes.domain.chart.ChartRegistry;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

@ApplicationScoped
public class ChartRegistryImpl implements ChartRegistry {

  private final Map<UUID, ChartDefinition> charts;

  public ChartRegistryImpl() {
    this.charts = new HashMap<>();
  }

  @Override
  public UUID add(ChartDefinition chartDefinition) {
    UUID id = UUID.randomUUID();
    charts.put(id, chartDefinition);
    return id;
  }

  @Override
  public void forEach(BiConsumer<UUID, ChartDefinition> consumer) {
    charts.forEach(consumer);
  }

  @Override
  public boolean isEmpty() {
    return charts.isEmpty();
  }

  @Override
  public int size() {
    return charts.size();
  }

  @Override
  public void clear() {
    charts.clear();
  }

}
