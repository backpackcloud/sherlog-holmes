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

package com.backpackcloud.sherlogholmes.config;

import com.backpackcloud.sherlogholmes.Preferences;
import com.backpackcloud.sherlogholmes.domain.FallbackMode;
import com.backpackcloud.sherlogholmes.domain.Pipeline;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class PipelineConfig implements ConfigObject<Pipeline> {

  private final String modelId;
  private final String parserId;
  private final String mapperId;
  private final String stepsId;
  private final String fallbackMode;

  @JsonCreator
  public PipelineConfig(@JsonProperty("model") String modelId,
                        @JsonProperty("parser") String parserId,
                        @JsonProperty("mapper") String mapperId,
                        @JsonProperty("steps") String stepsId,
                        @JsonProperty("fallback") String fallbackMode) {
    this.modelId = modelId;
    this.parserId = parserId;
    this.mapperId = mapperId;
    this.stepsId = stepsId;
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

  public String stepsId() {
    return stepsId;
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
      config.stepsFor(stepsId),
      FallbackMode.valueOf(
        (fallbackMode != null ? fallbackMode : config.preferences().text(Preferences.DEFAULT_FALLBACK_MODE).get())
          .toUpperCase()
      ),
      config.preferences()
    );
  }

}
