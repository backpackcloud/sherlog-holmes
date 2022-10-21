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

import com.backpackcloud.sherlogholmes.impl.AttributeImpl;
import com.backpackcloud.spectaculous.Backstage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AttributeTest {

  @Test
  public void testInstantiation() {
    AttributeType type = mock(AttributeType.class);
    Attribute attribute = new AttributeImpl("test", type, "foo");

    assertEquals(type, attribute.type());
    assertEquals("foo", attribute.value().orElse(null));
  }

  @Test
  public void testValueSet() {
    AttributeType type = mock(AttributeType.class);
    when(type.isValid("bar")).thenReturn(true);

    Backstage.describe(Attribute.class)
      .given(new AttributeImpl("test", type, "foo"))
      .then(attr -> attr.assign("bar"))

      .from(attr -> attr.value().orElse(null)).expect("bar");

    verify(type).isValid("bar");
  }

  @Test
  public void testInvalidValueSet() {
    AttributeType type = mock(AttributeType.class);
    when(type.isValid("bar")).thenReturn(false);

    Backstage.describe(Attribute.class)
      .given(new AttributeImpl<String>("test", type, "foo"))

      .when(attr -> attr.assign("bar")).expectFailure()

      .from(attr -> (String) attr.value().orElse(null)).expect("foo");

    verify(type).isValid("bar");
  }

}
