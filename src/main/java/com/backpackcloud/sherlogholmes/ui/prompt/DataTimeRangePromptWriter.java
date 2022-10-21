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

package com.backpackcloud.sherlogholmes.ui.prompt;

import com.backpackcloud.cli.ui.Prompt;
import com.backpackcloud.cli.ui.PromptWriter;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;

import javax.enterprise.context.ApplicationScoped;
import java.time.Duration;

@ApplicationScoped
public class DataTimeRangePromptWriter implements PromptWriter {

  private final DataRegistry registry;

  public DataTimeRangePromptWriter(DataRegistry registry) {
    this.registry = registry;
  }

  @Override
  public String name() {
    return "data-range";
  }

  @Override
  public void addTo(Prompt prompt, PromptSide side) {
    if (registry.isEmpty()) {
      return;
    }

    Duration duration = registry.durationOf("timestamp");

    if (!duration.isZero()) {
      Prompt.PromptSegmentBuilder segment = prompt
        .newSegment("data_range")
        .addIcon("nf-fa-calendar");
      long days = duration.toDays();
      if (days > 0) {
        segment.add(days + "d");
      }
      long hours = duration.toHoursPart();
      if (hours > 0) {
        segment.add(hours + "h");
      }
      long minutes = duration.toMinutesPart();
      if (minutes > 0) {
        segment.add(minutes + "m");
      }
      long seconds = duration.toSecondsPart();
      if (seconds > 0) {
        segment.add(seconds + "s");
      }
      long millis = duration.toMillisPart();
      if (millis > 0) {
        segment.add(millis + "ms");
      }
    }
  }

}
