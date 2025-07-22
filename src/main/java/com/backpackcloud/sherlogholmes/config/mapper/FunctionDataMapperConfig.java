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

package com.backpackcloud.sherlogholmes.config.mapper;

import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.model.DataMapper;
import com.backpackcloud.sherlogholmes.model.mappers.FunctionDataMapper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public record FunctionDataMapperConfig(Map<String, String> attributesToMap,
                                       List<String> attributesToCopy) implements DataMapperConfig {

  @JsonCreator
  public FunctionDataMapperConfig(@JsonProperty("map") Map<String, String> attributeMappings,
                                  @JsonProperty("copy") String attributeCopies) {

    this(
      attributeMappings != null ? new HashMap<>(attributeMappings) : Collections.emptyMap(),
      attributeCopies != null ? List.of(attributeCopies.split("\\s*,\\s*")) : Collections.emptyList()
    );
  }

  @Override
  public DataMapper<Function<String, String>> get(Config config) {
    Map<String, String> attributesMap = new HashMap<>(attributesToMap);
    attributesToCopy.forEach(attr -> attributesMap.put(attr, attr));
    return new FunctionDataMapper(attributesMap);
  }

  public static FunctionDataMapperConfig from(String spec) {
    List<String> attributesToCopy = new ArrayList<>();
    Map<String, String> attributesToMap = new HashMap<>();
    if (spec != null) {
      String[] split = spec.trim().split(",");
      for (String attr : split) {
        if (attr.contains("=>")) {
          String[] map = attr.split("=>", 2);
          attributesToMap.put(map[0], map[1]);
        } else {
          attributesToCopy.add(attr);
        }
      }
    }
    return new FunctionDataMapperConfig(attributesToMap, attributesToCopy);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    attributesToMap.forEach((key, value) -> builder.append(key).append("=>").append(value).append(" "));
    attributesToCopy.forEach(attr -> builder.append(attr).append(" "));
    return builder.toString().trim();
  }

}
