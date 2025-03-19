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

import com.backpackcloud.cli.annotations.Action;
import com.backpackcloud.cli.CommandContext;
import com.backpackcloud.cli.annotations.CommandDefinition;
import com.backpackcloud.cli.annotations.ParameterCount;
import com.backpackcloud.cli.annotations.Suggestions;
import com.backpackcloud.cli.ui.Paginator;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.preferences.UserPreferences;
import com.backpackcloud.sherlogholmes.Preferences;
import com.backpackcloud.sherlogholmes.model.Attribute;
import com.backpackcloud.sherlogholmes.model.Count;
import com.backpackcloud.sherlogholmes.model.DataEntry;
import com.backpackcloud.sherlogholmes.model.DataRegistry;
import com.backpackcloud.sherlogholmes.ui.suggestions.AttributeSuggester;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.atomic.AtomicInteger;

@CommandDefinition(
  name = "count",
  type = "Data Visualization",
  description = "Counts the entries in the registry"
)
public class CountCommand {

  private final DataRegistry registry;
  private final AttributeSuggester attributeSuggester;
  private final UserPreferences preferences;

  public CountCommand(DataRegistry registry, UserPreferences preferences) {
    this.registry = registry;
    this.attributeSuggester = new AttributeSuggester(registry);
    this.preferences = preferences;
  }

  private int count(NavigableSet<DataEntry> set, String countAttribute) {
    if (countAttribute == null || countAttribute.isEmpty()) {
      return set.size();
    }
    AtomicInteger count = new AtomicInteger();

    set.forEach(entry -> {
      entry.attribute(countAttribute)
        .flatMap(Attribute::value)
        .map(value -> {
          if (value instanceof Integer i) {
            return i;
          }
          return Integer.parseInt(value.toString());
        })
        .ifPresent(count::addAndGet);
    });

    return count.get();
  }

  @Action
  public void execute(CommandContext context,
                      Paginator paginator,
                      String attribute,
                      String counter) {
    Map<?, NavigableSet<DataEntry>> valuesMap = registry.index(attribute);
    String countAttribute = counter != null ? counter : preferences.get(Preferences.COUNT_ATTRIBUTE).value();

    Map<String, Count<?>> countMap = new HashMap<>();
    AtomicInteger total = new AtomicInteger();
    AtomicInteger nameLength = new AtomicInteger();

    valuesMap.entrySet().stream()
      .peek(entry -> total.addAndGet(count(entry.getValue(), countAttribute)))
      .map(entry -> new Count<>(entry.getKey(), count(entry.getValue(), countAttribute)))
      .peek(count -> nameLength.set((Math.max(nameLength.get(), count.object().toString().length()))))
      .forEach(count -> countMap.put(count.object().toString(), count));

    if (total.get() > 0) {
      int valueLength = Integer.toString(total.get()).length();

      boolean subset = countAttribute == null && total.get() < registry.size();

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

      context.writer().write(String.format("%-" + Math.min(3, nameLength.get()) + "s ", "="))
        .withStyle("count//b")
        .write(total.get());

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
          .write(String.format("%7.3f%%", 100.0 * totalCount / registry.size()));
      }

      context.writer().newLine();
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
