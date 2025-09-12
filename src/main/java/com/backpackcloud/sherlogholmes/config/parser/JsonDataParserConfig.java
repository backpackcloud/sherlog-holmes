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

package com.backpackcloud.sherlogholmes.config.parser;

import com.backpackcloud.io.SerialBitter;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.model.DataParser;
import com.backpackcloud.sherlogholmes.model.parsers.JsonDataParser;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class JsonDataParserConfig implements DataParserConfig {

  private final SerialBitter serialBitter;
  private final Map<String, String> attributeMappings;

  public JsonDataParserConfig(@JsonProperty("attributes") Map<String, String> attributeMappings) {
    this.attributeMappings = attributeMappings;
    this.serialBitter = SerialBitter.JSON();
  }


  @Override
  public DataParser get(Config config) {
    return new JsonDataParser(serialBitter, attributeMappings);
  }

}
