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

package com.backpackcloud.sherlogholmes.domain;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.sherlogholmes.domain.mappers.FunctionDataMapper;
import com.backpackcloud.sherlogholmes.domain.parsers.RegexDataParser;
import com.backpackcloud.sherlogholmes.impl.AttributeSpecImpl;
import com.backpackcloud.sherlogholmes.impl.DataModelImpl;
import com.backpackcloud.spectaculous.Backstage;
import com.backpackcloud.spectaculous.Operation;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.regex.Pattern;

public class RegexDataParserTest {

  private final String pattern = "(?<timestamp>\\d{2,4}-\\d{2}-\\d{2,4}T\\d{2}:\\d{2}:\\d{2}) (?<level>\\w+) \\[(?<category>[^]]+)] \\((?<origin>[^)]+)\\) (?<message>.+)";

  private final DataModel model = new DataModelImpl()
    .add("timestamp", new AttributeSpecImpl<>(AttributeType.datetime(), false))
    .add("level", new AttributeSpecImpl<>(AttributeType.enumOf("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"), false))
    .add("category", new AttributeSpecImpl<>(AttributeType.text(), false))
    .add("origin", new AttributeSpecImpl<>(AttributeType.text(), false))
    .add("message", new AttributeSpecImpl<>(AttributeType.text(), false));

  private final RegexDataParser parser = new RegexDataParser(Pattern.compile(pattern, Pattern.DOTALL));

  private final DataMapper<Function<String, String>> mapper = FunctionDataMapper.attributesFrom(model);

  private DataEntry entryFrom(String content) {
    Function<String, String> function = parser.parse(new Metadata("test", 1), content).orElseThrow();
    return mapper.map(model.dataSupplier(), function).orElseThrow();
  }

  @Test
  public void testSingleLineParsing() {
    String line = "2022-09-26T20:59:24 DEBUG [chat] (room-1702) chat bot is glorious, replying to Baylor Harris with an ASCII image of a horse";
    DataEntry data = entryFrom(line);
    Backstage.describe(DataEntry.class)
      .given(data)

      .from(attribute("level")).expect("DEBUG")
      .from(attribute("category")).expect("chat")
      .from(attribute("origin")).expect("room-1702")
      .from(attribute("message")).expect("chat bot is glorious, replying to Baylor Harris with an ASCII image of a horse")
      .from(attribute("timestamp")).expect(LocalDateTime.parse("2022-09-26T20:59:24"));
  }

  @Test
  public void testMultilineParsing() {
    String lines = "2022-09-26T20:59:24 DEBUG [chat] (room-1702) chat bot is glorious,\nreplying to Baylor Harris with\nan ASCII image of a horse";
    DataEntry data = entryFrom(lines);
    Backstage.describe(DataEntry.class)
      .given(data)

      .from(attribute("level")).expect("DEBUG")
      .from(attribute("category")).expect("chat")
      .from(attribute("origin")).expect("room-1702")
      .from(attribute("message")).expect("chat bot is glorious,\nreplying to Baylor Harris with\nan ASCII image of a horse")
      .from(attribute("timestamp")).expect(LocalDateTime.parse("2022-09-26T20:59:24"));
  }

  private static <E> Operation<DataEntry, E> attribute(String name) {
    return entry -> (E) entry.attribute(name).orElseThrow(UnbelievableException::new).value().orElse(null);
  }

}
