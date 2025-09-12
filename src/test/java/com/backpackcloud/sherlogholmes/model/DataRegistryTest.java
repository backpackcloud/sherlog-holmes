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

package com.backpackcloud.sherlogholmes.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataRegistryTest {

  private final AttributeType<String> fooBarType = AttributeType.enumOf("foo", "bar");
  private int count = 0;

  private DataEntry newEntry() {
    DataEntry entry = new DataEntry();
    entry.addAttribute("timestamp", LocalDateTime.now());
    entry.addAttribute("random", Math.random());
    entry.addAttribute("fooBar", String.class)
      .ofType(fooBarType)
      .withValue(count++ % 2 == 0 ? "foo" : "bar");

    return entry;
  }

  @Test
  public void testAdd() {
    DataRegistry registry = new DataRegistry(new FilterStack());

    assertEquals(0, registry.size());
    assertTrue(registry.isEmpty());

    for (int i = 0; i < 100; i++) {
      registry.add(newEntry());
    }

    assertEquals(100, registry.size());
    assertFalse(registry.isEmpty());
  }

  @Test
  public void testCounter() {
    DataRegistry registry = new DataRegistry(new FilterStack());

    registry.addCounter("fooBar");

    for (int i = 0; i < 100; i++) {
      registry.add(newEntry());
    }

    assertEquals(2, registry.valuesFor("fooBar").size());

    registry.removeCounter("fooBar");

    assertEquals(0, registry.valuesFor("fooBar").size());

    for (int i = 0; i < 100; i++) {
      registry.add(newEntry());
    }

    registry.addCounter("fooBar");
    assertEquals(0, registry.valuesFor("fooBar").size());
  }

}
