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

package com.backpackcloud.sherlogholmes.config.model;

import com.backpackcloud.configuration.Configuration;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.config.ConfigObject;
import com.backpackcloud.sherlogholmes.model.DataModel;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DataModelConfig implements ConfigObject<DataModel> {

  private final Configuration format;
  private final String modelsToInclude;
  private final Map<String, DataAttributeConfig> attributes;

  @JsonCreator
  public DataModelConfig(@JsonProperty("format") Configuration format,
                         @JsonProperty("include") String modelsToInclude,
                         @JsonProperty("attributes") Map<String, DataAttributeConfig> attributes) {
    this.format = format;
    this.modelsToInclude = modelsToInclude;
    this.attributes = attributes != null ? attributes : new HashMap<>();
  }

  public DataModel get(Config config) {
    DataModel model = new DataModel(format.get());
    attributes.forEach((name, attrConfig) -> {
      model.add(name, attrConfig.get(config));
    });
    if (modelsToInclude != null) {
      Arrays.stream(modelsToInclude.split("\\s*,\\s*"))
        .forEach(additionalModel -> model.addFrom(config.dataModelFor(additionalModel)));
    }
    return model;
  }

}
