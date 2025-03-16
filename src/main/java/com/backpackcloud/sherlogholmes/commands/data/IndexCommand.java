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
import com.backpackcloud.cli.Writer;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.cli.ui.components.PromptSuggestion;
import com.backpackcloud.sherlogholmes.model.AttributeType;
import com.backpackcloud.sherlogholmes.model.DataRegistry;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.stream.Collectors;

@CommandDefinition(
  name = "index",
  description = "shows index information",
  type = "Data Visualization"
)
@ApplicationScoped
@RegisterForReflection
public class IndexCommand implements AnnotatedCommand {

  private final DataRegistry registry;

  public IndexCommand(DataRegistry registry) {
    this.registry = registry;
  }

  @Action
  public void execute(Writer writer, String attribute) {
    if (attribute == null) {
      registry.indexedAttributes().stream()
        .map(registry::index);
    } else {
      registry.index(attribute).forEach((value, entries) ->
        writer.write(registry.typeOf(attribute).orElse(AttributeType.TEXT).format(value))
          .writeln(String.format(" -> %d", entries.size()))
      );
    }
  }

  @Suggestions
  public List<Suggestion> execute() {
    return registry.indexedAttributes().stream()
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

}
