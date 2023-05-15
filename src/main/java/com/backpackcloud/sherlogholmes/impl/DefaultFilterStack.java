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
import com.backpackcloud.cli.Writer;
import com.backpackcloud.sherlogholmes.domain.DataFilter;
import com.backpackcloud.sherlogholmes.domain.FilterStack;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

@ApplicationScoped
public class DefaultFilterStack implements FilterStack {

  private final Deque<DataFilter> stack;

  private final List<Runnable> stackListeners;

  private final List<Runnable> filterListeners;

  public DefaultFilterStack() {
    this.stack = new ArrayDeque<>();
    this.stackListeners = new ArrayList<>();
    this.filterListeners = new ArrayList<>();
  }

  private void notifyStackChange() {
    this.stackListeners.forEach(Runnable::run);
  }

  private void notifyFilter() {
    this.filterListeners.forEach(Runnable::run);
  }

  @Override
  public void onStackChange(Runnable action) {
    this.stackListeners.add(action);
  }

  @Override
  public void onFilter(Runnable action) {
    this.filterListeners.add(action);
  }

  @Override
  public DataFilter peek() {
    return stack.isEmpty() ? null : stack.peek();
  }

  @Override
  public DataFilter filter() {
    try {
      return stack.stream().reduce(DataFilter.ALLOW_ALL, DataFilter::and);
    } finally {
      notifyFilter();
    }
  }

  @Override
  public FilterStack push(DataFilter filter) {
    stack.push(filter);

    return this;
  }

  @Override
  public DataFilter pop() {
    if (stack.isEmpty()) {
      throw new UnbelievableException("Stack is empty");
    }
    try {
      return stack.pop();
    } finally {
      notifyStackChange();
    }
  }

  @Override
  public void apply(Operation operation) {
    if (stack.isEmpty()) {
      throw new UnbelievableException("Stack is empty");
    }
    // use direct calls to pop to avoid triggering the listeners
    switch (operation) {
      case AND -> {
        if (stack.size() < 2) {
          throw new UnbelievableException("Stack size < 2");
        }
        push(stack.pop().and(stack.pop()));
      }
      case OR -> {
        if (stack.size() < 2) {
          throw new UnbelievableException("Stack size < 2");
        }
        push(stack.pop().or(stack.pop()));
      }
      case NOT -> push(stack.pop().negate());
      case DUP -> push(stack.peek());
    }
  }

  @Override
  public boolean isEmpty() {
    return stack.isEmpty();
  }

  @Override
  public int size() {
    return stack.size();
  }

  @Override
  public Stream<DataFilter> stream() {
    return stack.stream();
  }

  @Override
  public void clear() {
    stack.clear();
    notifyStackChange();
  }

  @Override
  public void toDisplay(Writer writer) {
    if (!isEmpty()) {
      Iterator<DataFilter> iterator = stack.iterator();
      int size = stack.size();
      DataFilter filter;
      for (int i = 1; iterator.hasNext(); i++) {
        filter = iterator.next();
        if (size > 1) {
          if (i == size) {
            writer.write("╰─ ");
          } else if (i == 1) {
            writer.write("╭─ ");
          } else {
            writer.write("|– ");
          }
        }
        writer
          .writeIcon("filter")
          .write(" ")
          .write(filter)
          .newLine();
      }
    }
  }

}
