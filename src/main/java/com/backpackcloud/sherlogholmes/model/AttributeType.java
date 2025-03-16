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

import com.backpackcloud.sherlogholmes.model.types.EnumType;
import com.backpackcloud.sherlogholmes.model.types.TemporalType;
import com.fasterxml.jackson.annotation.JsonValue;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

@RegisterForReflection
public interface AttributeType<E> extends Converter<E>, Formatter<E>, Validator<E>, Comparator<E> {

  @JsonValue
  String name();

  @Override
  default String format(E value) {
    return String.valueOf(value);
  }

  @Override
  default boolean isValid(E value) {
    return true;
  }

  @Override
  default int compare(E o1, E o2) {
    return ((Comparable) o1).compareTo(o2);
  }

  AttributeType<String> TEXT = create("text", input -> input);
  AttributeType<Integer> NUMBER = create("number", Integer::parseInt);
  AttributeType<Double> DECIMAL = create("decimal", Double::parseDouble);
  AttributeType<LocalTime> TIME = new TemporalType<>(DateTimeFormatter.ISO_TIME, LocalTime::from);
  AttributeType<LocalDate> DATE = new TemporalType<>(DateTimeFormatter.ISO_DATE, LocalDate::from);
  AttributeType<LocalDateTime> DATETIME = new TemporalType<>(DateTimeFormatter.ISO_DATE_TIME, LocalDateTime::from);
  AttributeType<Boolean> FLAG = create("flag", Boolean::parseBoolean);

  static <E> AttributeType<E> create(String name, Converter<E> converter) {
    return new AttributeType<>() {
      @Override
      public String name() {
        return name;
      }

      @Override
      public E convert(String input) {
        return converter.convert(input);
      }
    };
  }

  static AttributeType<String> enumOf(String... values) {
    return new EnumType(values);
  }

  static AttributeType<String> text() {
    return TEXT;
  }

  static AttributeType<Integer> number() {
    return NUMBER;
  }

  static AttributeType<Double> decimal() {
    return DECIMAL;
  }

  static AttributeType<LocalTime> time() {
    return TIME;
  }

  static AttributeType<LocalDate> date() {
    return DATE;
  }

  static AttributeType<LocalDateTime> datetime() {
    return DATETIME;
  }

  static AttributeType<Boolean> flag() {
    return FLAG;
  }

}
