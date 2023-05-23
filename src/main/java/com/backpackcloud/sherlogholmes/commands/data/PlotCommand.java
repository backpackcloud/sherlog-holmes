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
import com.backpackcloud.cli.CommandContext;
import com.backpackcloud.cli.CommandDefinition;
import com.backpackcloud.cli.ParameterCount;
import com.backpackcloud.cli.Suggestions;
import com.backpackcloud.cli.preferences.UserPreferences;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.cli.ui.impl.PromptSuggestion;
import com.backpackcloud.serializer.JSON;
import com.backpackcloud.serializer.Serializer;
import com.backpackcloud.sherlogholmes.Preferences;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.domain.FilterStack;
import com.backpackcloud.sherlogholmes.domain.TimeUnit;
import com.backpackcloud.sherlogholmes.domain.chart.ChartDefinition;
import com.backpackcloud.sherlogholmes.domain.chart.ChartProducer;
import com.backpackcloud.sherlogholmes.domain.chart.ChartRegistry;
import com.backpackcloud.sherlogholmes.impl.WebChart;
import com.backpackcloud.sherlogholmes.ui.suggestions.AttributeSuggester;
import com.backpackcloud.sherlogholmes.ui.suggestions.ChronoUnitSuggestions;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
  private final ChartProducer chartDataProducer;
  private final AttributeSuggester attributeSuggester;
  private final UserPreferences preferences;
  private final Serializer serializer;
  private final FilterStack filterStack;
  private final ChartRegistry chartRegistry;
  private final Set<String> configTemplates;

  public PlotCommand(ChartProducer chartDataProducer,
                     DataRegistry registry,
                     UserPreferences preferences,
                     @JSON Serializer serializer,
                     FilterStack filterStack,
                     ChartRegistry chartRegistry, Config config) {
    this.chartDataProducer = chartDataProducer;
    this.attributeSuggester = new AttributeSuggester(registry);
    this.preferences = preferences;
    this.serializer = serializer;
    this.filterStack = filterStack;
    this.chartRegistry = chartRegistry;
    this.configTemplates = new HashSet<>(config.charts().keySet());

    this.preferences.get(Preferences.MAX_SERIES_COUNT).listen(o -> drawCharts());
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
  public void execute(String template, TimeUnit unit, String field, String counter) {
    ChartDefinition def = new ChartDefinition(template, filterStack.filter(), unit, field, counter);
    UUID id = chartRegistry.add(def);
    drawChart(id, def);
  }

  @Suggestions
  public List<Suggestion> execute(@ParameterCount int paramCount) {
    if (paramCount == 1) {
      return configTemplates.stream()
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

  @ConsumeEvent("command.filter")
  public void onFilter(CommandContext context) {
    drawCharts();
  }

  @ConsumeEvent("command.clear")
  public void onClearCharts(CommandContext context) {
    if (chartRegistry.isEmpty()) {
      sessions.forEach(((s, session) -> session.getAsyncRemote().sendObject("clear")));
    }
  }

  private void drawCharts() {
    chartRegistry.forEach(this::drawChart);
  }

  private void drawChart(UUID chartId, ChartDefinition definition) {
    String chart = serializer.serialize(new WebChart(
      chartId.toString(),
      definition.config(),
      chartDataProducer.produce(definition.unit(), definition.filter(), definition.field(), definition.counter()),
      preferences.number(Preferences.MAX_SERIES_COUNT).get()
    ));
    sessions.forEach((sessionId, session) -> session.getAsyncRemote().sendObject(chart));
  }

}
