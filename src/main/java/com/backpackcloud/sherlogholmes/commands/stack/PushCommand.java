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

package com.backpackcloud.sherlogholmes.commands.stack;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.cli.annotations.Action;
import com.backpackcloud.cli.annotations.CommandDefinition;
import com.backpackcloud.cli.annotations.Line;
import com.backpackcloud.cli.annotations.ParameterCount;
import com.backpackcloud.cli.annotations.Suggestions;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.sherlogholmes.model.DataRegistry;
import com.backpackcloud.sherlogholmes.model.FilterFactory;
import com.backpackcloud.sherlogholmes.model.FilterStack;
import com.backpackcloud.sherlogholmes.ui.suggestions.AttributeSuggester;

import java.util.List;

@CommandDefinition(
  name = "push",
  event = "stack",
  type = "Stack Manipulation",
  description = "Adds the given filter to the stack."
)
public class PushCommand {

  private final FilterFactory filterFactory;
  private final FilterStack stack;

  private final AttributeSuggester attributeSuggester;

  public PushCommand(FilterFactory filterFactory, FilterStack stack, DataRegistry registry) {
    this.filterFactory = filterFactory;
    this.stack = stack;
    this.attributeSuggester = new AttributeSuggester(registry);
  }

  @Action
  public void execute(@Line String expression) {
    if (expression == null) {
      throw new UnbelievableException("No filter expression given");
    }
    stack.push(filterFactory.create(expression));
  }

  @Suggestions
  public List<? extends Suggestion> execute(@ParameterCount int paramCount, String attribute) {
    if (paramCount == 1) {
      return attributeSuggester.suggestAttributeNames();
    } else if (paramCount == 2) {
      return attributeSuggester.suggestOperands();
    } else {
      return attributeSuggester.suggestRegistryAttributeValues(attribute);
    }
  }

}
