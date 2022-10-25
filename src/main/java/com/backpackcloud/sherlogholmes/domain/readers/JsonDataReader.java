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

package com.backpackcloud.sherlogholmes.domain.readers;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.serializer.Serializer;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.DataParser;
import com.backpackcloud.sherlogholmes.domain.DataReader;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class JsonDataReader implements DataReader<Function<String, String>> {

  private final Charset inputCharset;
  private final Serializer serializer;

  public JsonDataReader(Charset inputCharset, Serializer serializer) {
    this.inputCharset = inputCharset;
    this.serializer = serializer;
  }

  @Override
  public void read(String location, Supplier<DataEntry> dataSupplier, DataParser<Function<String, String>> parser, Consumer<DataEntry> consumer) {
    try {
      Files.lines(Path.of(location), inputCharset).forEach(line -> {
        JsonNode jsonNode = serializer.deserialize(line.trim(), JsonNode.class);
        parser.parse(dataSupplier, pointer -> jsonNode.at(pointer).asText()).ifPresent(consumer);
      });
    } catch (IOException e) {
      throw new UnbelievableException(e);
    }
  }

}
