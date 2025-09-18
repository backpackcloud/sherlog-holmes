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
import com.backpackcloud.cli.ui.Paginator;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.sherlogholmes.model.Count;
import com.backpackcloud.sherlogholmes.model.Counter;
import com.backpackcloud.sherlogholmes.model.DataRegistry;
import com.backpackcloud.sherlogholmes.ui.suggestions.AttributeSuggester;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@CommandDefinition(
  name = "count",
  type = "Data Visualization",
  description = "Counts the entries in the registry"
)
public class CountCommand {

  private final DataRegistry registry;
  private final AttributeSuggester attributeSuggester;

  public CountCommand(DataRegistry registry) {
    this.registry = registry;
    this.attributeSuggester = new AttributeSuggester(registry);
  }

  @Action
  public void execute(Writer writer,
                      Paginator paginator,
                      @InputParameter String attribute) {
    Map<?, AtomicInteger> valuesMap;

    if (registry.hasCounter(attribute)) {
      valuesMap = registry.counter(attribute);
    } else {
      Counter counter = new Counter();
      counter.add(attribute);
      registry.entries().parallel().forEach(counter);
      valuesMap = counter.counterFor(attribute).orElse(Collections.emptyMap());
    }

    Map<String, Count<?>> countMap = new HashMap<>();
    AtomicInteger total = new AtomicInteger();
    AtomicInteger nameLength = new AtomicInteger();

    valuesMap.entrySet().stream()
      .peek(entry -> total.addAndGet(entry.getValue().intValue()))
      .map(entry -> new Count<>(entry.getKey(), entry.getValue().intValue()))
      .peek(count -> nameLength.set((Math.max(nameLength.get(), count.object().toString().length()))))
      .forEach(count -> countMap.put(count.object().toString(), count));

    if (total.get() > 0) {
      int valueLength = Integer.toString(total.get()).length();

      boolean subset = total.get() < registry.size();

      paginator.from(
        countMap.values().stream()
          .sorted(Comparator.reverseOrder())
      ).print((paginatorWriter, count) -> {
        paginatorWriter
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
          paginatorWriter.withStyle("percentage//b")
            .write(String.format("%7.3f%%", count.percentageOf(registry.size())))
            .write(" ");
        }
      }).paginate();

      writer.write(String.format("%-" + Math.min(3, nameLength.get()) + "s ", "="))
        .withStyle("count//b")
        .write(total.get());

      writer.newLine();
    }
  }

  @ParameterSuggestion(parameter = "attribute")
  public List<Suggestion> execute() {
    return attributeSuggester.suggestCountedAttributes();
  }

}
