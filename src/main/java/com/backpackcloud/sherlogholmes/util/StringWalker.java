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

package com.backpackcloud.sherlogholmes.util;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringWalker {

  public static final Pattern DEFAULT_INTERPOLATION_PATTERN = Pattern.compile("\\{(?<token>[^}]+)}");

  private final Pattern pattern;
  private final Consumer<String> textConsumer;
  private final Consumer<String> patternConsumer;

  public StringWalker(Consumer<String> textConsumer, Consumer<String> patternConsumer) {
    this(DEFAULT_INTERPOLATION_PATTERN, textConsumer, patternConsumer);
  }

  public StringWalker(Pattern pattern, Consumer<String> textConsumer, Consumer<String> patternConsumer) {
    this.pattern = pattern;
    this.textConsumer = textConsumer;
    this.patternConsumer = patternConsumer;
  }

  public void walk(String value) {
    if (value == null || value.isBlank()) return;

    Matcher matcher = pattern.matcher(value);

    int pos = 0;
    while (matcher.find()) {
      if (matcher.start() > pos) {
        textConsumer.accept(value.substring(pos, matcher.start()));
      }
      patternConsumer.accept(matcher.group("token"));
      pos = matcher.end();
    }

    if (pos < value.length()) {
      textConsumer.accept(value.substring(pos));
    }
  }

}