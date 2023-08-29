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

package com.backpackcloud.sherlogholmes.domain.types;

import com.backpackcloud.sherlogholmes.domain.AttributeType;

import java.util.HashMap;
import java.util.Map;

public class EnumType implements AttributeType<String> {

  private final Map<String, Integer> valuesMap;

  public EnumType(String... values) {
    this.valuesMap = new HashMap<>();

    for (int i = 0; i < values.length; i++) {
      this.valuesMap.put(values[i], i);
    }
  }

  @Override
  public String name() {
    return "enum";
  }

  @Override
  public String convert(String input) {
    return input;
  }

  @Override
  public boolean isValid(String value) {
    return this.valuesMap.containsKey(value);
  }

  @Override
  public int compare(String o1, String o2) {
    return this.valuesMap.getOrDefault(o1, -1)
      .compareTo(this.valuesMap.getOrDefault(o2, -1));
  }

}
