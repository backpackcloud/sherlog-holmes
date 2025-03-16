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

package com.backpackcloud.sherlogholmes.model;

import com.backpackcloud.UnbelievableException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AttributeTest {

  @Test
  public void testInstantiation() {
    AttributeType type = mock(AttributeType.class);

    Attribute attribute = new Attribute("test", new AttributeSpec(type, false));

    assertEquals(type, attribute.spec().type());
    assertTrue(attribute.value().isEmpty());
  }

  @Test
  public void testValueSet() {
    AttributeType type = mock(AttributeType.class);
    when(type.isValid("bar")).thenReturn(true);

    Attribute attr = new Attribute("test", new AttributeSpec(type, false));
    attr.assign("bar");

    assertEquals("bar", attr.value().orElse(null));

    verify(type).isValid("bar");
  }

  @Test
  public void testInvalidValueSet() {
    AttributeType type = mock(AttributeType.class);
    when(type.isValid("foo")).thenReturn(true);
    when(type.isValid("bar")).thenReturn(false);

    Attribute attr = new Attribute("test", new AttributeSpec(type, false)).assign("foo");

    assertThrows(UnbelievableException.class, () -> attr.assign("bar"));
    assertEquals("foo", attr.value().orElse(null));

    verify(type).isValid("bar");
  }

}
