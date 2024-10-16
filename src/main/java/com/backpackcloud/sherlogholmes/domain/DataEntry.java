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

package com.backpackcloud.sherlogholmes.domain;

import com.backpackcloud.cli.Displayable;
import com.backpackcloud.cli.Writer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface DataEntry extends Comparable<DataEntry>, Displayable {

  void addAttribute(Attribute attribute);

  void addAttribute(String name, AttributeSpec spec);

  AttributeBuilder addAttribute(String name);

  <E> AttributeBuilder<E> addAttribute(String name, Class<E> valueType);

  <E> AttributeBuilder<E> addAttribute(String name, E value);

  boolean hasAttribute(String name);

  void remove(Attribute attribute);

  void remove(String name);

  <E> Optional<Attribute<E>> attribute(String name);

  <E> Optional<Attribute<E>> attribute(String name, Class<E> type);

  List<Attribute> attributes();

  String displayFormat();

  DataEntry displayFormat(String format);

  void toDisplay(Writer writer, String format);

  default <E> E valueOf(String attributeName) {
    return (E) valueOf(attributeName, Object.class);
  }

  default <E> E valueOf(String attributeName, Class<E> type) {
    if (attributeName.contains(":")) {
      return (E) Stream.of(attributeName.split(":"))
        .map(attr -> valueOf(attr, String.class))
        .collect(Collectors.joining(":"));
    }
    if (type.equals(String.class)) {
      return (E) attribute(attributeName).flatMap(Attribute::formattedValue).orElse(null);
    }
    return (E) attribute(attributeName, type).flatMap(Attribute::value).orElse(null);
  }

}
