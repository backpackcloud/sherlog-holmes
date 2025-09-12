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

package com.backpackcloud.sherlogholmes.model;

import com.backpackcloud.sherlogholmes.model.steps.AttributeExtractStep;
import com.backpackcloud.sherlogholmes.model.steps.AttributeRelationStep;
import com.backpackcloud.sherlogholmes.model.steps.AttributeReplaceStep;
import com.backpackcloud.sherlogholmes.model.steps.AttributeSetStep;
import com.backpackcloud.sherlogholmes.model.steps.BasicPipelineStep;
import com.backpackcloud.sherlogholmes.model.steps.RegexMapperStep;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface PipelineStep {

  void analyze(DataEntry dataEntry);

  default PipelineStep andThen(PipelineStep after) {
    return dataEntry -> {
      this.analyze(dataEntry);
      after.analyze(dataEntry);
    };
  }

  PipelineStep NOTHING = dataEntry -> {
  };

  @JsonCreator
  static PipelineStep create(@JsonProperty("when") DataFilter filter,
                             @JsonProperty("assign") AttributeSetStep attributeSetStep,
                             @JsonProperty("extract") AttributeExtractStep attributeExtractStep,
                             @JsonProperty("map") RegexMapperStep regexMapperStep,
                             @JsonProperty("relate") AttributeRelationStep attributeRelationStep,
                             @JsonProperty("replace") AttributeReplaceStep attributeReplaceStep,
                             @JsonProperty("do") List<PipelineStep> nestedSteps,
                             @JsonProperty("else") PipelineStep alternativeStep) {
    PipelineStep steps = Stream.of(
        attributeSetStep,
        attributeExtractStep,
        regexMapperStep,
        attributeRelationStep,
        attributeReplaceStep)
      .filter(Objects::nonNull)
      .reduce(NOTHING, PipelineStep::andThen);

    if (nestedSteps != null) {
      steps = Stream.concat(Stream.of(steps), nestedSteps.stream())
        .reduce(NOTHING, PipelineStep::andThen);
    }

    Predicate<DataEntry> predicate = filter != null ? filter : DataFilter.ALLOW_ALL;

    return new BasicPipelineStep(predicate, steps, alternativeStep != null ? alternativeStep : NOTHING);
  }

}
