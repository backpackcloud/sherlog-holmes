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

package com.backpackcloud.sherlogholmes.model.types;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.sherlogholmes.model.AttributeType;

public class FallbackType<E> implements AttributeType<E> {

  private final AttributeType[] types;
  private int index = -1;

  public FallbackType(AttributeType... types) {
    this.types = types;
  }

  @Override
  public String name() {
    return "fallback";
  }

  @Override
  public String format(E value) {
    if (index == -1) {
      for (int i = 0; i < types.length; i++) {
        try {
          String result = types[i].format(value);
          index = i;
          return result;
        } catch (Exception e) {
          // try the next one
        }
      }
      throw new UnbelievableException("No more attributes available");
    }
    return types[index].format(value);
  }

  @Override
  public E convert(String input) {
    if (index == -1) {
      for (int i = 0; i < types.length; i++) {
        try {
          E result = (E) types[i].convert(input);
          index = i;
          return result;
        } catch (Exception e) {
          // try the next one
        }
      }
      throw new UnbelievableException("No more attributes available");
    }
    return (E) types[index].convert(input);
  }

}
