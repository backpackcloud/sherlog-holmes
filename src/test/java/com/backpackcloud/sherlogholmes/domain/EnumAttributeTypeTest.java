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

package com.backpackcloud.sherlogholmes.domain;

import com.backpackcloud.spectaculous.Backstage;
import org.junit.jupiter.api.Test;

public class EnumAttributeTypeTest {
  private final AttributeType<String> enumType = AttributeType.enumOf("debug", "info", "warn", "error");

  @Test
  public void testValidValues() {
    Backstage.describe(AttributeType.class)
      .given(enumType)

      .from(type -> type.isValid("debug")).expect(true)
      .from(type -> type.isValid("info")).expect(true)
      .from(type -> type.isValid("warn")).expect(true)
      .from(type -> type.isValid("error")).expect(true)
      .from(type -> type.isValid("fatal")).expect(false)
      .from(type -> type.isValid("DEBUG")).expect(false);
  }

  @Test
  public void testOrder() {
    Backstage.describe(AttributeType.class)
      .given(enumType)

      .from(type -> type.compare("debug", "info")).expect(-1)
      .from(type -> type.compare("debug", "warn")).expect(-1)
      .from(type -> type.compare("debug", "error")).expect(-1)

      .from(type -> type.compare("error", "info")).expect(1)
      .from(type -> type.compare("error", "warn")).expect(1)
      .from(type -> type.compare("error", "debug")).expect(1)

      .from(type -> type.compare("debug", "debug")).expect(0)
      .from(type -> type.compare("warn", "warn")).expect(0)
      .from(type -> type.compare("error", "error")).expect(0);
    ;
  }

}
