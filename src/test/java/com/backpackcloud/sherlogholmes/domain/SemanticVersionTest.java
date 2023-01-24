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

import com.backpackcloud.sherlogholmes.domain.types.SemanticVersion;
import com.backpackcloud.spectaculous.Backstage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SemanticVersionTest {

  @Test
  public void testParse() {
    Backstage.describe(SemanticVersion.class)
      .given(SemanticVersion.of("1.2.3"))
      .from(SemanticVersion::major).expect(1)
      .from(SemanticVersion::minor).expect(2)
      .from(SemanticVersion::micro).expect(3)

      .given(SemanticVersion.of("1.2"))
      .from(SemanticVersion::major).expect(1)
      .from(SemanticVersion::minor).expect(2)
      .from(SemanticVersion::micro).expect(0)

      .given(SemanticVersion.of("1"))
      .from(SemanticVersion::major).expect(1)
      .from(SemanticVersion::minor).expect(0)
      .from(SemanticVersion::micro).expect(0);
  }

  @Test
  public void testComparison() {
    SemanticVersion a = new SemanticVersion(3, 1, 0);
    SemanticVersion b = new SemanticVersion(3, 1, 1);

    Assertions.assertTrue(a.compareTo(b) < 0);
    Assertions.assertTrue(b.compareTo(a) > 0);
  }

}
