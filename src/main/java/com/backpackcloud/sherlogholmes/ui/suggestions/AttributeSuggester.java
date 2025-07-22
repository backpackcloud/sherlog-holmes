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
import com.backpackcloud.sherlogholmes.model.AttributeType;
import com.backpackcloud.sherlogholmes.model.DataRegistry;
import com.backpackcloud.sherlogholmes.model.Operand;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class AttributeSuggester {

  private final DataRegistry registry;

  public AttributeSuggester(DataRegistry registry) {
    this.registry = registry;
  }

  public List<Suggestion> suggestAttributeNames() {
    return registry.attributeNames().stream()
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

  public List<Suggestion> suggestNonIndexedAttributeNames() {
    return registry.attributeNames().stream()
      .filter(attr -> !registry.hasIndex(attr))
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

  public List<Suggestion> suggestAttributeNames(Predicate<AttributeType> predicate) {
    return registry.attributeNames().stream()
      .filter(name -> predicate.test(registry.typeOf(name).orElse(AttributeType.TEXT)))
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

  public List<Suggestion> suggestAllIndexedAttributes() {
    return registry.indexedAttributes().stream()
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

  public List<Suggestion> suggestIndexedAttributes() {
    return registry.indexedAttributes().stream()
      .filter(attr -> !registry.index(attr).isEmpty())
      .map(PromptSuggestion::suggest)
      .collect(Collectors.toList());
  }

  public List<? extends Suggestion> suggestOperands() {
    List<Suggestion> result = new ArrayList<>();

    for (Operand operand : Operand.values()) {
      result.add(PromptSuggestion.suggest(operand.symbol()).describedAs(operand.name().toLowerCase()));
      result.add(PromptSuggestion.suggest(operand.name().toLowerCase()).describedAs(operand.symbol()));
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
