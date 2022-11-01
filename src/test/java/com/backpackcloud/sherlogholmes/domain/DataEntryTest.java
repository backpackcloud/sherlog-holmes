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

import com.backpackcloud.sherlogholmes.impl.DataEntryImpl;
import com.backpackcloud.spectaculous.Backstage;
import org.junit.jupiter.api.Test;

public class DataEntryTest {

  private DataEntry dataEntry() {
    return new DataEntryImpl();
  }

  @Test
  public void testInstance() {
    Backstage.describe(DataEntry.class)
      .given(dataEntry())
      .from(entry -> entry.attributes().isEmpty()).expect(true);
  }

  @Test
  public void testTimestampAttribute() {
    Backstage.describe(DataEntry.class)
      .given(dataEntry())

      .then(entry -> entry.addAttribute("timestamp").ofType(AttributeType.DATE))

      .from(entry -> entry.attributes().size()).expect(1);
  }

  @Test
  public void testAddAndRemoveAttribute() {
    Backstage.describe(DataEntry.class)
      .given(dataEntry())
      .then(entry -> entry.addAttribute("foo"))

      .from(entry -> entry.hasAttribute("foo")).expect(true)

      .then(entry -> entry.remove("foo"))
      .from(entry -> entry.hasAttribute("foo")).expect(false);
  }

}
