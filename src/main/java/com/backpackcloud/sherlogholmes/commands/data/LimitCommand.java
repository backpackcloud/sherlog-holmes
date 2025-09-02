package com.backpackcloud.sherlogholmes.commands.data;

import com.backpackcloud.cli.annotations.Action;
import com.backpackcloud.cli.annotations.CommandDefinition;
import com.backpackcloud.cli.annotations.InputParameter;
import com.backpackcloud.cli.annotations.ParameterSuggestion;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.cli.ui.components.PromptSuggestion;
import com.backpackcloud.sherlogholmes.model.DataRegistry;
import com.backpackcloud.sherlogholmes.model.Limit;

import java.time.temporal.ChronoUnit;
import java.util.List;

@CommandDefinition(
  name = "limit",
  description = "Limits the entries",
  type = "Data Visualization"
)
public class LimitCommand {

  private final DataRegistry registry;

  public LimitCommand(DataRegistry registry) {
    this.registry = registry;
  }

  @Action
  public void first(@InputParameter Integer amount, @InputParameter ChronoUnit unit) {
    this.registry.setLimit(new Limit(amount, unit, Limit.Position.HEAD));
  }

  @Action
  public void last(@InputParameter Integer amount, @InputParameter ChronoUnit unit) {
    this.registry.setLimit(new Limit(amount, unit, Limit.Position.TAIL));
  }

  @Action
  public void off() {
    this.registry.clearLimit();
  }

  @Action
  public Limit show() {
    return this.registry.limit().orElse(null);
  }

  @ParameterSuggestion(parameter = "unit")
  public List<Suggestion> suggestUnits() {
    return PromptSuggestion.suggest(ChronoUnit.class);
  }


}
