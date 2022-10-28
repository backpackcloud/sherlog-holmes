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

package com.backpackcloud.sherlogholmes.domain.mappers;

import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.DataMapper;

import java.util.Optional;
import java.util.function.Supplier;

public class ColumnDataMapper implements DataMapper<String[]> {

  private final String[] attributeOrder;

  public ColumnDataMapper(String[] attributeOrder) {
    this.attributeOrder = attributeOrder;
  }

  @Override
  public Optional<DataEntry> map(Supplier<DataEntry> dataSupplier, String[] data) {
    if (data.length == attributeOrder.length) {
      DataEntry dataEntry = dataSupplier.get();
      for (int i = 0; i < attributeOrder.length; i++) {
        String attrName = attributeOrder[i];
        String value = data[i];
        dataEntry.attribute(attrName)
          .ifPresent(attr -> attr.assignFromInput(value.strip()));
      }
      return Optional.of(dataEntry);
    }
    return Optional.empty();
  }

}
