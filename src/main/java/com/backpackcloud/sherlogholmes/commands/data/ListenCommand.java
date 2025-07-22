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

package com.backpackcloud.sherlogholmes.commands.data;

import com.backpackcloud.cli.Writer;
import com.backpackcloud.cli.annotations.Action;
import com.backpackcloud.cli.annotations.CommandDefinition;
import com.backpackcloud.cli.annotations.InputParameter;
import com.backpackcloud.cli.annotations.ParameterSuggestion;
import com.backpackcloud.cli.annotations.PreferenceValue;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.cli.ui.components.PromptSuggestion;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.model.DataReader;
import com.backpackcloud.sherlogholmes.model.DataRegistry;
import com.backpackcloud.sherlogholmes.model.Pipeline;
import com.backpackcloud.sherlogholmes.model.readers.SocketDataReader;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@CommandDefinition(
  name = "listen",
  description = "Listen for log entries arriving on a given port",
  type = "Data"
)
public class ListenCommand {

  private final ExecutorService executorService;

  private final DataRegistry registry;
  private final Config config;

  public ListenCommand(DataRegistry registry, Config config) {
    this.registry = registry;
    this.config = config;
    this.executorService = Executors.newCachedThreadPool();
  }

  @Action
  public void execute(@PreferenceValue("input-charset") String inputCharset,
                      Writer writer,
                      @InputParameter String pipelineId,
                      @InputParameter String portsInput) {
    List<Integer> ports = new ArrayList<>();

    String[] split = portsInput.split(",");
    for (String segment : split) {
      String[] range = segment.split("-", 2);
      int from = Integer.parseInt(range[0]);
      int to = range.length == 1 ? from : Integer.parseInt(range[1]);

      while (from <= to) {
        ports.add(from++);
      }
    }

    DataReader<Integer> dataReader = new SocketDataReader(Charset.forName(inputCharset));
    Pipeline pipeline = config.pipelineFor(pipelineId);

    ports.stream()
      .map(port -> (Runnable) () -> pipeline.run(dataReader, port, entry -> {
        registry.add(entry);
        writer.writeln(entry);
      }))
      .forEach(this.executorService::submit);
  }

  @ParameterSuggestion(parameter = "pipelineId")
  public List<Suggestion> suggestPipeline() {
    return config.pipelines().keySet()
      .stream().map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

}
