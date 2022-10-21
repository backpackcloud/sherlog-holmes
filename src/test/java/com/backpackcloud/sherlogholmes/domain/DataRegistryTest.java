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
import com.backpackcloud.sherlogholmes.impl.DataRegistryImpl;
import com.backpackcloud.spectaculous.Backstage;
import com.backpackcloud.spectaculous.TargetedAction;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

public class DataRegistryTest {

  private final AttributeType<String> fooBarType = AttributeType.enumOf("foo", "bar");
  private final TargetedAction<DataRegistry> addEntry = registry -> registry.add(newEntry());

  private int count = 0;

  private DataEntry newEntry() {
    DataEntry entry = new DataEntryImpl();
    entry.add("timestamp", LocalDateTime.now());
    entry.add("random", Math.random());
    entry.add("fooBar", String.class)
      .ofType(fooBarType)
      .withValue(count++ % 2 == 0 ? "foo" : "bar");

    return entry;
  }

  @Test
  public void testAdd() {
    Backstage.describe(DataRegistry.class)
      .given(new DataRegistryImpl())

      .from(DataRegistry::size).expect(0)
      .from(DataRegistry::isEmpty).expect(true)

      .then(addEntry.repeat(100))

      .from(DataRegistry::size).expect(100)
      .from(DataRegistry::isEmpty).expect(false);
  }

  @Test
  public void testIndex() {
    Backstage.describe(DataRegistry.class)
      .given(new DataRegistryImpl())

      .then(registry -> registry.addIndex("fooBar"))
      .then(addEntry.repeat(100))

      .from(registry -> registry.valuesFor("fooBar")).expect(set -> set.size() == 2)

      .then(registry -> registry.removeIndex("fooBar"))

      .from(registry -> registry.valuesFor("fooBar")).expect(Set::isEmpty)

      .then(addEntry.repeat(100))

      .then(registry -> registry.addIndex("fooBar"))
      .from(registry -> registry.valuesFor("fooBar")).expect(Set::isEmpty);
  }

}
