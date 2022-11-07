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

package com.backpackcloud.sherlogholmes.domain.exporters;

import com.backpackcloud.cli.Writer;
import com.backpackcloud.serializer.Serializer;
import com.backpackcloud.sherlogholmes.domain.Attribute;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.DataExporter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class JsonDataExporter implements DataExporter {

  private final String[] attributes;

  public JsonDataExporter(String... attributes) {
    this.attributes = attributes;
  }

  @Override
  public void export(Writer writer, Stream<DataEntry> stream) {
    Serializer serializer = Serializer.json();
    stream.forEach(entry -> {
      Map<String, String> map = new HashMap<>();
      for (String name : attributes) {
        map.put(name, entry.attribute(name)
          .flatMap(Attribute::formattedValue)
          .orElse(null));
      }
      writer.writeln(serializer.serialize(map));
    });
  }

}
