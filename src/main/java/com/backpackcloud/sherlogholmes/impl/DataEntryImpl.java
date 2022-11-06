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

package com.backpackcloud.sherlogholmes.impl;

import com.backpackcloud.cli.Writer;
import com.backpackcloud.sherlogholmes.domain.Attribute;
import com.backpackcloud.sherlogholmes.domain.AttributeBuilder;
import com.backpackcloud.sherlogholmes.domain.AttributeSpec;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.text.StringWalker;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DataEntryImpl implements DataEntry {

  private final Map<String, Attribute> attributes;
  private String displayFormat;

  public DataEntryImpl() {
    this.attributes = new LinkedHashMap<>();
  }

  @Override
  public boolean hasAttribute(String name) {
    return this.attributes.containsKey(name);
  }

  @Override
  public void addAttribute(Attribute attribute) {
    this.attributes.put(attribute.name(), attribute);
  }

  @Override
  public void addAttribute(String name, AttributeSpec spec) {
    addAttribute(new AttributeImpl(name, spec));
  }

  @Override
  public AttributeBuilder addAttribute(String name) {
    return new AttributeBuilderImpl(name, dataAttribute -> this.attributes.put(name, dataAttribute));
  }

  @Override
  public <E> AttributeBuilder<E> addAttribute(String name, Class<E> valueType) {
    return new AttributeBuilderImpl(name, valueType, dataAttribute -> this.attributes.put(name, dataAttribute));
  }

  @Override
  public <E> AttributeBuilder<E> addAttribute(String name, E value) {
    return new AttributeBuilderImpl(name, value, dataAttribute -> this.attributes.put(name, dataAttribute));
  }

  @Override
  public void remove(Attribute attribute) {
    if (this.attributes.containsValue(attribute)) {
      this.attributes.remove(attribute.name());
    }
  }

  @Override
  public void remove(String name) {
    this.attributes.remove(name);
  }

  @Override
  public <E> Optional<Attribute<E>> attribute(String name) {
    if (name == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(this.attributes.get(name));
  }

  @Override
  public <E> Optional<Attribute<E>> attribute(String name, Class<E> type) {
    if (name == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(this.attributes.get(name));
  }

  @Override
  public List<Attribute> attributes() {
    return new ArrayList<>(this.attributes.values());
  }

  @Override
  public void displayFormat(String format) {
    this.displayFormat = format;
  }

  @Override
  public void toDisplay(Writer writer) {
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
        format = "";
      }

      attribute(name)
        .flatMap(Attribute::formattedValue)
        .ifPresent(value -> {
          Writer styledWriter = writer.withStyle("attribute-" + name + "-" + value, "attribute-" + name);
          if (showIcon) {
            styledWriter.writeIcon("attribute-" + name).write(" ");
          }
          if (showValue) {
            styledWriter.write(String.format("%" + format + "s", value));
          }
        });

    });

    walker.walk(displayFormat);
  }

  @Override
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

  @Override
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

  @Override
  public int hashCode() {
    return Objects.hash(attributes);
  }

}
