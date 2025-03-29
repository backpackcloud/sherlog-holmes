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

import com.backpackcloud.cli.Writer;

public class CompositeFilter implements DataFilter {

  private final DataFilter left;
  private final DataFilter right;
  private final BooleanOperation operation;

  public CompositeFilter(DataFilter left, DataFilter right, BooleanOperation operation) {
    this.left = left;
    this.right = right;
    this.operation = operation;
  }

  @Override
  public boolean test(DataEntry entry) {
    return switch (operation) {
      case OR -> left.test(entry) || right.test(entry);
      case AND -> left.test(entry) && right.test(entry);
    };
  }

  @Override
  public void toDisplay(Writer writer) {
    writer.withStyle("filter_grouping").write("(");
    left.toDisplay(writer);
    writer.withStyle("filter_grouping").write(") ");
    writer.withStyle("filter_operation").write(operation.name().toLowerCase());
    writer.withStyle("filter_grouping").write(" (");
    right.toDisplay(writer);
    writer.withStyle("filter_grouping").write(")");
  }

  @Override
  public DataFilter or(DataFilter other) {
    return new CompositeFilter(this, other, BooleanOperation.OR);
  }

  @Override
  public DataFilter and(DataFilter other) {
    return new CompositeFilter(this, other, BooleanOperation.AND);
  }

  @Override
  public DataFilter negate() {
    return switch (operation) {
      case OR -> new CompositeFilter(left.negate(), right.negate(), BooleanOperation.AND);
      case AND -> new CompositeFilter(left.negate(), right.negate(), BooleanOperation.OR);
    };
  }

  public enum BooleanOperation {
    AND, OR
  }

}
