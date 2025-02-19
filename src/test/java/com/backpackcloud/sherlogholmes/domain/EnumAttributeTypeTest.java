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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EnumAttributeTypeTest {
  private final AttributeType<String> type = AttributeType.enumOf("debug", "info", "warn", "error");

  @Test
  public void testValidValues() {
    assertTrue(type.isValid("debug"));
    assertTrue(type.isValid("info"));
    assertTrue(type.isValid("warn"));
    assertTrue(type.isValid("error"));
    assertFalse(type.isValid("fatal"));
    assertFalse(type.isValid("DEBUG"));
  }

  @Test
  public void testOrder() {
    assertEquals(-1, type.compare("debug", "info"));
    assertEquals(-1, type.compare("debug", "warn"));
    assertEquals(-1, type.compare("debug", "error"));

    assertEquals(1, type.compare("error", "info"));
    assertEquals(1, type.compare("error", "warn"));
    assertEquals(1, type.compare("error", "debug"));

    assertEquals(0, type.compare("debug", "debug"));
    assertEquals(0, type.compare("warn", "warn"));
    assertEquals(0, type.compare("error", "error"));
  }

}
