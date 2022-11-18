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
import com.backpackcloud.cli.ParameterCount;
import com.backpackcloud.cli.PreferenceValue;
import com.backpackcloud.cli.Suggestions;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.cli.ui.impl.PromptSuggestion;
import com.backpackcloud.serializer.JSON;
import com.backpackcloud.serializer.Serializer;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.domain.TimeUnit;
import com.backpackcloud.sherlogholmes.domain.chart.Bucket;
import com.backpackcloud.sherlogholmes.domain.chart.Chart;
import com.backpackcloud.sherlogholmes.domain.chart.ChartDataProducer;
import com.backpackcloud.sherlogholmes.domain.chart.Series;
import com.backpackcloud.sherlogholmes.impl.WebChart;
import com.backpackcloud.sherlogholmes.ui.suggestions.AttributeSuggester;
import com.backpackcloud.sherlogholmes.ui.suggestions.ChronoUnitSuggestions;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.enterprise.context.ApplicationScoped;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
@CommandDefinition(
  name = "timeseries",
  description = "Creates a content for plotting charts",
  type = "Data Visualization",
  allowOutputRedirect = true
)
@RegisterForReflection
public class TimeSeriesCommand implements AnnotatedCommand {

  private final AttributeSuggester attributeSuggester;

  private final ChartDataProducer chartDataProducer;

  private final Serializer serializer;

  public TimeSeriesCommand(DataRegistry dataRegistry,
                           ChartDataProducer chartDataProducer,
                           @JSON Serializer serializer) {
    this.attributeSuggester = new AttributeSuggester(dataRegistry);
    this.chartDataProducer = chartDataProducer;
    this.serializer = serializer;
  }

  @Action
  public String execute(Format format, TimeUnit unit,
                        @PreferenceValue("max-series-count") int maxSeries,
                        String attribute, String counter) {
    if (attribute == null || attribute.isBlank()) {
      throw new UnbelievableException("No attribute given");
    }

    Chart chart = chartDataProducer.produceData(unit, attribute, counter);

    switch (format) {
      case CSV -> {
        StringBuilder builder = new StringBuilder();
        builder.append("series,").append(String.join(",", chart.bucketNames())).append("\n");

        Consumer<Series> printSeries = series -> {
          builder.append(series.name()).append(",")
            .append(series.buckets().stream()
              .map(Bucket::value)
              .map(String::valueOf)
              .collect(Collectors.joining(",")))
            .append("\n");
        };
        chart.series().forEach(printSeries);
        Stream.of(chart.average(), chart.total()).forEach(printSeries);
        return builder.toString();
      }
      case JSON -> {
        return serializer.serialize(new WebChart(chart, maxSeries));
      }
      default -> throw new UnbelievableException("invalid format");
    }
  }

  @Suggestions
  public List<Suggestion> execute(@ParameterCount int paramCount) {
    if (paramCount == 1) {
      return Arrays.stream(Format.values())
        .map(Enum::name)
        .map(String::toLowerCase)
        .map(PromptSuggestion::suggest)
        .collect(Collectors.toList());
    } else if (paramCount == 2) {
      return ChronoUnitSuggestions.suggestUnits();
    } else if (paramCount == 3) {
      return attributeSuggester.suggestIndexedAttributes();
    } else if (paramCount == 4) {
      return attributeSuggester.suggestAttributeNames();
    }
    return Collections.emptyList();
  }

  public enum Format {
    CSV, JSON
  }

}
