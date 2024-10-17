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

package com.backpackcloud.sherlogholmes;

import com.backpackcloud.cli.preferences.PreferenceSpec;

public enum Preferences implements PreferenceSpec {

  TIMESTAMP_ATTRIBUTE(
    "which attribute holds a timestamp for time bound calculations",
    Type.TEXT,
    "timestamp"
  ),

  COUNT_ATTRIBUTE(
    "which attribute should be used for counting entries",
    Type.TEXT,
    ""
  ),

  DEFAULT_FALLBACK_MODE(
    "sets the default fallback behavior to use when parsing content fails",
    Type.TEXT,
    "ignore"
  ),

  SHOW_ADDED_ENTRIES(
    "shows entries as they are added in the registry",
    Type.FLAG,
    "false"
  ),

  SHOW_METADATA(
    "shows the parsed metadata when viewing entries",
    Type.FLAG,
    "false"
  ),

  STACK_OPERATION(
    "sets the stack operation for creating the filter (and/or)",
    Type.TEXT,
    "and"
  ),

  REMOVE_ANSI_COLORS(
    "removes the ansi colors from the readed content",
    Type.FLAG,
    "true"
  );

  private final String id;
  private final String description;
  private final Type type;
  private final String defaultValue;

  Preferences(String description, Type type, String defaultValue) {
    this.id = name().toLowerCase().replaceAll("_", "-");
    this.description = description;
    this.type = type;
    this.defaultValue = defaultValue;
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public String description() {
    return description;
  }

  @Override
  public Type type() {
    return type;
  }

  @Override
  public String defaultValue() {
    return defaultValue;
  }

}
