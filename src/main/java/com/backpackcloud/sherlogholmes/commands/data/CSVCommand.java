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

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.cli.Action;
import com.backpackcloud.cli.AnnotatedCommand;
import com.backpackcloud.cli.CommandDefinition;
import com.backpackcloud.cli.PreferenceValue;
import com.backpackcloud.cli.Suggestions;
import com.backpackcloud.cli.ui.Paginator;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.ui.suggestions.AttributeSuggester;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
@CommandDefinition(
  name = "csv",
  description = "Creates a csv content with the desired log attributes and/or tags",
  type = "Data Visualization",
  allowOutputRedirect = true
)
@RegisterForReflection
public class CSVCommand implements AnnotatedCommand {

  private final DataRegistry registry;

  private final AttributeSuggester attributeSuggester;

  public CSVCommand(DataRegistry registry) {
    this.registry = registry;
    this.attributeSuggester = new AttributeSuggester(registry);
  }

  @Action
  public void printRawData(Paginator paginator,
                           @PreferenceValue("csv-header") boolean showHeader,
                           String... fields) {
    if (fields.length == 0) {
      throw new UnbelievableException("No field given");
    }

    List<List<String>> data = new ArrayList<>();
    List<String> selectedFields = List.of(fields);

    if (showHeader) {
      data.add(selectedFields);
    }

    registry.entries().stream().map(entry -> {
      List<String> row = new ArrayList<>();

      selectedFields.stream().map(name ->
          entry.attribute(name)
            .map(attribute -> attribute.type().format(attribute.value()))
            .orElseThrow())
        .forEach(row::add);

      return row;
    }).forEach(data::add);

    paginator.from(data)
      .print((writer, row) -> writer.writeln(String.join(",", row)))
      .paginate();
  }

  @Suggestions
  public List<Suggestion> suggestAttributes() {
    return attributeSuggester.suggestAttributeNames();
  }

}
