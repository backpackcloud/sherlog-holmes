package com.backpackcloud.sherlogholmes.commands.data;

import com.backpackcloud.cli.Action;
import com.backpackcloud.cli.AnnotatedCommand;
import com.backpackcloud.cli.CommandDefinition;
import com.backpackcloud.cli.RawInput;
import com.backpackcloud.sherlogholmes.model.AttributeSpec;
import com.backpackcloud.sherlogholmes.model.DataRegistry;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;

@CommandDefinition(
  name = "attr",
  description = "Creates or remove entry attributes",
  type = "Data Manipulation"
)
@RegisterForReflection
@ApplicationScoped
public class AttributeCommand implements AnnotatedCommand {

  private final DataRegistry registry;

  public AttributeCommand(DataRegistry registry) {
    this.registry = registry;
  }

  @Action
  public void add(String name, @RawInput String specExpression) {
    AttributeSpec spec = AttributeSpec.create(specExpression);

    registry.stream().forEach(entry -> {
      if (!entry.hasAttribute(name)) {
        entry.addAttribute(name, spec);
      }
    });
  }

  @Action
  public void remove(String name) {
    registry.stream().forEach(entry -> {
      if (entry.hasAttribute(name)) {
        entry.remove(name);
      }
    });
  }

}
