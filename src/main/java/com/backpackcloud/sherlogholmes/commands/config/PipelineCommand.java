package com.backpackcloud.sherlogholmes.commands.config;

import com.backpackcloud.cli.Writer;
import com.backpackcloud.cli.annotations.Action;
import com.backpackcloud.cli.annotations.CommandDefinition;
import com.backpackcloud.cli.annotations.InputParameter;
import com.backpackcloud.cli.annotations.ParameterSuggestion;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.cli.ui.components.PromptSuggestion;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.config.PipelineConfig;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CommandDefinition(
  name = "pipeline",
  description = "Manages the pipelines available",
  type = "Configuration"
)
public class PipelineCommand {

  private final Config config;

  public PipelineCommand(Config config) {
    this.config = config;
  }

  @Action
  public void create(@InputParameter String pipelineName,
                     @InputParameter String modelName,
                     @InputParameter String parserName,
                     @InputParameter String mapperName,
                     @InputParameter String[] steps) {
    this.config.pipelines().put(
      pipelineName,
      new PipelineConfig(
        modelName,
        parserName,
        mapperName,
        steps
      )
    );
  }

  @Action
  public void list(Writer writer) {
    this.config.pipelines().forEach((key, pipeline) -> {
      writer.withStyle("pipeline-name").writeln(key);

      writer.withStyle("pipeline-property-name").write("\t").write("model:\t\t");
      writer.withStyle("pipeline-property-value").writeln(pipeline.modelId());

      writer.withStyle("pipeline-property-name").write("\t").write("parser:\t\t");
      writer.withStyle("pipeline-property-value").writeln(pipeline.parserId());

      writer.withStyle("pipeline-property-name").write("\t").write("mapper:\t\t");
      writer.withStyle("pipeline-property-value").writeln(pipeline.mapperId());

      writer.withStyle("pipeline-property-name").write("\t").write("steps:\t\t");
      writer.withStyle("pipeline-property-value").writeln(Arrays.toString(pipeline.stepsIds()));
    });
  }

  @ParameterSuggestion(action = "create", parameter = "modelName")
  public List<Suggestion> suggestModels() {
    return this.config.models()
      .keySet()
      .stream()
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

  @ParameterSuggestion(action = "create", parameter = "parserName")
  public List<Suggestion> suggestParsers() {
    return this.config.parsers()
      .keySet()
      .stream()
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

  @ParameterSuggestion(action = "create", parameter = "mapperName")
  public List<Suggestion> suggestMappers() {
    return this.config.mappers()
      .keySet()
      .stream()
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

  @ParameterSuggestion(action = "create", parameter = "steps")
  public List<Suggestion> suggestSteps() {
    return this.config.steps()
      .keySet()
      .stream()
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

}
