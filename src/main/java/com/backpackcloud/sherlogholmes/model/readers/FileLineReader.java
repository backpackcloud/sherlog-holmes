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

package com.backpackcloud.sherlogholmes.model.readers;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.sherlogholmes.model.DataReader;
import com.backpackcloud.sherlogholmes.model.Metadata;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class FileLineReader implements DataReader<String> {

  private final Charset charset;
  private final int skipLines;

  public FileLineReader(Charset charset, int skipLines) {
    this.charset = charset;
    this.skipLines = skipLines;
  }

  @Override
  public void read(String location, BiConsumer<Metadata, String> consumer) {
    try {
      Path locationPath = Path.of(location);

      if (locationPath.toFile().exists()) {
        readLines(locationPath, consumer);
      } else {
        glob(location, consumer);
      }
    } catch (InvalidPathException e) {
      glob(location, consumer);
    }
  }

  private void glob(String location, BiConsumer<Metadata, String> consumer) {
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + location);
    File directory = new File(location).getParentFile();

    if (directory != null) {
      for (File file : directory.listFiles()) {
        Path path = Path.of(file.getPath());
        if (matcher.matches(path)) {
          readLines(path, consumer);
        }
      }
    }
  }

  private void readLines(Path path, BiConsumer<Metadata, String> consumer) {
    AtomicInteger count = new AtomicInteger(0);
    try {
      Files.lines(path, charset)
        .forEach(line -> {
          if (count.get() >= skipLines) {
            consumer.accept(new Metadata(path.getFileName().toString(), count.incrementAndGet()), line);
          }
        });
    } catch (IOException e) {
      throw new UnbelievableException(e);
    }
  }

}
