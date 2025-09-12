/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Marcelo "Ataxexe" Guimarães
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.backpackcloud.sherlogholmes;

import com.backpackcloud.cli.CLI;
import com.backpackcloud.cli.builder.CLIBuilder;
import com.backpackcloud.cli.ui.components.FileSuggester;
import com.backpackcloud.cli.ui.prompt.CloseSegmentsWriter;
import com.backpackcloud.cli.ui.prompt.NewLineWriter;
import com.backpackcloud.cli.ui.prompt.PromptCharWriter;
import com.backpackcloud.configuration.Configuration;
import com.backpackcloud.configuration.ConfigurationSupplier;
import com.backpackcloud.io.SerialBitter;
import com.backpackcloud.sherlogholmes.commands.data.*;
import com.backpackcloud.sherlogholmes.commands.stack.AndOperationCommand;
import com.backpackcloud.sherlogholmes.commands.stack.DupCommand;
import com.backpackcloud.sherlogholmes.commands.stack.NotOperationCommand;
import com.backpackcloud.sherlogholmes.commands.stack.OrOperationCommand;
import com.backpackcloud.sherlogholmes.commands.stack.PopCommand;
import com.backpackcloud.sherlogholmes.commands.stack.PushCommand;
import com.backpackcloud.sherlogholmes.commands.stack.StackCommand;
import com.backpackcloud.sherlogholmes.commands.stack.SwapCommand;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.model.DataRegistry;
import com.backpackcloud.sherlogholmes.model.FilterFactory;
import com.backpackcloud.sherlogholmes.model.FilterStack;
import com.backpackcloud.sherlogholmes.ui.prompt.DataCountPromptWriter;
import com.backpackcloud.sherlogholmes.ui.prompt.DataTimeRangePromptWriter;
import com.backpackcloud.sherlogholmes.ui.prompt.FilterStackPromptWriter;
import com.backpackcloud.sherlogholmes.ui.prompt.LimitPromptWriter;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

@CommandLine.Command(
  name = "sherlog-holmes",
  mixinStandardHelpOptions = true,
  description = "Summons your companion for logging errands.",
  version = "2.0.0"
)
public class Application implements Callable<Integer> {

  @CommandLine.Option(
    names = {"-c", "--config"},
    description = "The config files to use."
  )
  File[] configFiles;

  @CommandLine.Option(
    names = {"-p", "--preference"},
    description = "Sets a user preference."
  )
  String[] preferences;

  @CommandLine.Option(
    names = {"-d", "--default"},
    description = "Adds the default config file."
  )
  boolean useDefaultConfig;

  @CommandLine.Parameters(
    description = "Additional commands to run as soon as the CLI is ready."
  )
  String[] commands;

  public Integer call() {
    Configuration banner = Configuration.resource("META-INF/banner.txt");
    System.out.println(banner.read());

    SerialBitter serialBitter = SerialBitter.YAML();

    FilterFactory filterFactory = new FilterFactory();
    FilterStack filterStack = new FilterStack();

    CLIBuilder builder = new CLIBuilder(serialBitter)
      .addComponent(filterStack, FilterStack.class)
      .addComponent(filterFactory, FilterFactory.class)
      .addComponent(DataRegistry.class)
      .addComponent(FileSuggester.class)

      .registerPreferences(Preferences.class);

    ConfigurationSupplier configSupplier = new ConfigurationSupplier("sherlog");
    List<Configuration> configurations = new ArrayList<>();

    Config config = null;

    if (configFiles != null && configFiles.length > 0) {
      if (useDefaultConfig) {
        addDefaults(configurations, configSupplier);
      }

      Stream.of(configFiles)
        .filter(File::exists)
        .map(File::getPath)
        .map(Configuration::file)
        .forEach(configurations::add);

    } else {
      if (useDefaultConfig) {
        addDefaults(configurations, configSupplier);
      }
    }

    if (configurations.isEmpty()) {
      addDefaults(configurations, configSupplier);
    }

    for (Configuration configuration : configurations) {
      if (configuration.isSet()) {
        Config parsedConfiguration = serialBitter.deserialize(configuration.read(), Config.class);
        if (config == null) {
          config = parsedConfiguration;
        } else {
          config.mergeWith(parsedConfiguration);
        }
      }
    }

    builder
      .addComponent(config, Config.class)

      .addCommands(
        AssignCommand.class,
        CountCommand.class,
        FilterCommand.class,
        HeadCommand.class,
        CounterCommand.class,
        InspectCommand.class,
        ListDataCommand.class,
        ExportDataCommand.class,
        ListenCommand.class,
        LimitCommand.class,
        TailCommand.class,
        AndOperationCommand.class,
        DupCommand.class,
        NotOperationCommand.class,
        OrOperationCommand.class,
        PopCommand.class,
        PushCommand.class,
        StackCommand.class,
        SwapCommand.class
      )

      .addMacros(config.macros())

      .addLeftPrompt(
        DataCountPromptWriter.class,
        DataTimeRangePromptWriter.class,
        FilterStackPromptWriter.class,
        CloseSegmentsWriter.class,
        NewLineWriter.class,
        PromptCharWriter.class
      )

      .addRightPrompt(LimitPromptWriter.class)
      .addDefaultRightPrompts();

    if (preferences != null) {
      for (String preferenceExpression : preferences) {
        String[] tokens = preferenceExpression.split("=", 2);
        builder.setPreference(tokens[0], tokens[1]);
      }
    }

    config.filters().forEach((name, filter) -> {
      filterStack.save(name, filter);
    });

    CLI cli = builder.build();

    cli.execute(config.commands().toArray(String[]::new));

    if (commands != null) {
      Stream.of(commands).forEach(cli::execute);
    }

    cli.start();

    return 0;
  }

  private static void addDefaults(List<Configuration> configurations, ConfigurationSupplier configSupplier) {
    configurations.add(configSupplier.getDefault());

    configSupplier.fromUserHome().ifSet(configurations::add);
    configSupplier.fromWorkingDir().ifSet(configurations::add);
  }

}
