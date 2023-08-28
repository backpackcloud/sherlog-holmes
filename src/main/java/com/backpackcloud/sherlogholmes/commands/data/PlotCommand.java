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

import com.backpackcloud.cli.Action;
import com.backpackcloud.cli.AnnotatedCommand;
import com.backpackcloud.cli.CommandDefinition;
import com.backpackcloud.cli.Suggestions;
import com.backpackcloud.cli.preferences.UserPreferences;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.cli.ui.impl.PromptSuggestion;
import com.backpackcloud.serializer.JSON;
import com.backpackcloud.serializer.Serializer;
import com.backpackcloud.sherlogholmes.Preferences;
import com.backpackcloud.sherlogholmes.domain.AttributeType;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.domain.FilterStack;
import com.backpackcloud.sherlogholmes.domain.TimeUnit;
import com.backpackcloud.sherlogholmes.domain.chart.Chart;
import com.backpackcloud.sherlogholmes.domain.chart.ChartBuilder;
import com.backpackcloud.sherlogholmes.impl.WebChart;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandDefinition(
  name = "plot",
  description = "Plots a chart in the web interface",
  allowOutputRedirect = true
)
@RegisterForReflection
@ApplicationScoped
@ServerEndpoint("/data")
public class PlotCommand implements AnnotatedCommand {

  private final Map<String, Session> sessions = new ConcurrentHashMap<>();

  private final DataRegistry registry;
  private final UserPreferences preferences;
  private final Serializer serializer;
  private final FilterStack filterStack;

  public PlotCommand(DataRegistry registry,
                     UserPreferences preferences,
                     @JSON Serializer serializer,
                     FilterStack filterStack) {
    this.registry = registry;
    this.preferences = preferences;
    this.serializer = serializer;
    this.filterStack = filterStack;
  }

  @OnOpen
  public void register(Session session) {
    sessions.put(session.getId(), session);
  }

  @OnClose
  public void deregister(Session session) {
    sessions.remove(session.getId());
  }

  @Action
  public void execute(String label, String series, String reduction, String name) {
    ChartBuilder builder = ChartBuilder.create(label, series, reduction);

    Chart chart = builder.build(registry.entries(filterStack.filter()).stream());

    String serializedChart = serializer.serialize(new WebChart(
      name,
      chart,
      preferences.number(Preferences.MAX_SERIES_COUNT).get()
    ));

    sessions.forEach((sessionId, session) -> session.getAsyncRemote().sendObject(serializedChart));
  }

  @Suggestions
  public List<Suggestion> suggest(String label, String series, String reduction, String name) {
    if (series == null) {
      return Stream.concat(
          Arrays.stream(TimeUnit.values())
            .map(Enum::name)
            .map(String::toLowerCase)
            .map(unit -> String.format("temporal.%s", unit)),
          Stream.of("category")
        )
        .map(PromptSuggestion::suggest)
        .collect(Collectors.toList());
    } else if (reduction == null) {
      String prefix;
      if (series.contains(":")) {
        prefix = series.substring(0, series.lastIndexOf(":") + 1);
      } else if (series.contains(",")) {
        prefix = series.substring(0, series.indexOf(",") + 1);
      } else {
        prefix = "";
      }
      return registry.attributeNames().stream()
        .map(attr -> prefix + attr)
        .map(PromptSuggestion::suggest)
        .map(PromptSuggestion::incomplete)
        .collect(Collectors.toList());
    } else if (name == null) {
      return Stream.concat(
          Stream.concat(
            Stream.concat(
              registry.attributeNames().stream()
                .filter(attr -> registry.typeOf(attr)
                  .filter(type -> type.equals(AttributeType.NUMBER)).isPresent())
                .map(attr -> String.format("count:%s", attr)),
              registry.attributeNames().stream()
                .filter(attr -> registry.typeOf(attr)
                  .filter(type -> type.equals(AttributeType.DECIMAL)).isPresent())
                .map(attr -> String.format("sum:%s", attr))),
            registry.attributeNames().stream()
              .map(attr -> String.format("unique:%s", attr))),
          Stream.of("count")
        )
        .map(PromptSuggestion::suggest)
        .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }

}
