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

import jakarta.enterprise.context.ApplicationScoped;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@ApplicationScoped
@CommandDefinition(
  name = "tail",
  description = "Shows the last N entries",
  type = "Data Visualization",
  allowOutputRedirect = true,
  aliases = "last"
)
@RegisterForReflection
public class TailCommand implements AnnotatedCommand {

  private final DataRegistry registry;

  public TailCommand(DataRegistry registry) {
    this.registry = registry;
  }

  @Paginate
  @Action
  public Stream<DataEntry> execute(Integer amount, ChronoUnit unit) {
    if (registry.isEmpty()) {
      return Stream.empty();
    }

    Stream<DataEntry> stream;

    if (unit == null) {
      List<DataEntry> entries = new ArrayList<>(registry.entries()
        .descendingSet()
        .stream()
        .limit(amount)
        .toList());

      Collections.reverse(entries);

      return entries.stream();

    } else {
      Temporal reference = registry.entries().first()
        .attribute("timestamp", Temporal.class)
        .flatMap(Attribute::value)
        .map(temporal -> temporal.minus(amount, unit))
        .orElseThrow();
      return registry.entries()
        .stream()
        .filter(entry ->
          entry.attribute("timestamp", Temporal.class)
            .flatMap(Attribute::value)
            .map(timestamp -> registry.typeOf("timestamp")
              .orElseThrow()
              .compare(timestamp, reference) >= 0)
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
