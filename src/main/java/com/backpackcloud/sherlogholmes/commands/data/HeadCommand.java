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
import com.backpackcloud.cli.Paginate;
import com.backpackcloud.cli.Suggestions;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.sherlogholmes.domain.Attribute;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.ui.suggestions.ChronoUnitSuggestions;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.enterprise.context.ApplicationScoped;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import java.util.stream.Stream;

@ApplicationScoped
@CommandDefinition(
  name = "head",
  description = "Shows the first N entries",
  type = "Data Visualization",
  allowOutputRedirect = true,
  aliases = "first"
)
@RegisterForReflection
public class HeadCommand implements AnnotatedCommand {

  private final DataRegistry registry;

  public HeadCommand(DataRegistry registry) {
    this.registry = registry;
  }

  @Action
  @Paginate
  public Stream<DataEntry> execute(Integer amount, ChronoUnit unit) {
    if (registry.isEmpty()) {
      return Stream.empty();
    }

    if (unit == null) {
      return registry.stream()
        .limit(amount);
    } else {
      NavigableSet<DataEntry> entries = registry.entries();
      Temporal reference = entries.first()
        .attribute("timestamp", Temporal.class)
        .flatMap(Attribute::value)
        .map(temporal -> temporal.plus(amount, unit))
        .orElseThrow();
      return entries.stream()
        .filter(entry ->
          entry.attribute("timestamp", Temporal.class)
            .flatMap(Attribute::value)
            .map(timestamp -> registry.typeOf("timestamp")
              .orElseThrow()
              .compare(timestamp, reference) <= 0)
            .orElse(false));
    }
  }

  @Suggestions
  public List<Suggestion> execute(String amount, String unit) {
    if (unit != null) {
      return ChronoUnitSuggestions.suggestUnits();
    }
    return Collections.emptyList();
  }

}
