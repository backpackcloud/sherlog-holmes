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
import com.backpackcloud.cli.Suggestions;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.cli.ui.impl.FileSuggester;
import com.backpackcloud.cli.ui.impl.PromptSuggestion;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.domain.DataModel;
import com.backpackcloud.sherlogholmes.domain.DataParser;
import com.backpackcloud.sherlogholmes.domain.DataReader;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
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
  public void execute(String readerId, String parserId, String modelId, String location) {
    DataReader<?> dataReader = config.dataReader(readerId).orElseThrow();
    DataParser dataParser = config.dataParser(parserId).orElseThrow();
    DataModel dataModel = config.dataModel(modelId).orElseThrow();

    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + location);
    File directory = new File(location).getParentFile();

    if (directory != null) {
      for (File file : directory.listFiles()) {
        String path = file.getPath();
        if (matcher.matches(Path.of(path))) {
          dataReader.read(path, dataModel.dataSupplier(), dataParser, registry::add);
        }
      }
    } else {
      dataReader.read(location, dataModel.dataSupplier(), dataParser, registry::add);
    }

  }

  @Suggestions
  public List<Suggestion> suggest(String readerId, String parserId, String modelId, String location) {
    if (parserId == null) {
      return config.readers().keySet()
        .stream().map(PromptSuggestion::suggest)
        .collect(Collectors.toList());
    }
    if (modelId == null) {
      return config.parsers().keySet()
        .stream().map(PromptSuggestion::suggest)
        .collect(Collectors.toList());
    }
    if (location == null) {
      return config.models().keySet()
        .stream().map(PromptSuggestion::suggest)
        .collect(Collectors.toList());
    }
    return suggester.suggest(location);
  }

}
