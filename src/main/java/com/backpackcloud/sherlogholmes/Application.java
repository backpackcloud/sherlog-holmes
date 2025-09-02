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
import com.backpackcloud.sherlogholmes.commands.config.MapperCommand;
import com.backpackcloud.sherlogholmes.commands.config.ModelCommand;
import com.backpackcloud.sherlogholmes.commands.config.ParserCommand;
import com.backpackcloud.sherlogholmes.commands.config.PipelineCommand;
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

import java.util.function.Consumer;
import java.util.stream.Stream;

public class Application {

  public int run(String... args) {
    SerialBitter serialBitter = SerialBitter.YAML();

    CLIBuilder builder = new CLIBuilder(serialBitter)
      .addComponent(FilterStack.class)
      .addComponent(DataRegistry.class)
      .addComponent(FilterFactory.class)
      .addComponent(FileSuggester.class)

      .register(Preferences.class);

    ConfigurationSupplier configSupplier = new ConfigurationSupplier("sherlog");

    Config config = serialBitter.deserialize(configSupplier.getDefault().read(), Config.class);

    Consumer<Configuration> merge = configuration -> config.mergeWith(
      serialBitter.deserialize(configuration.read(), Config.class)
    );

    configSupplier.fromUserHome().ifSet(merge);
    configSupplier.fromWorkingDir().ifSet(merge);

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
        SwapCommand.class,
        ModelCommand.class,
        ParserCommand.class,
        MapperCommand.class,
        PipelineCommand.class
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

    CLI cli = builder.build();

    cli.execute(config.commands().toArray(String[]::new));

    Stream.of(args).forEach(cli::execute);

    cli.start();

    return 0;
  }

}
