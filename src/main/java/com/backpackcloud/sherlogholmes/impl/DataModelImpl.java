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

package com.backpackcloud.sherlogholmes.impl;

import com.backpackcloud.sherlogholmes.domain.AttributeSpec;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.DataModel;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class DataModelImpl implements DataModel {

  private final Map<String, AttributeSpec<?>> attributes = new LinkedHashMap<>();
  private final String format;

  public DataModelImpl() {
    this(null);
  }

  public DataModelImpl(String format) {
    this.format = format;
  }

  @Override
  public DataModel add(String name, AttributeSpec<?> spec) {
    this.attributes.put(name, spec);
    return this;
  }

  @Override
  public Optional<AttributeSpec<?>> attribute(String name) {
    return Optional.ofNullable(attributes.get(name));
  }

  @Override
  public Map<String, AttributeSpec<?>> attributes() {
    return new HashMap<>(attributes);
  }

  public void addFrom(DataModel base) {
    base.attributes().forEach((name, spec) -> {
      if (!attributes.containsKey(name)) {
        add(name, spec);
      }
    });
  }

  @Override
  public Supplier<DataEntry> dataSupplier() {
    return () -> {
      DataEntry dataEntry = new DataEntryImpl();
      attributes.forEach(dataEntry::addAttribute);
      if (format != null) {
        dataEntry.displayFormat(format);
      }
      return dataEntry;
    };
  }

}
