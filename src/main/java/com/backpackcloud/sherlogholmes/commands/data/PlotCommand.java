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
import com.backpackcloud.cli.ParameterCount;
import com.backpackcloud.cli.Suggestions;
import com.backpackcloud.cli.preferences.UserPreferences;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.serializer.Serializer;
import com.backpackcloud.sherlogholmes.Preferences;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.domain.TimeUnit;
import com.backpackcloud.sherlogholmes.domain.chart.ChartDataProducer;
import com.backpackcloud.sherlogholmes.impl.WebChart;
import com.backpackcloud.sherlogholmes.ui.suggestions.AttributeSuggester;
import com.backpackcloud.sherlogholmes.ui.suggestions.ChronoUnitSuggestions;
import io.quarkus.runtime.annotations.RegisterForReflection;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@CommandDefinition(
  name = "plot",
  description = "Plots a chart in the web interface"
)
@RegisterForReflection
@ApplicationScoped
@ServerEndpoint("/data")
public class PlotCommand implements AnnotatedCommand {

  private final Map<String, Session> sessions = new ConcurrentHashMap<>();

  private final ChartDataProducer chartDataProducer;

  private final AttributeSuggester attributeSuggester;
  private final UserPreferences preferences;

  private final Serializer serializer;

  private TimeUnit lastTimeUnit;
  private String lastField;
  private String lastCounter;
  private String lastChart;

  public PlotCommand(ChartDataProducer chartDataProducer,
                     DataRegistry registry,
                     UserPreferences preferences) {
    this.chartDataProducer = chartDataProducer;
    this.attributeSuggester = new AttributeSuggester(registry);
    this.preferences = preferences;
    this.serializer = Serializer.json();
    this.preferences.get(Preferences.MAX_SERIES_COUNT).listen(o -> redrawChart());
  }

  @OnOpen
  public void register(Session session) {
    sessions.put(session.getId(), session);
    if (lastChart != null) {
      session.getAsyncRemote().sendObject(lastChart);
    }
  }

  @OnClose
  public void deregister(Session session) {
    sessions.remove(session.getId());
  }

  @Action
  public void execute(TimeUnit unit, String field, String counter) {
    if (unit == null && field == null) {
      redrawChart();
    } else {
      this.lastTimeUnit = unit;
      this.lastField = field;
      this.lastCounter = counter;

      redrawChart();
    }
  }

  @Suggestions
  public List<Suggestion> execute(@ParameterCount int paramCount) {
    if (paramCount == 1) {
      return ChronoUnitSuggestions.suggestUnits();
    } else if(paramCount == 2) {
      return attributeSuggester.suggestIndexedAttributes();
    } else if (paramCount == 3) {
      return attributeSuggester.suggestAttributeNames();
    }
    return Collections.emptyList();
  }

  public void redrawChart() {
    if (lastTimeUnit != null && lastField != null) {
      lastChart = serializer.serialize(
        new WebChart(
          lastField + " count",
          chartDataProducer.produceData(lastTimeUnit, lastField, lastCounter),
          preferences.number(Preferences.MAX_SERIES_COUNT).get()
        )
      );
      updateSessions();
    }
  }

  public void updateSessions() {
    sessions.forEach((id, session) -> session.getAsyncRemote().sendObject(lastChart));
  }

}
