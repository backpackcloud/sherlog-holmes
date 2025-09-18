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

package com.backpackcloud.sherlogholmes.model.parsers;

import com.backpackcloud.io.SerialBitter;
import com.backpackcloud.sherlogholmes.model.DataEntry;
import com.backpackcloud.sherlogholmes.model.DataModel;
import com.backpackcloud.sherlogholmes.model.DataParser;
import com.backpackcloud.sherlogholmes.model.Metadata;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Optional;

public class JsonDataParser implements DataParser {

  private final String name;
  private final DataModel dataModel;
  private final SerialBitter deserializer;
  private final Map<String, String> attributeMappings;

  public JsonDataParser(String name, DataModel dataModel, SerialBitter deserializer, Map<String, String> attributeMappings) {
    this.name = name;
    this.dataModel = dataModel;
    this.deserializer = deserializer;
    this.attributeMappings = attributeMappings;
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public DataModel dataModel() {
    return dataModel;
  }

  @Override
  public Optional<DataEntry> parse(Metadata metadata, String content) {
    if (content == null || content.isBlank()) {
      return Optional.empty();
    }
    JsonNode jsonNode = deserializer.deserialize(content.trim(), JsonNode.class);
    DataEntry entry = dataModel.create();
    metadata.attachTo(entry);
    attributeMappings.forEach((name, path) ->
      entry.attribute(name)
        .ifPresent(attr ->
          attr.assignFromInput(jsonNode.at(path).asText()))
    );
    return Optional.of(entry);
  }

}
