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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AttributeBuilder<T> {

  private static final Map<Class, AttributeType> typeMap = new HashMap<>() {{
    put(String.class, AttributeType.TEXT);
    put(Integer.class, AttributeType.NUMBER);
    put(Double.class, AttributeType.DECIMAL);
    put(LocalTime.class, AttributeType.TIME);
    put(LocalDate.class, AttributeType.DATE);
    put(LocalDateTime.class, AttributeType.DATETIME);
    put(Boolean.class, AttributeType.FLAG);
  }};

  private final String name;
  private final Consumer<Attribute> outcomeConsumer;
  private AttributeType type;
  private boolean multivalued;
  private Object value;

  public AttributeBuilder(String name, Consumer<Attribute> outcomeConsumer) {
    this.name = name;
    this.type = AttributeType.TEXT;
    this.outcomeConsumer = outcomeConsumer;
    this.outcomeConsumer.accept(build());
  }

  public AttributeBuilder(String name, Class valueType, Consumer<Attribute> outcomeConsumer) {
    this.name = name;
    this.type = typeMap.getOrDefault(valueType, AttributeType.TEXT);
    this.outcomeConsumer = outcomeConsumer;
    this.outcomeConsumer.accept(build());
  }

  public AttributeBuilder(String name, Object value, Consumer<Attribute> outcomeConsumer) {
    this.name = name;
    this.type = typeMap.getOrDefault(value.getClass(), AttributeType.TEXT);
    this.value = value;
    this.outcomeConsumer = outcomeConsumer;
    this.outcomeConsumer.accept(build());
  }

  public AttributeBuilder ofType(AttributeType type) {
    this.type = type;
    outcomeConsumer.accept(build());
    return this;
  }

  public AttributeBuilder multivalued(boolean multivalued) {
    this.multivalued = multivalued;
    return this;
  }

  public Attribute fromInput(String input) {
    Attribute attribute = build();
    attribute.assignFromInput(input);
    outcomeConsumer.accept(attribute);
    return attribute;
  }

  public Attribute withValue(Object value) {
    Attribute attribute = build();
    attribute.assign(value);
    outcomeConsumer.accept(attribute);
    return attribute;
  }

  public <E> AttributeBuilder<E> multivalued() {
    return multivalued(true);
  }

  private Attribute build() {
    Attribute attribute = new Attribute(name, new AttributeSpec(type, multivalued));
    if (value != null) {
      attribute.assign(value);
    }
    return attribute;
  }

}
