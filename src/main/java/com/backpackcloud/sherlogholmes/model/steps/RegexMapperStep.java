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

package com.backpackcloud.sherlogholmes.model.steps;

import com.backpackcloud.sherlogholmes.model.Attribute;
import com.backpackcloud.sherlogholmes.model.DataEntry;
import com.backpackcloud.sherlogholmes.model.PipelineStep;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMapperStep implements PipelineStep {

  private final String sourceAttribute;
  private final Pattern pattern;
  private final Set<String> namedGroups;

  public RegexMapperStep(String sourceAttribute, Pattern pattern) {
    this.sourceAttribute = sourceAttribute;
    this.pattern = pattern;
    this.namedGroups = new RegexAnalyzer(this.pattern).namedGroups();
  }

  @Override
  public void analyze(DataEntry dataEntry) {
    Matcher matcher = dataEntry.attribute(sourceAttribute)
      .flatMap(Attribute::formattedValue)
      .map(pattern::matcher)
      .orElse(null);

    if (matcher != null) {
      while (matcher.find()) {
        namedGroups.forEach(name ->
          dataEntry.attribute(name).ifPresent(attr ->
            attr.assignFromInput(matcher.group(name))));
      }
    }
  }

  @JsonCreator
  public static RegexMapperStep create(@JsonProperty("from") String sourceAttribute,
                                       @JsonProperty("regex") String regex) {
    return new RegexMapperStep(sourceAttribute, Pattern.compile(regex));
  }

}
