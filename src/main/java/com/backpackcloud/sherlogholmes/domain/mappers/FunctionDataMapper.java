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
import com.backpackcloud.sherlogholmes.domain.DataModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class FunctionDataMapper implements DataMapper<Function<String, String>> {

  private final Map<String, String> attributeMapping;

  public FunctionDataMapper(Map<String, String> attributeMapping) {
    this.attributeMapping = attributeMapping;
  }

  @Override
  public Optional<DataEntry> map(Supplier<DataEntry> dataSupplier, Function<String, String> function) {
    DataEntry entry = dataSupplier.get();

    attributeMapping.forEach((attributeName, functionParameter) ->
      entry.attribute(attributeName).ifPresent(attribute ->
        attribute.assignFromInput(function.apply(functionParameter))));

    return Optional.of(entry);
  }

  public static FunctionDataMapper attributesFrom(DataModel dataModel) {
    Map<String, String> map = new HashMap<>();
    dataModel.attributeNames().forEach(attr -> map.put(attr, attr));
    return new FunctionDataMapper(map);
  }

}
