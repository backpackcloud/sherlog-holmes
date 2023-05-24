package com.backpackcloud.sherlogholmes.ui.prompt;

import com.backpackcloud.cli.ui.Prompt;
import com.backpackcloud.cli.ui.PromptWriter;
import com.backpackcloud.sherlogholmes.domain.chart.ChartRegistry;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ChartCountPromptWriter implements PromptWriter {

  private final ChartRegistry registry;

  public ChartCountPromptWriter(ChartRegistry registry) {
    this.registry = registry;
  }

  @Override
  public String name() {
    return "chart-count";
  }

  @Override
  public void addTo(Prompt prompt, PromptSide side) {
    prompt.newSegment("data_count")
      .addIcon("nf-fa-bar_chart")
      .add(registry.size());
  }

}
