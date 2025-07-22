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

package com.backpackcloud.sherlogholmes.model;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.sherlogholmes.model.types.FallbackType;
import com.backpackcloud.sherlogholmes.model.types.TemporalType;
import com.backpackcloud.sherlogholmes.model.types.UriType;
import com.backpackcloud.sherlogholmes.model.types.UrlType;
import com.backpackcloud.sherlogholmes.model.types.VersionType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQuery;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record AttributeSpec<E>(@JsonProperty AttributeType<E> type,
                               @JsonProperty boolean multivalued) {

  public static AttributeSpec<?> create(String spec) {
    Matcher matcher = Pattern.compile("^(?<type>[0-9a-zA-Z\\-_]+\\*?)(?<multivalued>\\[\\])?\\s*\\|?\\s*(?<config>.+)?$")
      .matcher(spec);

    if (matcher.find()) {
      String type = matcher.group("type");
      String configuration = matcher.group("config");
      boolean multivalued = matcher.group("multivalued") != null;

      return switch (type) {
        case "text" -> new AttributeSpec<>(AttributeType.text(), multivalued);
        case "number" -> new AttributeSpec<>(AttributeType.number(), multivalued);
        case "decimal" -> new AttributeSpec<>(AttributeType.decimal(), multivalued);
        case "enum" -> new AttributeSpec<>(AttributeType.enumOf(configuration.split(",")), multivalued);
        case "time" -> new AttributeSpec<>(
          new TemporalType<>(DateTimeFormatter.ofPattern(configuration), LocalTime::from), multivalued
        );
        case "date" -> new AttributeSpec<>(
          new TemporalType<>(DateTimeFormatter.ofPattern(configuration), LocalDate::from), multivalued
        );
        case "datetime" -> new AttributeSpec<>(
          new TemporalType<>(DateTimeFormatter.ofPattern(configuration), LocalDateTime::from), multivalued
        );
        case "datetime*" -> new AttributeSpec<>(fallbackTemporal(configuration, LocalDateTime::from), multivalued);
        case "zoned-datetime" -> new AttributeSpec<>(
          new TemporalType<>(DateTimeFormatter.ofPattern(configuration), ZonedDateTime::from), multivalued
        );
        case "zoned-datetime*" -> new AttributeSpec<>(fallbackTemporal(configuration, ZonedDateTime::from), multivalued);
        case "offset-datetime" -> new AttributeSpec<>(
          new TemporalType<>(DateTimeFormatter.ofPattern(configuration), OffsetDateTime::from), multivalued
        );
        case "offset-datetime*" -> new AttributeSpec<>(fallbackTemporal(configuration, OffsetDateTime::from), multivalued);
        case "flag" -> new AttributeSpec<>(AttributeType.flag(), multivalued);
        case "version" -> new AttributeSpec<>(new VersionType(), false);
        case "url" -> new AttributeSpec<>(new UrlType(), false);
        case "uri" -> new AttributeSpec<>(new UriType(), false);
        default -> throw new UnbelievableException("Invalid type: " + type);
      };
    }
    throw new UnbelievableException("Illegal spec");
  }

  private static AttributeType fallbackTemporal(String configuration, TemporalQuery query) {
    return new FallbackType<>(
      new TemporalType<>(
        DateTimeFormatter.ofPattern(
          configuration.replace("(", "").replace(")", "")
        ),
        query
      ),
      new TemporalType<>(
        DateTimeFormatter.ofPattern(
          configuration.replaceFirst("^.*\\(", "")
            .replaceFirst("\\).*$", "")
        ),
        LocalTime::from
      )
    );
  }

}
