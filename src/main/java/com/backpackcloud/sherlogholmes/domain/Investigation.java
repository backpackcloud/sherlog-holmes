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

package com.backpackcloud.sherlogholmes.domain;

import java.util.List;
import java.util.function.Consumer;

public class Investigation {

  private final DataModel dataModel;
  private final DataParser<Object> dataParser;
  private final DataMapper<Object> dataMapper;
  private final List<PipelineStep> analysisSteps;
  private final FallbackMode fallbackMode;

  public Investigation(DataModel dataModel,
                       DataParser dataParser,
                       DataMapper dataMapper,
                       List<PipelineStep> analysisSteps,
                       FallbackMode fallbackMode) {
    this.dataModel = dataModel;
    this.dataParser = dataParser;
    this.dataMapper = dataMapper;
    this.analysisSteps = analysisSteps;
    this.fallbackMode = fallbackMode;
  }

  public void analyze(DataReader dataReader, String location, Consumer<DataEntry> consumer) {
    switch (fallbackMode) {
      case IGNORE -> dataReader.read(location, content -> dataParser.parse(content)
        .ifPresent(struct -> dataMapper.map(dataModel.dataSupplier(), struct)
          .stream()
          .peek(entry -> analysisSteps.forEach(step -> step.analyze(entry)))
          .forEach(consumer)));
      case APPEND -> {
        StagingArea stagingArea = new StagingArea(consumer);

        dataReader.read(location, content ->
          dataParser.parse(content)
            .ifPresentOrElse(
              structure -> stagingArea.push(content, structure),
              () -> stagingArea.push(content)
            ));

        stagingArea.push();
      }
    }
  }

  private class StagingArea {

    private StringBuilder content;
    private Object structure;
    private final Consumer<DataEntry> consumer;

    private StagingArea(Consumer<DataEntry> consumer) {
      this.consumer = consumer;
    }

    public void push() {
      if (this.structure != null) {
        dataMapper.map(dataModel.dataSupplier(), this.structure)
          .stream()
          .peek(entry -> analysisSteps.forEach(step -> step.analyze(entry)))
          .forEach(consumer);
        this.structure = null;
        this.content = null;
      }
    }

    public void push(String content) {
      this.structure = null;
      if (this.content == null) {
        this.content = new StringBuilder(content);
      } else {
        this.content.append("\n").append(content);
      }
    }

    public void push(String content, Object structure) {
      if (this.structure == null && this.content != null) {
        dataParser.parse(this.content.toString())
          .ifPresent(struct -> this.structure = struct);
      }
      push();
      this.content = new StringBuilder(content);
      this.structure = structure;
    }

  }

}
