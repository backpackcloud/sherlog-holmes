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

package com.backpackcloud.sherlogholmes.ui.prompt;

import com.backpackcloud.cli.ui.Prompt;
import com.backpackcloud.cli.ui.PromptWriter;
import com.backpackcloud.sherlogholmes.model.DataRegistry;

public class LimitPromptWriter implements PromptWriter {

  private final DataRegistry registry;

  public LimitPromptWriter(DataRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void addTo(Prompt prompt, PromptSide side) {
    registry.limit().ifPresent(limit -> prompt.newSegment()
      .addIcon("data", "icon-limit")
      .add(registry.entries().count())
      .addIcon("limit", "icon-limit")
      .add(limit.toString()));
  }

}
