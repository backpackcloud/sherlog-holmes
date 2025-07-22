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
import com.backpackcloud.sherlogholmes.config.parser.CsvDataParserConfig;
import com.backpackcloud.sherlogholmes.config.parser.JsonDataParserConfig;
import com.backpackcloud.sherlogholmes.config.parser.RegexDataParserConfig;
import com.backpackcloud.sherlogholmes.config.parser.SplitDataParserConfig;

import java.util.List;

@CommandDefinition(
  name = "parser",
  description = "Manages the parsers available",
  type = "Configuration"
)
public class ParserCommand {

  private final Config config;

  public ParserCommand(Config config) {
    this.config = config;
  }

  @Action("create")
  public void createParser(@InputParameter String type, @InputParameter String name, @InputParameter String... args) {
    switch (type) {
      case "regex":
        if (args.length == 0) {
          throw new UnbelievableException("Pattern not supplied");
        }
        this.config.parsers().put(name, new RegexDataParserConfig(args[0]));
        break;
      case "csv":
        boolean skipFirstLine = false;
        if (args.length > 1) {
          skipFirstLine = Boolean.parseBoolean(args[1]);
        }
        this.config.parsers().put(name, new CsvDataParserConfig(skipFirstLine));
        break;
      case "json":
        this.config.parsers().put(name, new JsonDataParserConfig());
        break;
      case "split":
        if (args.length == 0) {
          throw new UnbelievableException("Pattern not supplied");
        }
        String pattern = args[0];
        int limit = 0;
        if (args.length > 2) {
          limit = Integer.parseInt(args[1]);
        }
        this.config.parsers().put(name, new SplitDataParserConfig(pattern, limit));
        break;
      default:
        throw new UnbelievableException("Unknown parser type");
    }
  }

  @Action("list")
  public void listParsers(Writer writer) {
    this.config.parsers().forEach((name, parser) -> {
      writer.write(name).write(":\t").writeln(parser.toString());
    });
  }

  @ParameterSuggestion(action = "create", parameter = "type")
  public List<Suggestion> suggestParserTypes() {
    return List.of(
      PromptSuggestion.suggest("regex"),
      PromptSuggestion.suggest("csv"),
      PromptSuggestion.suggest("json"),
      PromptSuggestion.suggest("split")
    );
  }

}
