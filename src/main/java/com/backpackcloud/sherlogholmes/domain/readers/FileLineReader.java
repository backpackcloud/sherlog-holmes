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

package com.backpackcloud.sherlogholmes.domain.readers;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.sherlogholmes.domain.DataReader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FileLineReader implements DataReader {

  private final Charset charset;
  private final int linesToSkip;
  private final boolean removeAnsiColors;

  public FileLineReader(Charset charset, int linesToSkip, boolean removeAnsiColors) {
    this.charset = charset;
    this.linesToSkip = linesToSkip;
    this.removeAnsiColors = removeAnsiColors;
  }

  @Override
  public void read(String location, Consumer<String> consumer) {
    AtomicInteger count = new AtomicInteger(0);
    Predicate<String> ignoredLines = s -> count.incrementAndGet() > linesToSkip;
    try {
      Files.lines(Path.of(location), charset)
        .filter(ignoredLines)
        .forEach(line -> consumer.accept(removeAnsiColors ?
          line.replaceAll("\\x1B(?:[@-Z\\\\-_]|\\[[0-?]*[ -/]*[@-~])", "") :
          line
        ));
    } catch (IOException e) {
      throw new UnbelievableException(e);
    }
  }

}