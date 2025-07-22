package com.backpackcloud.sherlogholmes.commands.config;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.cli.Writer;
import com.backpackcloud.cli.annotations.Action;
import com.backpackcloud.cli.annotations.CommandDefinition;
import com.backpackcloud.cli.annotations.InputParameter;
import com.backpackcloud.cli.annotations.ParameterSuggestion;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.cli.ui.components.PromptSuggestion;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.config.mapper.ColumnDataMapperConfig;
import com.backpackcloud.sherlogholmes.config.mapper.FunctionDataMapperConfig;

import java.util.List;

@CommandDefinition(
  name = "mapper",
  description = "Manages the mappers available",
  type = "Configuration"
)
public class MapperCommand {

  private final Config config;

  public MapperCommand(Config config) {
    this.config = config;
  }

  @Action("create")
  public void createMapper(@InputParameter String type, @InputParameter String name, @InputParameter String... args) {
    switch (type) {
      case "column":
        if (args.length == 0) {
          throw new UnbelievableException("Columns not supplied");
        }
        this.config.mappers().put(name, new ColumnDataMapperConfig(args));
        break;
      case "function":
        this.config.mappers().put(name, FunctionDataMapperConfig.from(String.join(",", args)));
        break;
      default:
        throw new UnbelievableException("Unknown mapper type");
    }
  }

  @Action("list")
  public void listMappers(Writer writer) {
    this.config.mappers().forEach((name, mapper) -> {
      writer.write(name).write(":\t").writeln(mapper.toString());
    });
  }

  @ParameterSuggestion(action = "create", parameter = "type")
  public List<Suggestion> suggestMapperType() {
    return List.of(
      PromptSuggestion.suggest("column"),
      PromptSuggestion.suggest("function")
    );
  }

}
