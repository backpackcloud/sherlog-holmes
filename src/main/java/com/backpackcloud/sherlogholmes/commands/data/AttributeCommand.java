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

import com.backpackcloud.cli.annotations.Action;
import com.backpackcloud.cli.annotations.CommandDefinition;
import com.backpackcloud.cli.annotations.Line;
import com.backpackcloud.sherlogholmes.model.AttributeSpec;
import com.backpackcloud.sherlogholmes.model.DataRegistry;

@CommandDefinition(
  name = "attr",
  description = "Creates or remove entry attributes",
  type = "Data Manipulation"
)
public class AttributeCommand {

  private final DataRegistry registry;

  public AttributeCommand(DataRegistry registry) {
    this.registry = registry;
  }

  @Action
  public void add(String name, @Line String specExpression) {
    AttributeSpec spec = AttributeSpec.create(specExpression);

    registry.stream().forEach(entry -> {
      if (!entry.hasAttribute(name)) {
        entry.addAttribute(name, spec);
      }
    });
  }

  @Action
  public void remove(String name) {
    registry.stream().forEach(entry -> {
      if (entry.hasAttribute(name)) {
        entry.remove(name);
      }
    });
  }

}
