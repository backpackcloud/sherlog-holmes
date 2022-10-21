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
import com.opencsv.CSVReader;

import java.io.FileReader;
import java.util.function.Consumer;

public class CsvDataReader implements DataReader<String[]> {

  private final boolean skipFirstLine;

  public CsvDataReader(boolean skipFirstLine) {
    this.skipFirstLine = skipFirstLine;
  }

  @Override
  public void read(String location, DataParser<String[]> parser, Consumer<DataEntry> consumer) {
    try {
      FileReader fileReader = new FileReader(location);
      CSVReader csvReader = new CSVReader(fileReader);

      int count = skipFirstLine ? 0 : 1;

      String[] nextRecord;

      while ((nextRecord = csvReader.readNext()) != null) {
        // assume first line is the header
        if (count++ > 0) {
          parser.parse(nextRecord).ifPresent(consumer);
        }
      }
    } catch (Exception e) {
      throw new UnbelievableException(e);
    }
  }

}
