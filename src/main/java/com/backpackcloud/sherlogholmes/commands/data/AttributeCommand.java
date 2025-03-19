package com.backpackcloud.sherlogholmes.commands.data;

import com.backpackcloud.cli.annotations.Action;
import com.backpackcloud.cli.annotations.CommandDefinition;
import com.backpackcloud.cli.annotations.Line;
import com.backpackcloud.sherlogholmes.model.AttributeSpec;
import com.backpackcloud.sherlogholmes.model.DataRegistry;

@CommandDefinition(
  name = "attr",
  description = "Creates or remove entry attributes",
  type = "Data Manipulation"
)
public class AttributeCommand {

  private final DataRegistry registry;

  public AttributeCommand(DataRegistry registry) {
    this.registry = registry;
  }

  @Action
  public void add(String name, @Line String specExpression) {
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
