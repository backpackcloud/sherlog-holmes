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

import com.backpackcloud.cli.Displayable;
import com.backpackcloud.cli.Writer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.function.Predicate;

@JsonDeserialize(using = DataFilterDeserializer.class)
public interface DataFilter extends Displayable, Predicate<DataEntry> {

  boolean test(DataEntry entry);

  @Override
  default void toDisplay(Writer writer) {
    writer.write(toString());
  }

  /**
   * Creates a new Filter composed of this filter and the given one
   * by using the OR operation.
   *
   * @param other the other filter to use for composing.
   * @return a new Filter that represents the OR operation between this filter and the given one.
   */
  DataFilter or(DataFilter other);

  /**
   * Creates a new EntryFilter composed of this filter and the given one
   * by using the AND operation.
   *
   * @param other the other filter to use for composing.
   * @return a new EntryFilter that represents the AND operation between this filter and the given one.
   */
  DataFilter and(DataFilter other);

  /**
   * Creates a new EntryFilter that returns the opposite of what this filter
   * would return.
   *
   * @return a new EntryFilter that represents the NOT operation.
   */
  @Override
  DataFilter negate();

  DataFilter ALLOW_ALL = new DataFilter() {
    @Override
    public boolean test(DataEntry entry) {
      return true;
    }

    @Override
    public DataFilter or(DataFilter other) {
      return ALLOW_ALL;
    }

    @Override
    public DataFilter and(DataFilter other) {
      return other;
    }

    @Override
    public DataFilter negate() {
      return DENY_ALL;
    }
  };

  DataFilter DENY_ALL = new DataFilter() {
    @Override
    public boolean test(DataEntry entry) {
      return false;
    }

    @Override
    public DataFilter or(DataFilter other) {
      return other;
    }

    @Override
    public DataFilter and(DataFilter other) {
      return DENY_ALL;
    }

    @Override
    public DataFilter negate() {
      return ALLOW_ALL;
    }
  };

}
