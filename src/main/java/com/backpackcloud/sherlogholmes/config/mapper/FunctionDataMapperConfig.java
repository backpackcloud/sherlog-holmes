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

package com.backpackcloud.sherlogholmes.config.mapper;

import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.domain.DataMapper;
import com.backpackcloud.sherlogholmes.domain.mappers.FunctionDataMapper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@RegisterForReflection
public class FunctionDataMapperConfig implements DataMapperConfig {

  private final Map<String, String> attributeMappings;
  private final List<String> attributeCopies;

  @JsonCreator
  public FunctionDataMapperConfig(@JsonProperty("map") Map<String, String> attributeMappings,
                                  @JsonProperty("copy") String attributeCopies) {

    this.attributeMappings = attributeMappings;
    this.attributeCopies = attributeCopies != null ?
      List.of(attributeCopies.split("\\s*,\\s*")) :
      null;
  }


  @Override
  public DataMapper<Function<String, String>> get(Config config) {
    Map<String, String> attributesMap = new HashMap<>();
    if (attributeMappings != null) {
      attributesMap.putAll(attributeMappings);
    }
    if (attributeCopies != null) {
      attributeCopies.forEach(attr -> attributesMap.put(attr, attr));
    }
    return new FunctionDataMapper(attributesMap);
  }

}
