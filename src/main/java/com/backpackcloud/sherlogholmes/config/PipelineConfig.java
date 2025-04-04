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

package com.backpackcloud.sherlogholmes.config;

import com.backpackcloud.preferences.UserPreferences;
import com.backpackcloud.sherlogholmes.Preferences;
import com.backpackcloud.sherlogholmes.model.FallbackMode;
import com.backpackcloud.sherlogholmes.model.Pipeline;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class PipelineConfig implements ConfigObject<Pipeline> {


  private final UserPreferences preferences;
  private final String modelId;
  private final String parserId;
  private final String mapperId;
  private final String[] stepsIds;
  private final String fallbackMode;

  @JsonCreator
  public PipelineConfig(@JacksonInject UserPreferences preferences,
                        @JsonProperty("model") String modelId,
                        @JsonProperty("parser") String parserId,
                        @JsonProperty("mapper") String mapperId,
                        @JsonProperty("steps") String stepsIds,
                        @JsonProperty("fallback") String fallbackMode) {
    this.preferences = preferences;
    this.modelId = modelId;
    this.parserId = parserId;
    this.mapperId = mapperId;
    this.stepsIds = stepsIds.split("\\s*,\\s*");
    this.fallbackMode = fallbackMode;
  }

  public String modelId() {
    return modelId;
  }

  public String parserId() {
    return parserId;
  }

  public String mapperId() {
    return mapperId;
  }

  public String fallbackMode() {
    return fallbackMode;
  }

  @Override
  public Pipeline get(Config config) {
    return new Pipeline(
      config.dataModelFor(modelId),
      config.dataParserFor(parserId),
      config.dataMapperFor(mapperId),
      Arrays.stream(stepsIds)
        .map(config::stepsFor)
        .flatMap(List::stream)
        .collect(Collectors.toList()),
      FallbackMode.valueOf(
        (fallbackMode != null ? fallbackMode : preferences.get(Preferences.DEFAULT_FALLBACK_MODE).value())
          .toUpperCase()
      ),
      preferences
    );
  }

}
