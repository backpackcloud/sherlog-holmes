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

package com.backpackcloud.sherlogholmes.config;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.cli.Macro;
import com.backpackcloud.cli.ui.ColorMap;
import com.backpackcloud.cli.ui.IconMap;
import com.backpackcloud.cli.ui.StyleMap;
import com.backpackcloud.cli.ui.Theme;
import com.backpackcloud.configuration.Configuration;
import com.backpackcloud.preferences.UserPreferences;
import com.backpackcloud.sherlogholmes.Preferences;
import com.backpackcloud.sherlogholmes.config.mapper.DataMapperConfig;
import com.backpackcloud.sherlogholmes.config.model.DataModelConfig;
import com.backpackcloud.sherlogholmes.config.parser.DataParserConfig;
import com.backpackcloud.sherlogholmes.model.DataMapper;
import com.backpackcloud.sherlogholmes.model.DataModel;
import com.backpackcloud.sherlogholmes.model.DataParser;
import com.backpackcloud.sherlogholmes.model.DataRegistry;
import com.backpackcloud.sherlogholmes.model.FallbackMode;
import com.backpackcloud.sherlogholmes.model.Pipeline;
import com.backpackcloud.sherlogholmes.model.PipelineStep;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

  private final Map<String, String> patterns;
  private final UserPreferences userPreferences;
  private final List<String> commands;
  private final Map<String, DataModelConfig> models;
  private final Map<String, DataParserConfig> parsers;
  private final Map<String, DataMapperConfig> mappers;
  private final Map<String, List<PipelineStep>> steps;
  private final Map<String, PipelineConfig> pipelines;
  private final List<Macro> macros;

  @JsonCreator
  public Config(@JacksonInject UserPreferences userPreferences,
                @JacksonInject Theme theme,
                @JacksonInject DataRegistry registry,
                @JsonProperty("preferences") Map<String, Configuration> preferences,
                @JsonProperty("patterns") Map<String, String> patterns,
                @JsonProperty("icons") Map<String, String> icons,
                @JsonProperty("colors") Map<String, String> colors,
                @JsonProperty("styles") Map<String, String> styles,
                @JsonProperty("commands") List<String> commands,
                @JsonProperty("macros") List<Macro> macros,
                @JsonProperty("models") Map<String, DataModelConfig> models,
                @JsonProperty("parsers") Map<String, DataParserConfig> parsers,
                @JsonProperty("mappers") Map<String, DataMapperConfig> mappers,
                @JsonProperty("steps") Map<String, List<PipelineStep>> steps,
                @JsonProperty("pipelines") Map<String, PipelineConfig> pipelines,
                @JsonProperty("index") List<String> index) {
    this(userPreferences, patterns, commands, macros, models, parsers, mappers, steps, pipelines);

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

    if (index != null) {
      index.forEach(registry::addIndex);
    }

    userPreferences.register(Preferences.class);

    if (preferences != null) {
      preferences.forEach((id, configuration) ->
        userPreferences
          .find(id)
          .ifPresent(preference -> preference.set(configuration.read())));
    }

    if (models != null) {
      models.forEach((id, map) -> {
        if (this.parsers.containsKey(id) && this.mappers.containsKey(id) && !this.pipelines.containsKey(id)) {
          this.pipelines.put(id, new PipelineConfig(id, id, id, new String[]{id}, FallbackMode.USER_DEFAULT));
        }
      });
    }
  }

  private Config(UserPreferences userPreferences,
                 Map<String, String> patterns,
                 List<String> commands,
                 List<Macro> macros,
                 Map<String, DataModelConfig> models,
                 Map<String, DataParserConfig> parsers,
                 Map<String, DataMapperConfig> mappers,
                 Map<String, List<PipelineStep>> steps,
                 Map<String, PipelineConfig> pipelines) {
    this.userPreferences = userPreferences;
    this.commands = commands != null ? commands : new ArrayList<>();
    this.macros = macros != null ? macros : new ArrayList<>();
    this.models = models != null ? models : new HashMap<>();
    this.parsers = parsers != null ? parsers : new HashMap<>();
    this.mappers = mappers != null ? mappers : new HashMap<>();
    this.steps = steps != null ? steps : new HashMap<>();
    this.pipelines = pipelines != null ? pipelines : new HashMap<>();
    this.patterns = patterns == null ? new HashMap<>() : patterns;
  }

  public List<String> commands() {
    return commands;
  }

  public Map<String, String> patterns() {
    return patterns;
  }

  private <E, T extends ConfigObject<E>> E getObject(Map<String, T> map, String key) {
    if (map.containsKey(key)) {
      return map.get(key).get(this);
    }
    throw new UnbelievableException("No " + key + " present");
  }

  public DataModel dataModelFor(String id) {
    return getObject(models, id);
  }

  public DataModelConfig modelConfig(String name) {
    return models.get(name);
  }

  public UserPreferences userPreferences() {
    return userPreferences;
  }

  public DataParser dataParserFor(String id) {
    return getObject(parsers, id);
  }

  public DataMapper dataMapperFor(String id) {
    return getObject(mappers, id);
  }

  public List<PipelineStep> stepsFor(String id) {
    return steps.getOrDefault(id, Collections.emptyList());
  }

  public Pipeline pipelineFor(String id) {
    return getObject(pipelines, id);
  }

  public Map<String, DataModelConfig> models() {
    return models;
  }

  public Map<String, DataParserConfig> parsers() {
    return parsers;
  }

  public Map<String, DataMapperConfig> mappers() {
    return mappers;
  }

  public Map<String, List<PipelineStep>> steps() {
    return steps;
  }

  public Map<String, PipelineConfig> pipelines() {
    return pipelines;
  }

  public List<Macro> macros() {
    return macros;
  }

  /**
   * Combines this configuration with the given one. The given configuration takes
   * precedence.
   *
   * @param other the configuration to combine
   */
  public void mergeWith(Config other) {
    this.patterns.putAll(other.patterns);
    this.commands.addAll(other.commands);
    this.macros.addAll(other.macros);
    this.models.putAll(other.models);
    this.parsers.putAll(other.parsers);
    this.mappers.putAll(other.mappers);
    this.steps.putAll(other.steps);
    this.pipelines.putAll(other.pipelines);
  }

}
