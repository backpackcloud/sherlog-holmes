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

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.sherlogholmes.domain.Attribute;
import com.backpackcloud.sherlogholmes.domain.AttributeType;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class AttributeImpl<E> implements Attribute<E> {

  private final String name;
  private final AttributeType<E> type;

  private E value;

  public AttributeImpl(String name, AttributeType<E> type) {
    this.name = name;
    this.type = type;
  }

  public AttributeImpl(String name, AttributeType<E> type, E value) {
    this(name, type);
    this.value = value;
  }

  @Override
  public String name() {
    return this.name;
  }

  @Override
  public AttributeType<E> type() {
    return this.type;
  }

  @Override
  public Attribute<E> assign(E value) {
    if (this.type.isValid(value)) {
      this.value = value;
    } else {
      throw new UnbelievableException("Invalid value: " + value);
    }
    return this;
  }

  @Override
  public Attribute<E> assignFromInput(String input) {
    return assign(type.convert(input));
  }

  @Override
  public Optional<E> value() {
    return Optional.ofNullable(value);
  }

  @Override
  public Optional<String> formattedValue() {
    return value().map(type::format);
  }

  @Override
  public Stream<E> values() {
    return value == null ? Stream.empty() : Stream.of(value);
  }

  @Override
  public Stream<String> formattedValues() {
    return value == null ? Stream.empty() : Stream.of(type.format(value));
  }

  @Override
  public boolean multivalued() {
    return false;
  }

  @Override
  public int compareTo(Attribute<E> other) {
    E otherValue = other.value().orElse(null);
    if (value == otherValue) {
      return 0;
    }
    if (value == null) {
      return -1;
    }
    if (otherValue == null) {
      return 1;
    }
    return type.compare(value, otherValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Attribute attribute)) return false;
    return name.equals(attribute.name()) &&
      type.equals(attribute.type()) &&
      Objects.equals(value, attribute.value().orElse(null));
  }

}
