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


import com.backpackcloud.configuration.Configuration;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.domain.AttributeType;
import com.backpackcloud.sherlogholmes.domain.types.TemporalType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@RegisterForReflection
public class OffsetDateTimeAttributeTypeConfig implements DataAttributeConfig {

  private final Configuration indexable;
  private final Configuration format;
  private final boolean multivalued;

  @JsonCreator
  public OffsetDateTimeAttributeTypeConfig(@JsonProperty("indexable") Configuration indexable,
                                           @JsonProperty("multivalued") Boolean multivalued,
                                           @JsonProperty("format") Configuration format) {
    this.indexable = indexable;
    this.multivalued = multivalued != null ? multivalued : false;
    this.format = format;
  }

  @Override
  public boolean multivalued() {
    return multivalued;
  }

  @Override
  public AttributeType get(Config config) {
    return new TemporalType<>(
      format == null ?
        DateTimeFormatter.ISO_OFFSET_DATE_TIME :
        DateTimeFormatter.ofPattern(format.get()),
      OffsetDateTime::from
    );
  }

  @Override
  public boolean indexable() {
    return indexable.asBoolean();
  }

}
