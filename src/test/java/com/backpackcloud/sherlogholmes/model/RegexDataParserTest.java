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

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.sherlogholmes.model.parsers.RegexDataParser;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegexDataParserTest {

  private final String pattern = "(?<timestamp>\\d{2,4}-\\d{2}-\\d{2,4}T\\d{2}:\\d{2}:\\d{2}) (?<level>\\w+) \\[(?<category>[^]]+)] \\((?<origin>[^)]+)\\) (?<message>.+)";

  private final DataModel model = new DataModel()
    .add("timestamp", new AttributeSpec<>(AttributeType.datetime(), false))
    .add("level", new AttributeSpec<>(AttributeType.enumOf("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL"), false))
    .add("category", new AttributeSpec<>(AttributeType.text(), false))
    .add("origin", new AttributeSpec<>(AttributeType.text(), false))
    .add("message", new AttributeSpec<>(AttributeType.text(), false));

  private final RegexDataParser parser = new RegexDataParser(model, Pattern.compile(pattern, Pattern.DOTALL), true);

  @Test
  public void testSingleLineParsing() {
    entryFrom("2022-09-26T20:59:24 DEBUG [chat] (room-1702) chat bot is glorious, replying to Baylor Harris with an ASCII image of a horse");

    assertEquals(("DEBUG"), attribute("level"));
    assertEquals(("chat"), attribute("category"));
    assertEquals(("room-1702"), attribute("origin"));
    assertEquals(("chat bot is glorious, replying to Baylor Harris with an ASCII image of a horse"), attribute("message"));
    assertEquals((LocalDateTime.parse("2022-09-26T20:59:24")), attribute("timestamp"));
  }

  @Test
  public void testMultilineParsing() {
    entryFrom("2022-09-26T20:59:24 DEBUG [chat] (room-1702) chat bot is glorious,\nreplying to Baylor Harris with\nan ASCII image of a horse");

    assertEquals(("DEBUG"), attribute("level"));
    assertEquals(("chat"), attribute("category"));
    assertEquals(("room-1702"), attribute("origin"));
    assertEquals(("chat bot is glorious,\nreplying to Baylor Harris with\nan ASCII image of a horse"), attribute("message"));
    assertEquals((LocalDateTime.parse("2022-09-26T20:59:24")), attribute("timestamp"));
  }

  DataEntry data;

  private void entryFrom(String content) {
    Metadata metadata = new Metadata("test", 1);
    data = parser.parse(metadata, content).orElseThrow();
  }

  private Object attribute(String name) {
    return data.attribute(name).orElseThrow(UnbelievableException::new).value().orElse(null);
  }

}
