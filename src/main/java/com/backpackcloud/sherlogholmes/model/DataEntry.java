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

package com.backpackcloud.sherlogholmes.model;

import com.backpackcloud.cli.Displayable;
import com.backpackcloud.cli.Writer;
import com.backpackcloud.sherlogholmes.util.StringWalker;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataEntry implements Comparable<DataEntry>, Displayable {

  @JsonValue
  private final Map<String, Attribute> attributes;
  private final String displayFormat;

  public DataEntry() {
    this(null);
  }

  public DataEntry(String displayFormat) {
    this.displayFormat = displayFormat;
    this.attributes = new LinkedHashMap<>();
  }

  public DataEntry(Map<String, Attribute> attributes, String displayFormat) {
    this.attributes = attributes;
    this.displayFormat = displayFormat;
  }

  public boolean hasAttribute(String name) {
    return this.attributes.containsKey(name);
  }

  public void addAttribute(Attribute attribute) {
    this.attributes.put(attribute.name(), attribute);
  }

  public void addAttribute(String name, AttributeSpec spec) {
    addAttribute(new Attribute(name, spec));
  }

  public AttributeBuilder addAttribute(String name) {
    return new AttributeBuilder<>(name, dataAttribute -> this.attributes.put(name, dataAttribute));
  }

  public <E> AttributeBuilder<E> addAttribute(String name, Class<E> valueType) {
    return new AttributeBuilder<>(name, valueType, dataAttribute -> this.attributes.put(name, dataAttribute));
  }

  public <E> AttributeBuilder<E> addAttribute(String name, E value) {
    return new AttributeBuilder<>(name, value, dataAttribute -> this.attributes.put(name, dataAttribute));
  }

  public void remove(Attribute attribute) {
    if (this.attributes.containsValue(attribute)) {
      this.attributes.remove(attribute.name());
    }
  }

  public void remove(String name) {
    this.attributes.remove(name);
  }

  public <E> Optional<Attribute<E>> attribute(String name) {
    if (name == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(this.attributes.get(name));
  }

  public <E> Optional<Attribute<E>> attribute(String name, Class<E> type) {
    if (name == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(this.attributes.get(name));
  }

  public List<Attribute> attributes() {
    return new ArrayList<>(this.attributes.values());
  }

  public String displayFormat() {
    return displayFormat;
  }

  public DataEntry displayFormat(String format) {
    return new DataEntry(attributes, format);
  }

  public void toDisplay(Writer writer) {
    toDisplay(writer, displayFormat);
  }

  public void toDisplay(Writer writer, String outputFormat) {
    StringWalker walker = new StringWalker(writer::write, token -> {
      String name;
      String format;
      boolean showIcon, showValue;
      int indexOfPipe = token.indexOf("|");
      int indexOfHash = token.indexOf("#");
      int indexOfAt = token.indexOf("@");

      showIcon = token.startsWith("#") || token.startsWith("@");
      showValue = token.startsWith("#") || !showIcon;
      if (indexOfPipe > 0) {
        format = token.substring(indexOfPipe + 1);
        name = token.substring(Math.max(indexOfHash, indexOfAt) + 1, indexOfPipe);
      } else {
        name = token.substring(Math.max(indexOfHash, indexOfAt) + 1);
        format = null;
      }

      attribute(name)
        .flatMap(Attribute::formattedValue)
        .ifPresent(value -> {
          Writer styledWriter = writer.withStyle("attribute-" + name + "-" + value, "attribute-" + name);
          if (showIcon) {
            styledWriter.writeIcon("attribute-" + name).write(" ");
          }
          if (showValue) {
            styledWriter.write(format != null ? String.format(format, value) : value);
          }
        });

    });

    walker.walk(outputFormat);
  }

  public int compareTo(DataEntry dataEntry) {
    for (Attribute attribute : attributes.values()) {
      int result = dataEntry.attribute(attribute.name())
        .map(otherAttribute -> attribute.compareTo(otherAttribute))
        .orElse(1);
      if (result != 0) {
        return result;
      }
    }
    return 0;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DataEntry dataEntry)) return false;
    if (attributes.size() != dataEntry.attributes().size()) {
      return false;
    }
    for (Attribute<?> attribute : attributes.values()) {
      boolean equals = dataEntry.attribute(attribute.name())
        .map(attribute::equals)
        .orElse(false);
      if (!equals) {
        return false;
      }
    }
    return true;
  }

  public int hashCode() {
    return Objects.hash(attributes);
  }

  public <E> E valueOf(String attributeName) {
    return (E) valueOf(attributeName, Object.class);
  }

  public <E> E valueOf(String attributeName, Class<E> type) {
    if (attributeName.contains(":")) {
      return (E) Stream.of(attributeName.split(":"))
        .map(attr -> valueOf(attr, String.class))
        .collect(Collectors.joining(":"));
    }
    if (type.equals(String.class)) {
      return (E) attribute(attributeName).flatMap(Attribute::formattedValue).orElse(null);
    }
    return attribute(attributeName, type).flatMap(Attribute::value).orElse(null);
  }

}
