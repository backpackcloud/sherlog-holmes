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

package com.backpackcloud.sherlogholmes.config.parser;

import com.backpackcloud.configuration.Configuration;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.model.DataParser;
import com.backpackcloud.sherlogholmes.model.parsers.SplitDataParser;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplitDataParserConfig implements DataParserConfig {

  private static final Pattern PATTERN = Pattern.compile("\\{\\{(?<key>[^}]+)}}");

  private final Configuration patternString;
  private final Configuration limit;

  @JsonCreator
  public SplitDataParserConfig(@JsonProperty("pattern") Configuration patternString,
                               @JsonProperty("limit") Configuration limit) {
    this.patternString = patternString;
    this.limit = limit;
  }

  @Override
  public DataParser<String[]> get(Config config) {
    Map<String, String> patterns = config.patterns();

    Matcher matcher = PATTERN.matcher(patternString.get());

    String result = matcher.replaceAll(matchResult -> {
      String key = matchResult.group("key");
      return patterns.get(key);
    });

    return new SplitDataParser(Pattern.compile(result, Pattern.DOTALL), limit.asInteger().orElse(0));
  }

}
