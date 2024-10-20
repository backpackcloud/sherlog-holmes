/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Marcelo Guimarães
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

import com.backpackcloud.cli.Action;
import com.backpackcloud.cli.AnnotatedCommand;
import com.backpackcloud.cli.CommandDefinition;
import com.backpackcloud.cli.PreferenceValue;
import com.backpackcloud.cli.Suggestions;
import com.backpackcloud.cli.Writer;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.cli.ui.impl.FileSuggester;
import com.backpackcloud.cli.ui.impl.PromptSuggestion;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.domain.DataReader;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.domain.Pipeline;
import com.backpackcloud.sherlogholmes.domain.readers.FileLineReader;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

@CommandDefinition(
  name = "inspect",
  description = "Inspects data inside the given file",
  type = "Data"
)
@ApplicationScoped
@RegisterForReflection
public class InspectCommand implements AnnotatedCommand {

  private final DataRegistry registry;
  private final Config config;
  private final FileSuggester suggester;

  public InspectCommand(DataRegistry registry, Config config) {
    this.registry = registry;
    this.config = config;
    this.suggester = new FileSuggester();
  }

  @Action
  public void execute(@PreferenceValue("show-added-entries") boolean showEntries,
                      @PreferenceValue("input-charset") String inputCharset,
                      Writer writer,
                      String pipelineId,
                      String location) {
    DataReader dataReader = new FileLineReader(Charset.forName(inputCharset));
    Pipeline pipeline = config.pipelineFor(pipelineId);

    pipeline.run(dataReader, location, entry -> {
      if (showEntries) {
        writer.writeln(entry);
      }
      registry.add(entry);
    });
  }

  @Suggestions
  public List<Suggestion> execute(String pipelineId, String location) {
    if (location == null) {
      return config.pipelines().keySet()
        .stream().map(PromptSuggestion::suggest)
        .collect(Collectors.toList());
    }
    return suggester.suggest(location);
  }

}
