package com.backpackcloud.sherlogholmes.commands.data;

import com.backpackcloud.cli.annotations.Action;
import com.backpackcloud.cli.annotations.CommandDefinition;
import com.backpackcloud.cli.annotations.Line;
import com.backpackcloud.sherlogholmes.model.DataRegistry;
import com.backpackcloud.sherlogholmes.model.PipelineStep;
import com.backpackcloud.sherlogholmes.model.steps.AttributeSetStep;

import java.util.HashMap;
import java.util.Map;

@CommandDefinition(
  name = "assign",
  description = "Assigns a value to an attribute",
  type = "Data Manipulation"
)
public class AssignCommand {

  private final DataRegistry registry;

  public AssignCommand(DataRegistry registry) {
    this.registry = registry;
  }

  @Action
  public void execute(String name, @Line String value) {
    Map<String, String> assignMap = new HashMap<>();
    assignMap.put(name, value);

    PipelineStep attributeSet = new AttributeSetStep(assignMap);

    registry.stream().forEach(attributeSet::analyze);
  }

}
