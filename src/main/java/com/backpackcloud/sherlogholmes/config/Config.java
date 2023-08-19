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

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.cli.preferences.UserPreferences;
import com.backpackcloud.cli.ui.ColorMap;
import com.backpackcloud.cli.ui.IconMap;
import com.backpackcloud.cli.ui.StyleMap;
import com.backpackcloud.cli.ui.Theme;
import com.backpackcloud.configuration.Configuration;
import com.backpackcloud.sherlogholmes.Preferences;
import com.backpackcloud.sherlogholmes.config.exporter.DataExporterConfig;
import com.backpackcloud.sherlogholmes.config.mapper.DataMapperConfig;
import com.backpackcloud.sherlogholmes.config.model.DataModelConfig;
import com.backpackcloud.sherlogholmes.config.parser.DataParserConfig;
import com.backpackcloud.sherlogholmes.config.reader.DataReaderConfig;
import com.backpackcloud.sherlogholmes.domain.DataExporter;
import com.backpackcloud.sherlogholmes.domain.DataMapper;
import com.backpackcloud.sherlogholmes.domain.DataModel;
import com.backpackcloud.sherlogholmes.domain.DataParser;
import com.backpackcloud.sherlogholmes.domain.DataReader;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.domain.Pipeline;
import com.backpackcloud.sherlogholmes.domain.PipelineStep;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RegisterForReflection
public class Config {
  private final Map<String, String> patterns;
  private final UserPreferences preferences;
  private final List<String> commands;
  private final Map<String, List<String>> macros;
  private final Map<String, DataModelConfig> models;
  private final Map<String, DataReaderConfig> readers;
  private final Map<String, DataParserConfig> parsers;
  private final Map<String, DataMapperConfig> mappers;
  private final Map<String, List<PipelineStep>> steps;
  private final Map<String, PipelineConfig> pipelines;
  private final Map<String, DataExporterConfig> exporters;

  private final Map<String, Map> charts;

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
                @JsonProperty("macros") Map<String, List<String>> macros,
                @JsonProperty("models") Map<String, DataModelConfig> models,
                @JsonProperty("readers") Map<String, DataReaderConfig> readers,
                @JsonProperty("parsers") Map<String, DataParserConfig> parsers,
                @JsonProperty("mappers") Map<String, DataMapperConfig> mappers,
                @JsonProperty("steps") Map<String, List<PipelineStep>> steps,
                @JsonProperty("pipelines") Map<String, PipelineConfig> pipelines,
                @JsonProperty("exporters") Map<String, DataExporterConfig> exporters,
                @JsonProperty("charts") Map<String, Map> charts,
                @JsonProperty("index") List<String> index) {
    this.preferences = userPreferences;
    this.commands = commands != null ? commands : Collections.emptyList();
    this.macros = macros != null ? macros : Collections.emptyMap();
    this.models = models;
    this.readers = readers;
    this.parsers = parsers;
    this.mappers = mappers;
    this.steps = steps != null ? steps : Collections.emptyMap();
    this.pipelines = pipelines != null ? pipelines : new HashMap<>();
    this.exporters = exporters;
    this.charts = charts != null ? charts : Collections.emptyMap();

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

    models.forEach((id, map) -> {
      if (this.parsers.containsKey(id) && this.mappers.containsKey(id)) {
        this.pipelines.put(id, new PipelineConfig(id, id, id, id, null));
      }
    });

    if(index != null) {
      index.forEach(registry::addIndex);
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

  private <E, T extends ConfigObject<E>> E getObject(Map<String, T> map, String key) {
    if (map.containsKey(key)) {
      return map.get(key).get(this);
    }
    throw new UnbelievableException("No " + key + " present");
  }

  public DataModel dataModelFor(String id) {
    return getObject(models, id);
  }

  public DataReader dataReaderFor(String id) {
    return getObject(readers, id);
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

  public DataExporter dataExporterFor(String id) {
    return getObject(exporters, id);
  }

  public Map<String, DataReaderConfig> readers() {
    return readers;
  }

  public Map<String, PipelineConfig> pipelines() {
    return pipelines;
  }

  public Map<String, DataExporterConfig> exporters() {
    return exporters;
  }

  public UserPreferences preferences() {
    return preferences;
  }

  public Map<String, Map> charts() {
    return charts;
  }

}
