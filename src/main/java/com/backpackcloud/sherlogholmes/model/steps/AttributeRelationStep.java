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

package com.backpackcloud.sherlogholmes.model.steps;

import com.backpackcloud.sherlogholmes.model.Attribute;
import com.backpackcloud.sherlogholmes.model.DataEntry;
import com.backpackcloud.sherlogholmes.model.PipelineStep;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class AttributeRelationStep implements PipelineStep {

  private final String sourceAttribute;
  private final String targetAttribute;
  private final Map<String, String> assignMap;

  public AttributeRelationStep(@JsonProperty("source") String sourceAttribute,
                               @JsonProperty("target") String targetAttribute,
                               @JsonProperty("relation") Map<String, String> assignMap) {
    this.sourceAttribute = sourceAttribute;
    this.targetAttribute = targetAttribute;
    this.assignMap = assignMap;
  }

  @Override
  public void analyze(DataEntry dataEntry) {
    dataEntry.attribute(sourceAttribute)
      .flatMap(Attribute::formattedValue)
      .filter(assignMap::containsKey)
      .map(assignMap::get)
      .ifPresent(value -> dataEntry.attribute(targetAttribute)
        .ifPresent(target -> target.assignFromInput(value)));
  }

}
