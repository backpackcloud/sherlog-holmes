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

package com.backpackcloud.sherlogholmes.domain.steps;

import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.PipelineStep;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;
import java.util.stream.Collectors;

@RegisterForReflection
public class AttributeExtractStep implements PipelineStep {

  private final String sourceAttribute;
  private final String targetAttribute;
  private final List<String> texts;

  @JsonCreator
  public AttributeExtractStep(@JsonProperty("from") String sourceAttribute,
                              @JsonProperty("to") String targetAttribute,
                              @JsonProperty("any") List<String> texts) {

    this.sourceAttribute = sourceAttribute;
    this.targetAttribute = targetAttribute;
    this.texts = texts.stream()
      // finds the most lengthy text that is present in the entry
      .sorted((o1, o2) -> o2.length() - o1.length())
      .collect(Collectors.toList());
  }

  @Override
  public void analyze(DataEntry dataEntry) {
    dataEntry.attribute(sourceAttribute)
      .ifPresent(source ->
        source.formattedValue()
          .ifPresent(value -> texts.stream()
            .filter(value::contains)
            .findFirst()
            .ifPresent(found ->
              dataEntry.attribute(targetAttribute)
                .ifPresent(target ->
                  target.assignFromInput(found)))));
  }

}