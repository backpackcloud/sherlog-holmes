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
import com.backpackcloud.cli.CommandContext;
import com.backpackcloud.cli.CommandDefinition;
import com.backpackcloud.cli.ParameterCount;
import com.backpackcloud.cli.Suggestions;
import com.backpackcloud.cli.ui.Paginator;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.sherlogholmes.domain.Count;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.ui.suggestions.AttributeSuggester;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
@CommandDefinition(
  name = "count",
  type = "Data Visualization",
  description = "Counts the entries in the registry",
  allowOutputRedirect = true
)
@RegisterForReflection
public class CountCommand implements AnnotatedCommand {

  private final DataRegistry registry;
  private final AttributeSuggester attributeSuggester;

  public CountCommand(DataRegistry registry) {
    this.registry = registry;
    this.attributeSuggester = new AttributeSuggester(registry);
  }

  @Action
  public void execute(CommandContext context, Paginator paginator, String attribute) {
    Map<?, NavigableSet<DataEntry>> valuesMap = registry.index(attribute);

    Map<String, Count<?>> countMap = new HashMap<>();
    AtomicInteger total = new AtomicInteger();
    AtomicInteger nameLength = new AtomicInteger();

    valuesMap.entrySet().stream()
      .peek(entry -> total.addAndGet(entry.getValue().size()))
      .map(entry -> new Count<>(entry.getKey(), entry.getValue().size()))
      .peek(count -> nameLength.set((Math.max(nameLength.get(), count.object().toString().length()))))
      .forEach(count -> countMap.put(count.object().toString(), count));

    int valueLength = Integer.toString(total.get()).length();

    boolean subset = total.get() != registry.size();

    paginator.from(
      countMap.values().stream()
        .sorted(Comparator.reverseOrder())
    ).print((writer, count) -> {
      writer
        .withStyle("name")
        .write(String.format("%-" + nameLength + "s", count.object()))
        .write(" ")

        .withStyle("count")
        .write(String.format("%" + valueLength + "d", count.value()))
        .write(" ")

        .withStyle("percentage//i")
        .write(String.format("%7.3f%%", count.percentageOf(total.get())))
        .write(" ");

      if (subset) {
        writer.withStyle("percentage//b")
          .write(String.format("%7.3f%%", count.percentageOf(registry.size())))
          .write(" ");
      }

      writer.newLine();
    }).paginate();

    if (subset && !countMap.isEmpty()) {
      Integer totalCount = countMap.values().stream()
        .map(Count::value)
        .reduce(0, Integer::sum, Integer::sum);

      context.writer().write(String.format("%-" + Math.min(3, nameLength.get()) + "s ", "="))
        .withStyle("count//b")
        .write(total.get())

        // we're not adding any number to the table
        .write(" ".repeat(10))

        .withStyle("percentage//b")
        .write(String.format("%7.3f%%", 100.0 * totalCount / registry.size()))
        .newLine();
    }
  }

  @Suggestions
  public List<Suggestion> execute(@ParameterCount int paramCount) {
    if (paramCount == 1) {
      return attributeSuggester.suggestIndexedAttributes();
    }
    return Collections.emptyList();
  }

}
