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

package com.backpackcloud.sherlogholmes.ui.suggestions;

import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.cli.ui.components.PromptSuggestion;
import com.backpackcloud.sherlogholmes.model.DataRegistry;
import com.backpackcloud.sherlogholmes.model.Operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AttributeSuggester {

  private final DataRegistry registry;

  public AttributeSuggester(DataRegistry registry) {
    this.registry = registry;
  }

  public List<Suggestion> suggestAllAttributes() {
    Set<String> strings = registry.attributeNames();
    strings.addAll(registry.countedAttributes());
    return strings
      .stream()
      .map(PromptSuggestion::suggest)
      .distinct()
      .collect(Collectors.toList());
  }

  public List<Suggestion> suggestAttributeNames() {
    return registry.attributeNames().stream()
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

  public List<Suggestion> suggestNonCountedAttributeNames() {
    return registry.attributeNames().stream()
      .filter(attr -> !registry.hasCounter(attr))
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

  public List<Suggestion> suggestAllCountedAttributes() {
    return registry.countedAttributes().stream()
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

  public List<Suggestion> suggestCountedAttributes() {
    return registry.countedAttributes().stream()
      .filter(attr -> !registry.counter(attr).isEmpty())
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

  public List<? extends Suggestion> suggestOperations() {
    List<Suggestion> result = new ArrayList<>();

    for (Operation operation : Operation.values()) {
      result.add(
        PromptSuggestion.suggest(operation.symbol())
          .describedAs(operation.name().toLowerCase().replace("_", " "))
      );
    }

    return result;
  }

  public List<Suggestion> suggestRegistryAttributeValues(String attribute) {
    return registry.valuesFor(attribute)
      .stream()
      .map(String::valueOf)
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

}
