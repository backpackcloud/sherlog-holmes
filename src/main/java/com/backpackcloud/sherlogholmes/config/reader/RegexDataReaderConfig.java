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

package com.backpackcloud.sherlogholmes.config.reader;

import com.backpackcloud.configuration.Configuration;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.domain.DataReader;
import com.backpackcloud.sherlogholmes.domain.readers.RegexDataReader;
import com.backpackcloud.text.Interpolator;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

@RegisterForReflection
public class RegexDataReaderConfig implements DataReaderConfig {

  private final Configuration patternString;
  private final Configuration removeAnsiColors;
  private final Configuration nonMatchMode;
  private final Configuration charsetName;

  @JsonCreator
  public RegexDataReaderConfig(@JsonProperty("pattern") Configuration patternString,
                               @JsonProperty("remove-ansi-colors") Configuration removeAnsiColors,
                               @JsonProperty("non-match-mode") Configuration nonMatchMode,
                               @JsonProperty("charset") Configuration charsetName) {
    this.patternString = patternString;
    this.removeAnsiColors = removeAnsiColors;
    this.nonMatchMode = nonMatchMode;
    this.charsetName = charsetName;
  }

  @Override
  public DataReader get(Config config) {
    Map<String, String> patterns = config.patterns();

    String result = new Interpolator(
      Interpolator.DOUBLE_BRACKET_INTERPOLATION_PATTERN,
      patterns::get
    ).eval(patternString.get()).orElseThrow();

    Pattern pattern = Pattern.compile(result);
    return new RegexDataReader(
      charsetName.map(Charset::forName).orElse(StandardCharsets.UTF_8),
      pattern,
      nonMatchMode.orElse(RegexDataReader.NonMatchMode.IGNORE),
      removeAnsiColors.asBoolean()
    );
  }

}
