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

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

public enum Operand {

  SET("*") {
    @Override
    public boolean execute(Attribute<?> attribute, Object reference) {
      return attribute.value().isPresent();
    }

    @Override
    public Operand invert() {
      return UNSET;
    }
  },

  UNSET("!") {
    @Override
    public boolean execute(Attribute<?> attribute, Object reference) {
      return attribute.value().isEmpty();
    }

    @Override
    public Operand invert() {
      return SET;
    }
  },

  EQUAL("==") {
    @Override
    public boolean execute(Attribute<?> attribute, Object reference) {
      return attribute.values().anyMatch(value -> value.equals(reference));
    }

    @Override
    public Operand invert() {
      return DIFFERENT;
    }

  },

  DIFFERENT("!=") {
    @Override
    public boolean execute(Attribute<?> attribute, Object reference) {
      return attribute.values().noneMatch(value -> value.equals(reference));
    }

    @Override
    public Operand invert() {
      return EQUAL;
    }
  },

  CONTAINS("%") {
    @Override
    public boolean execute(Attribute<?> attribute, Object reference) {
      return attribute.formattedValues()
        .anyMatch(value -> value.contains(String.valueOf(reference)));
    }

    @Override
    public Operand invert() {
      return EXCLUDES;
    }

  },

  EXCLUDES("!%") {
    @Override
    public boolean execute(Attribute<?> attribute, Object reference) {
      return attribute.formattedValues()
        .noneMatch(value -> value.contains(String.valueOf(reference)));
    }

    @Override
    public Operand invert() {
      return EQUAL;
    }
  },

  MATCHES("~=") {
    @Override
    public boolean execute(Attribute<?> attribute, Object reference) {
      Pattern pattern = Pattern.compile(String.valueOf(reference));
      return attribute.formattedValues()
        .anyMatch(value -> pattern.matcher(value).find());
    }

    @Override
    public Operand invert() {
      return MISMATCHES;
    }

  },

  MISMATCHES("!~=") {
    @Override
    public boolean execute(Attribute<?> attribute, Object reference) {
      Pattern pattern = Pattern.compile(String.valueOf(reference));
      return attribute.formattedValues()
        .noneMatch(value -> pattern.matcher(value).find());
    }

    @Override
    public Operand invert() {
      return EQUAL;
    }
  },

  LESS("<") {
    @Override
    public boolean execute(Attribute attribute, Object reference) {
      return attribute.values()
        .anyMatch(value -> attribute.spec().type().compare(value, reference) < 0);
    }

    @Override
    public Operand invert() {
      return GREATER_OR_EQUAL;
    }
  },

  LESS_OR_EQUAL("<=") {
    @Override
    public boolean execute(Attribute attribute, Object reference) {
      return attribute.values()
        .anyMatch(value -> attribute.spec().type().compare(value, reference) <= 0);
    }

    @Override
    public Operand invert() {
      return GREATER;
    }
  },

  GREATER(">") {
    @Override
    public boolean execute(Attribute attribute, Object reference) {
      return attribute.values()
        .anyMatch(value -> attribute.spec().type().compare(value, reference) > 0);
    }

    @Override
    public Operand invert() {
      return LESS_OR_EQUAL;
    }
  },

  GREATER_OR_EQUAL(">=") {
    @Override
    public boolean execute(Attribute attribute, Object reference) {
      return attribute.values()
        .anyMatch(value -> attribute.spec().type().compare(value, reference) >= 0);
    }

    @Override
    public Operand invert() {
      return LESS;
    }
  };

  private final String symbol;

  Operand(String symbol) {
    this.symbol = symbol;
  }

  public String symbol() {
    return symbol;
  }

  public abstract boolean execute(Attribute<?> attribute, Object reference);

  public abstract Operand invert();

  public static Optional<Operand> findBySymbol(String symbol) {
    return Arrays.stream(Operand.values())
      .filter(operand -> symbol.equals(operand.symbol))
      .findFirst();
  }

  public static Optional<Operand> findByName(String name) {
    try {
      return Optional.of(Operand.valueOf(name.toUpperCase()));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public static Optional<Operand> find(String key) {
    return findBySymbol(key)
      .or(() -> findByName(key));
  }

}
