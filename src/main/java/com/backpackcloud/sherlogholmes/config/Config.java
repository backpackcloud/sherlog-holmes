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

package com.backpackcloud.sherlogholmes.config;

import com.backpackcloud.configuration.Configuration;
import com.backpackcloud.cli.preferences.UserPreferences;
import com.backpackcloud.cli.ui.ColorMap;
import com.backpackcloud.cli.ui.IconMap;
import com.backpackcloud.cli.ui.StyleMap;
import com.backpackcloud.cli.ui.Theme;
import com.backpackcloud.sherlogholmes.Preferences;
import com.backpackcloud.sherlogholmes.config.model.DataModelConfig;
import com.backpackcloud.sherlogholmes.config.parser.DataParserConfig;
import com.backpackcloud.sherlogholmes.config.reader.DataReaderConfig;
import com.backpackcloud.sherlogholmes.domain.DataModel;
import com.backpackcloud.sherlogholmes.domain.DataParser;
import com.backpackcloud.sherlogholmes.domain.DataReader;
import com.backpackcloud.sherlogholmes.domain.PipelineStep;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RegisterForReflection
public class Config {
  private final Map<String, String> patterns;
  private final UserPreferences preferences;
  private final List<String> commands;
  private final Map<String, List<String>> macros;
  private final Map<String, DataReaderConfig> readers;
  private final Map<String, DataParserConfig> parsers;
  private final Map<String, DataModelConfig> models;
  private final List<PipelineStep> pipeline;

  @JsonCreator
  public Config(@JacksonInject UserPreferences userPreferences,
                @JacksonInject Theme theme,
                @JsonProperty("preferences") Map<String, Configuration> preferences,
                @JsonProperty("patterns") Map<String, String> patterns,
                @JsonProperty("icons") Map<String, String> icons,
                @JsonProperty("colors") Map<String, String> colors,
                @JsonProperty("styles") Map<String, String> styles,
                @JsonProperty("commands") List<String> commands,
                @JsonProperty("macros") Map<String, List<String>> macros,
                @JsonProperty("readers") Map<String, DataReaderConfig> readers,
                @JsonProperty("parsers") Map<String, DataParserConfig> parsers,
                @JsonProperty("models") Map<String, DataModelConfig> models,
                @JsonProperty("pipeline") List<PipelineStep> pipeline) {
    this.preferences = userPreferences;
    this.commands = commands != null ? commands : Collections.emptyList();
    this.macros = macros != null ? macros : Collections.emptyMap();
    this.readers = readers;
    this.parsers = parsers;
    this.models = models;
    this.pipeline = pipeline != null ? pipeline : Collections.emptyList();
    this.preferences.register(Preferences.values());
    this.patterns = patterns != null ? patterns : Collections.emptyMap();

    if (preferences != null) {
      this.preferences.load(preferences);
    }

    if (icons != null) {
      IconMap iconMap = theme.iconMap();
      icons.forEach(iconMap::put);
    }

    if (colors != null) {
      ColorMap colorMap = theme.colorMap();
      colors.forEach(colorMap::put);
    }

    if (styles != null) {
      StyleMap styleMap = theme.styleMap();
      styles.forEach(styleMap::put);
    }
  }

  public List<String> commands() {
    return commands;
  }

  public Map<String, String> patterns() {
    return patterns;
  }

  public Map<String, List<String>> macros() {
    return macros;
  }

  public Map<String, DataReaderConfig> readers() {
    return readers;
  }

  public Map<String, DataParserConfig> parsers() {
    return parsers;
  }

  public Map<String, DataModelConfig> models() {
    return models;
  }

  public Optional<DataReader> dataReader(String id) {
    if (readers.containsKey(id)) {
      DataReader<?> reader = readers.get(id).get(this);

      return Optional.of((location, dataSupplier, parser, consumer) ->
        reader.read(location, dataSupplier, parser, dataEntry -> {
          pipeline.forEach(step -> step.analyze(dataEntry));
          consumer.accept(dataEntry);
        }));
    }
    return Optional.empty();
  }

  public Optional<DataParser> dataParser(String id) {
    if (parsers.containsKey(id)) {
      return Optional.of(parsers.get(id).get(this));
    }
    return Optional.empty();
  }

  public Optional<DataModel> dataModel(String id) {
    if (models.containsKey(id)) {
      return Optional.of(models.get(id).get(this));
    }
    return Optional.empty();
  }

}
