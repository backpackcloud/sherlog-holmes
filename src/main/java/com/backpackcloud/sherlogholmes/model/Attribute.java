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
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class Attribute<E> implements Comparable<Attribute<E>> {

  private final String name;
  @JsonProperty
  private final AttributeSpec<E> spec;
  @JsonProperty
  private E value;
  @JsonProperty
  private Set<E> values;

  public Attribute(String name, AttributeSpec<E> spec) {
    this.name = name;
    this.spec = spec;
    if (spec.multivalued()) {
      this.values = new HashSet<>();
    }
  }

  private void setValue(E value) {
    if (spec.multivalued() && value != null) {
      this.values.add(value);
    } else {
      this.value = value;
    }
  }

  public String name() {
    return name;
  }

  public AttributeSpec<E> spec() {
    return spec;
  }

  public Attribute<E> assign(E value) {
    if (spec.type().isValid(value)) {
      setValue(value);
    } else {
      throw new UnbelievableException("Invalid value: " + value);
    }
    return this;
  }

  public Attribute<E> assignFromInput(String input) {
    if (input == null || input.isEmpty()) {
      return this;
    }
    return assign(spec.type().convert(input));
  }

  public Optional<E> value() {
    if (spec.multivalued()) {
      return values.isEmpty() ? Optional.empty() : Optional.of(values.iterator().next());
    }
    return Optional.ofNullable(value);
  }

  public Optional<String> formattedValue() {
    if (spec.multivalued()) {
      return values.isEmpty() ? Optional.empty() : Optional.of(spec.type().format(values.iterator().next()));
    }
    return value().map(spec.type()::format);
  }

  public Stream<E> values() {
    if (spec.multivalued()) {
      return values.stream();
    }
    return value == null ? Stream.empty() : Stream.of(value);
  }

  public Stream<String> formattedValues() {
    if (spec.multivalued()) {
      return values.stream().map(spec.type()::format);
    }
    return value == null ? Stream.empty() : Stream.of(spec.type().format(value));
  }

  public int compareTo(Attribute<E> other) {
    if (spec.multivalued()) {
      return values.size() - (int) other.values().count();
    }
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
    return spec.type().compare(value, otherValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, spec, value, values);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Attribute attribute)) return false;
    return name.equals(attribute.name) &&
      spec.equals(attribute.spec) &&
      Objects.equals(value, attribute.value) &&
      Objects.equals(values, attribute.values);
  }

}
