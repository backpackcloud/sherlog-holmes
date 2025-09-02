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

  public static final PreferenceSpec<String> TIMESTAMP_ATTRIBUTE = new PreferenceSpec<>(
    "timestamp-attribute",
    "which attribute holds a timestamp for time bound calculations",
    PreferenceType.TEXT,
    "timestamp"
  );

  public static final PreferenceSpec<String> COUNT_ATTRIBUTE = new PreferenceSpec<>(
    "count-attribute",
    "which attribute should be used for counting entries",
    PreferenceType.TEXT,
    ""
  );

  public static final PreferenceSpec<String> INPUT_CHARSET = new PreferenceSpec<>(
    "input-charset",
    "sets the charset to use for reading input files",
    PreferenceType.TEXT,
    "UTF-8"
  );

  public static final PreferenceSpec<Integer> FILE_READER_SKIP_LINES = new PreferenceSpec<>(
    "file-reader-skip-lines",
    "sets how many lines should be skipped when reading files (useful for CSVs with headers)",
    PreferenceType.NUMBER,
    "0"
  );

  public static final PreferenceSpec<String> STACK_OPERATION = new PreferenceSpec<>(
    "stack-operation",
    "sets the stack operation for creating the filter (and/or)",
    PreferenceType.TEXT,
    "and"
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
    "true"
  );

  private Preferences() {

  }
}
