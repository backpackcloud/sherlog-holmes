package com.backpackcloud.sherlogholmes.domain.chart;

import com.backpackcloud.cli.Registry;

import java.util.UUID;
import java.util.function.BiConsumer;

public interface ChartRegistry extends Registry {

  @Override
  default String name() {
    return "charts";
  }

  UUID add(ChartDefinition chartDefinition);

  void forEach(BiConsumer<UUID, ChartDefinition> consumer);

}
