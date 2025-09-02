package com.backpackcloud.sherlogholmes.commands.config;

import com.backpackcloud.cli.Writer;
import com.backpackcloud.cli.annotations.Action;
import com.backpackcloud.cli.annotations.CommandDefinition;
import com.backpackcloud.cli.annotations.InputParameter;
import com.backpackcloud.cli.annotations.ParameterSuggestion;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.cli.ui.components.FileSuggester;
import com.backpackcloud.cli.ui.components.PromptSuggestion;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.config.PipelineConfig;
import com.backpackcloud.sherlogholmes.config.mapper.ColumnDataMapperConfig;
import com.backpackcloud.sherlogholmes.config.model.DataAttributeConfig;
import com.backpackcloud.sherlogholmes.config.model.DataModelConfig;
import com.backpackcloud.sherlogholmes.config.parser.CsvDataParserConfig;
import com.backpackcloud.sherlogholmes.model.AttributeSpec;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandDefinition(
  name = "model",
  description = "Manages the data models available",
  type = "Configuration"
)
public class ModelCommand {

  private final Config config;

  private String name;
  private String format;
  private String exportFormat;
  private String modelsToInclude;
  private Map<String, DataAttributeConfig> attributes;
  private List<Consumer<DataModelConfig>> actions;

  public ModelCommand(Config config) {
    this.config = config;
  }

  @Action("new")
  public void newModel(@InputParameter String name) {
    this.name = name;
    this.format = null;
    this.modelsToInclude = null;
    this.attributes = new HashMap<>();
    this.actions = new ArrayList<>();
  }

  @Action("format")
  public void setFormat(@InputParameter String format) {
    this.format = format;
  }

  @Action("export-format")
  public void setExportFormat(@InputParameter String format) {
    this.exportFormat = format;
  }

  @Action("include")
  public void setModelsToInclude(@InputParameter String modelsToInclude) {
    this.modelsToInclude = modelsToInclude;
  }

  @Action("attr")
  public void addAttribute(@InputParameter String name, @InputParameter String spec) {
    this.attributes.put(name, new DataAttributeConfig(spec));
  }

  @Action("from-csv")
  public void loadFromCsv(Writer writer, @InputParameter String location) throws IOException, CsvValidationException {
    try (BufferedReader reader = Files.newBufferedReader(Path.of(location))) {
      String line = reader.readLine();
      try (CSVReader csvReader = new CSVReader(new StringReader(line))) {
        String[] attributes = Stream.of(csvReader.readNext())
          .map(attr -> attr.replaceAll("\\s+", "-"))
          .toArray(String[]::new);
        for (String attribute : attributes) {
          this.attributes.put(attribute, new DataAttributeConfig("text"));
          writer.writeln(attribute);
        }
        if (this.format == null) {
          StringBuilder builder = new StringBuilder();
          for (String attribute : attributes) {
            builder.append(attribute).append(": {").append(attribute).append("}\n");
          }
          this.format = builder.toString();
        }
        this.actions.add(model -> {
          // Since we parsed the header, let's skip the first line
          config.parsers().putIfAbsent(name, new CsvDataParserConfig(true));
          config.mappers().putIfAbsent(name, new ColumnDataMapperConfig(attributes));
          config.pipelines().putIfAbsent(name, new PipelineConfig(name, name, name, new String[0]));
        });
      }
    }
  }

  @Action("save")
  public void saveModel(Writer writer) {
    DataModelConfig modelConfig = new DataModelConfig(
      this.format,
      this.exportFormat,
      this.modelsToInclude,
      this.attributes
    );
    this.config.models().put(this.name, modelConfig);
    printModel(writer, this.name, modelConfig);
    this.actions.forEach(action -> action.accept(modelConfig));
  }

  @Action("load")
  public void loadModel(@InputParameter String name) {
    DataModelConfig model = this.config.modelConfig(name);
    this.name = name;
    this.format = model.format();
    this.modelsToInclude = model.modelsToInclude();
    this.attributes = model.attributes();
  }

  @Action("list")
  public void listModels(Writer writer) {
    this.config.models().forEach((name, config) -> {
      printModel(writer, name, config);
    });
  }

  private static void printModel(Writer writer, String name, DataModelConfig config) {
    writer.withStyle("model-property").write("Name: ");
    writer.withStyle("model-name").writeln(name);
    if (config.format() != null) {
      writer.withStyle("model-property").write("Format: ");
      writer.writeln(config.format());
    }
    if (config.modelsToInclude() != null) {
      writer.withStyle("model-property").write("Includes: ");
      writer.writeln(config.modelsToInclude());
    }
    if (!config.attributes().isEmpty()) {
      writer.withStyle("model-property").writeln("Attributes: ");
      config.attributes().forEach((attrName, attrConfig) -> {
        writer.withStyle("model-attribute-name").write("\t" + attrName).write(":\t");
        String specString = attrConfig.spec();
        AttributeSpec<?> spec = AttributeSpec.create(specString);
        writer.withStyle("model-attribute-type-" + spec.type().name()).writeln(specString);
      });
    }
  }

  @ParameterSuggestion(action = "from-csv", parameter = "location")
  public List<Suggestion> suggestLocations(@InputParameter String location) {
    return new FileSuggester().suggest(location);
  }

  @ParameterSuggestion(action = "load")
  @ParameterSuggestion(action = "include")
  public List<Suggestion> suggestModels() {
    return this.config.models().keySet().stream()
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

}
