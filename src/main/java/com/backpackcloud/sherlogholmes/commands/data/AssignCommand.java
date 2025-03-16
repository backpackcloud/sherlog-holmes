package com.backpackcloud.sherlogholmes.commands.data;

import com.backpackcloud.cli.Action;
import com.backpackcloud.cli.AnnotatedCommand;
import com.backpackcloud.cli.CommandDefinition;
import com.backpackcloud.cli.RawInput;
import com.backpackcloud.sherlogholmes.model.DataRegistry;
import com.backpackcloud.sherlogholmes.model.PipelineStep;
import com.backpackcloud.sherlogholmes.model.steps.AttributeSetStep;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.HashMap;
import java.util.Map;

@CommandDefinition(
  name = "assign",
  description = "Assigns a value to an attribute",
  type = "Data Manipulation"
)
@RegisterForReflection
@ApplicationScoped
public class AssignCommand implements AnnotatedCommand {

  private final DataRegistry registry;

  public AssignCommand(DataRegistry registry) {
    this.registry = registry;
  }

  @Action
  public void execute(String name, @RawInput String value) {
    Map<String, String> assignMap = new HashMap<>();
    assignMap.put(name, value);

    PipelineStep attributeSet = new AttributeSetStep(assignMap);

    registry.stream().forEach(attributeSet::analyze);
  }

}
