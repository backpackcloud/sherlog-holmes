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
import com.backpackcloud.sherlogholmes.domain.DataFilter;
import com.backpackcloud.sherlogholmes.domain.FilterFactory;
import com.backpackcloud.sherlogholmes.domain.Operand;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.StringTokenizer;

@ApplicationScoped
public class DefaultFilterFactory implements FilterFactory {

  @Override
  public DataFilter create(String expression) {
    if (expression == null || expression.isBlank()) {
      return DataFilter.ALLOW_ALL;
    }

    StringTokenizer tokenizer = new StringTokenizer(expression);
    String left = tokenizer.nextToken().trim();
    String symbol = tokenizer.nextToken().trim();
    String right = expression.substring(expression.indexOf(symbol) + symbol.length()).trim();

    Operand operand = Operand.find(symbol)
      .orElseThrow(() -> new UnbelievableException("Invalid operand: " + symbol));

    return new DefaultDataFilter(left, operand, right);
  }

}
