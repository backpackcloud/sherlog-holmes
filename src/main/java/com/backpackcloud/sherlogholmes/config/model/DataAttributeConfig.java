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

import com.backpackcloud.sherlogholmes.config.ConfigObject;
import com.backpackcloud.sherlogholmes.domain.AttributeType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.quarkus.runtime.annotations.RegisterForReflection;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(name = "text", value = TextAttributeTypeConfig.class),
  @JsonSubTypes.Type(name = "number", value = NumberAttributeTypeConfig.class),
  @JsonSubTypes.Type(name = "decimal", value = DecimalAttributeTypeConfig.class),
  @JsonSubTypes.Type(name = "enum", value = EnumAttributeTypeConfig.class),
  @JsonSubTypes.Type(name = "time", value = TimeAttributeTypeConfig.class),
  @JsonSubTypes.Type(name = "date", value = DateAttributeTypeConfig.class),
  @JsonSubTypes.Type(name = "datetime", value = DateTimeAttributeTypeConfig.class),
  @JsonSubTypes.Type(name = "zoned-datetime", value = ZonedDateTimeAttributeTypeConfig.class),
  @JsonSubTypes.Type(name = "offset-datetime", value = OffsetDateTimeAttributeTypeConfig.class),
  @JsonSubTypes.Type(name = "flag", value = FlagAttributeTypeConfig.class),
})
@RegisterForReflection
public interface DataAttributeConfig extends ConfigObject<AttributeType> {

  boolean indexable();

  boolean multivalued();

}
