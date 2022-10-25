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

package com.backpackcloud.sherlogholmes.domain.parsers;

import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.DataParser;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class FunctionDataParser implements DataParser<Function<String, String>> {

  private final Supplier<DataEntry> dataSupplier;

  private final Map<String, String> attributeMapping;

  public FunctionDataParser(Supplier<DataEntry> dataSupplier, Map<String, String> attributeMapping) {
    this.dataSupplier = dataSupplier;
    this.attributeMapping = attributeMapping;
  }

  @Override
  public Optional<DataEntry> parse(Function<String, String> function) {
    DataEntry entry = dataSupplier.get();

    attributeMapping.forEach((attributeName, functionParameter) ->
      entry.attribute(attributeName).ifPresent(attribute ->
        attribute.assignFromInput(function.apply(functionParameter))));

    return Optional.of(entry);
  }

}
