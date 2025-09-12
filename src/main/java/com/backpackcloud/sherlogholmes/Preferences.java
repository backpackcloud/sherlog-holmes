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

package com.backpackcloud.sherlogholmes;

import com.backpackcloud.preferences.PreferenceSpec;
import com.backpackcloud.preferences.PreferenceType;

public final class Preferences {

  public static final PreferenceSpec<String> INPUT_CHARSET = new PreferenceSpec<>(
    "input-charset",
    "sets the charset to use for reading input files",
    PreferenceType.TEXT,
    "UTF-8"
  );

  public static final PreferenceSpec<String> OUTPUT_CHARSET = new PreferenceSpec<>(
    "output-charset",
    "sets the charset to use for writing to output files",
    PreferenceType.TEXT,
    "UTF-8"
  );

  public static final PreferenceSpec<Boolean> REMOVE_ANSI_COLORS = new PreferenceSpec<>(
    "remove-ansi-colors",
    "removes the ansi colors from the readed content",
    PreferenceType.FLAG,
    "true"
  );

  public static final PreferenceSpec<Boolean> AUTO_FILTER = new PreferenceSpec<>(
    "auto-filter",
    "applies filter on every stack manipulation",
    PreferenceType.FLAG,
    "false"
  );

  private Preferences() {

  }
}
