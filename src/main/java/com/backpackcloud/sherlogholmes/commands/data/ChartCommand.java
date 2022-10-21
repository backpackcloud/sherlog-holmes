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

package com.backpackcloud.sherlogholmes.commands.data;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.cli.Action;
import com.backpackcloud.cli.AnnotatedCommand;
import com.backpackcloud.cli.CommandDefinition;
import com.backpackcloud.cli.Suggestions;
import com.backpackcloud.cli.Writer;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.domain.TimeUnit;
import com.backpackcloud.sherlogholmes.domain.chart.Bucket;
import com.backpackcloud.sherlogholmes.domain.chart.Chart;
import com.backpackcloud.sherlogholmes.domain.chart.ChartDataProducer;
import com.backpackcloud.sherlogholmes.ui.suggestions.AttributeSuggester;
import com.backpackcloud.sherlogholmes.ui.suggestions.ChronoUnitSuggestions;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@CommandDefinition(
  name = "chart",
  description = "Creates a csv content for plotting charts",
  type = "Data Visualization",
  allowOutputRedirect = true
)
@RegisterForReflection
public class ChartCommand implements AnnotatedCommand {

  private final AttributeSuggester attributeSuggester;

  private final ChartDataProducer chartDataProducer;

  public ChartCommand(DataRegistry dataRegistry, ChartDataProducer chartDataProducer) {
    this.attributeSuggester = new AttributeSuggester(dataRegistry);
    this.chartDataProducer = chartDataProducer;
  }

  @Action
  public void printData(Writer writer,
                        TimeUnit unit,
                        String attribute) {
    if (attribute == null || attribute.isBlank()) {
      throw new UnbelievableException("No attribute given");
    }

    Chart chart = chartDataProducer.produceData(unit, attribute);

    writer.write("series,").writeln(String.join(",", chart.bucketNames()));

    chart.series().forEach(series -> {
      writer.write(series.name()).write(",");
      writer.writeln(series.buckets().stream()
        .map(Bucket::value)
        .map(String::valueOf)
        .collect(Collectors.joining(",")));
    });
  }

  @Suggestions
  public List<Suggestion> suggestForGroup(String timeUnit, String... fields) {
    if (fields.length == 0) {
      return ChronoUnitSuggestions.suggestUnits();
    } else {
      return attributeSuggester.suggestAttributeNames();
    }
  }

}
