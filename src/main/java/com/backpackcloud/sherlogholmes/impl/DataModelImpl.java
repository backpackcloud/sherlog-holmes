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

import com.backpackcloud.sherlogholmes.domain.AttributeType;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.DataModel;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class DataModelImpl implements DataModel {

  private final Map<String, AttributeType> typeMap = new LinkedHashMap<>();
  private final Set<String> multivaluedAttributes = new HashSet<>();
  private final String format;

  public DataModelImpl() {
    this(null);
  }

  public DataModelImpl(String format) {
    this.format = format;
  }

  @Override
  public DataModel add(String attributeName, AttributeType attributeType) {
    this.typeMap.put(attributeName, attributeType);
    return this;
  }

  @Override
  public DataModel addMultivalued(String attributeName, AttributeType attributeType) {
    multivaluedAttributes.add(attributeName);
    return add(attributeName, attributeType);
  }

  @Override
  public Optional<AttributeType> typeOf(String name) {
    return Optional.ofNullable(this.typeMap.get(name));
  }

  @Override
  public Set<String> attributeNames() {
    return new HashSet<>(typeMap.keySet());
  }

  @Override
  public Supplier<DataEntry> dataSupplier() {
    return () -> {
      DataEntry dataEntry = new DataEntryImpl();
      typeMap.forEach((name, type) -> {
        if (multivaluedAttributes.contains(name)) {
          dataEntry.addAttribute(name).multivalued().ofType(type);
        } else {
          dataEntry.addAttribute(name).ofType(type);
        }
      });
      if (format != null) {
        dataEntry.displayFormat(format);
      }
      return dataEntry;
    };
  }

}
