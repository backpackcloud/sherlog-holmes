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

package com.backpackcloud.sherlogholmes.config.model;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.sherlogholmes.config.ConfigObject;
import com.backpackcloud.sherlogholmes.domain.AttributeSpec;
import com.backpackcloud.sherlogholmes.domain.AttributeType;
import com.backpackcloud.sherlogholmes.domain.types.SemanticVersionType;
import com.backpackcloud.sherlogholmes.domain.types.TemporalType;
import com.backpackcloud.sherlogholmes.impl.AttributeSpecImpl;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RegisterForReflection
public interface DataAttributeConfig extends ConfigObject<AttributeSpec> {

  @JsonCreator
  static DataAttributeConfig create(String spec) {
    Matcher matcher = Pattern.compile("^(?<type>[0-9a-zA-Z\\-_]+)(?<multivalued>\\[\\])?:?(?<config>.+)?$")
      .matcher(spec);

    if (matcher.find()) {
      String type = matcher.group("type");
      String configuration = matcher.group("config");
      boolean multivalued = matcher.group("multivalued") != null;

      return (config) -> switch (type) {
        case "text" -> new AttributeSpecImpl(AttributeType.text(), multivalued);
        case "number" -> new AttributeSpecImpl(AttributeType.number(), multivalued);
        case "decimal" -> new AttributeSpecImpl(AttributeType.decimal(), multivalued);
        case "enum" -> new AttributeSpecImpl(AttributeType.enumOf(configuration.split(",")), multivalued);
        case "time" -> new AttributeSpecImpl(
          new TemporalType(DateTimeFormatter.ofPattern(configuration), LocalTime::from), multivalued
        );
        case "date" -> new AttributeSpecImpl(
          new TemporalType(DateTimeFormatter.ofPattern(configuration), LocalDate::from), multivalued
        );
        case "datetime" -> new AttributeSpecImpl(
          new TemporalType(DateTimeFormatter.ofPattern(configuration), LocalDateTime::from), multivalued
        );
        case "zoned-datetime" -> new AttributeSpecImpl(
          new TemporalType(DateTimeFormatter.ofPattern(configuration), ZonedDateTime::from), multivalued
        );
        case "offset-datetime" -> new AttributeSpecImpl(
          new TemporalType(DateTimeFormatter.ofPattern(configuration), OffsetDateTime::from), multivalued
        );
        case "flag" -> new AttributeSpecImpl(AttributeType.flag(), multivalued);
        case "version" -> new AttributeSpecImpl(new SemanticVersionType(), false);
        default -> throw new UnbelievableException("Invalid type: " + type);
      };
    }
    throw new UnbelievableException("Illegal spec");
  }

}
