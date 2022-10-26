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
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.DataParser;
import com.backpackcloud.sherlogholmes.domain.DataReader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexDataReader implements DataReader<Function<String, String>> {

  private final Charset inputCharset;
  private final Pattern pattern;
  private final NonMatchMode nonMatchMode;
  private final boolean removeAnsiColors;

  public RegexDataReader(Charset inputCharset, Pattern pattern, NonMatchMode nonMatchMode, boolean removeAnsiColors) {
    this.inputCharset = inputCharset;
    this.pattern = pattern;
    this.nonMatchMode = nonMatchMode;
    this.removeAnsiColors = removeAnsiColors;
  }

  @Override
  public void read(String location, Supplier<DataEntry> dataSupplier, DataParser<Function<String, String>> parser, Consumer<DataEntry> consumer) {
    StringBuilder dataContent = new StringBuilder();
    try {
      Files.lines(Path.of(location), inputCharset).forEach(line -> {
        String content = removeAnsiColors ?
          line.replaceAll("\\x1B(?:[@-Z\\\\-_]|\\[[0-?]*[ -/]*[@-~])", "") :
          line;
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
          switch (nonMatchMode) {
            case IGNORE -> parser.parse(dataSupplier, matcher::group).ifPresent(consumer);
            case APPEND -> {
              // flushes previous content, as a new match means a new data entry
              if (!dataContent.isEmpty()) {
                Matcher dataContentMatcher = pattern.matcher(dataContent.toString());
                if (dataContentMatcher.find()) {
                  parser.parse(dataSupplier, dataContentMatcher::group).ifPresent(consumer::accept);
                }
                dataContent.delete(0, dataContent.length());
              }
              dataContent.append(content);
            }
          }
        } else {
          if (nonMatchMode == NonMatchMode.APPEND) {
            if (!dataContent.isEmpty()) {
              dataContent.append("\n");
            }
            dataContent.append(content);
          }
        }
      });
      if (nonMatchMode == NonMatchMode.APPEND && !dataContent.isEmpty()) {
        Matcher matcher = pattern.matcher(dataContent.toString());
        if (matcher.find()) {
          parser.parse(dataSupplier, matcher::group).ifPresent(consumer::accept);
        }
      }
    } catch (IOException e) {
      throw new UnbelievableException(e);
    }
  }

  public enum NonMatchMode {

    APPEND, IGNORE

  }

}
