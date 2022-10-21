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
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.DataFilter;
import com.backpackcloud.sherlogholmes.domain.Operand;
import com.backpackcloud.text.Interpolator;

public class DefaultDataFilter implements DataFilter {

  private final String attribute;
  private final Operand operand;
  private final String reference;

  public DefaultDataFilter(String attribute, Operand operand, String reference) {
    this.attribute = attribute;
    this.operand = operand;
    this.reference = reference;
  }

  @Override
  public boolean test(DataEntry entry) {
    String value = new Interpolator(token -> entry.attribute(token)
      .flatMap(Attribute::formattedValue)
      .orElse(""))
      .eval(reference)
      .orElse(reference);
    return entry.attribute(attribute)
      .filter(attribute -> operand.execute(attribute, value.isBlank() ? null : attribute.type().convert(value)))
      .isPresent();
  }

  @Override
  public DataFilter negate() {
    return new DefaultDataFilter(attribute, operand.invert(), reference);
  }

  @Override
  public void toDisplay(Writer writer) {
    writer
      .write(attribute).write(" ")
      .write(operand.symbol())
      .write(" ")
      .write(reference);
  }

  @Override
  public DataFilter or(DataFilter other) {
    return new CompositeFilter(this, other, CompositeFilter.BooleanOperation.OR);
  }

  @Override
  public DataFilter and(DataFilter other) {
    return new CompositeFilter(this, other, CompositeFilter.BooleanOperation.AND);
  }

}
