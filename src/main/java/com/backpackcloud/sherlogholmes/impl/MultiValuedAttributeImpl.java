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

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class MultiValuedAttributeImpl<E> implements Attribute<E> {

  private final String name;
  private final AttributeType<E> type;

  private final Set<E> values;

  public MultiValuedAttributeImpl(String name, AttributeType<E> type) {
    this.name = name;
    this.values = new HashSet<>();
    this.type = type;
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
      this.values.add(value);
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
    return values.isEmpty() ? Optional.empty() : Optional.of(values.iterator().next());
  }

  @Override
  public Optional<String> formattedValue() {
    return values.isEmpty() ? Optional.empty() : Optional.of(type.format(values.iterator().next()));
  }

  @Override
  public Stream<E> values() {
    return values.stream();
  }

  @Override
  public Stream<String> formattedValues() {
    return values.stream().map(type::format);
  }

  @Override
  public boolean multivalued() {
    return true;
  }

  @Override
  public int compareTo(Attribute<E> other) {
    return values.size() - (int) other.values().count();
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, type, values);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MultiValuedAttributeImpl attribute)) return false;
    return name.equals(attribute.name()) &&
      type.equals(attribute.type()) &&
      Objects.equals(values, attribute.values);
  }

}
