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

package com.backpackcloud.sherlogholmes.commands.data;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.cli.Writer;
import com.backpackcloud.cli.annotations.Action;
import com.backpackcloud.cli.annotations.CommandDefinition;
import com.backpackcloud.cli.annotations.InputParameter;
import com.backpackcloud.cli.annotations.ParameterSuggestion;
import com.backpackcloud.cli.annotations.PreferenceValue;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.cli.ui.Theme;
import com.backpackcloud.cli.ui.components.FileSuggester;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.model.Attribute;
import com.backpackcloud.sherlogholmes.model.DataRegistry;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.List;

@CommandDefinition(
  name = "export",
  type = "Data Visualization",
  description = "Exports the current entries"
)
public class ExportDataCommand {

  private final Config config;
  private final DataRegistry registry;
  private final Terminal terminal;
  private final Theme theme;
  private final FileSuggester suggester;

  public ExportDataCommand(Config config, DataRegistry registry, Terminal terminal, Theme theme, FileSuggester suggester) {
    this.config = config;
    this.registry = registry;
    this.terminal = terminal;
    this.theme = theme;
    this.suggester = suggester;
  }

  @Action
  public void execute(@PreferenceValue("output-charset") String outputCharset,
                      @InputParameter String location,
                      @InputParameter String outputFormat) {
    Charset charset = Charset.forName(outputCharset);
    try {
      try (RandomAccessFile randomAccessFile = new RandomAccessFile(location, "rw");
           FileChannel channel = randomAccessFile.getChannel()) {
        StringBuilder output = new StringBuilder();
        Writer builderWriter = new Writer(
          theme,
          AttributedStyle.DEFAULT,
          AttributedString::new,
          output::append,
          terminal);
        registry.entries().forEach(entry -> {
          if (outputFormat == null) {
            String dataModelId = entry.attribute("data-model", String.class)
              .flatMap(Attribute::value)
              .orElseThrow(UnbelievableException.because("Entry has no data-model associated with"));
            String format = config.dataModel(dataModelId).exportFormat();
            entry.write(builderWriter, format);
          } else {
            entry.write(builderWriter, outputFormat);
          }
          output.append("\n");
        });
        byte[] line = output.toString().getBytes(charset);
        ByteBuffer buffer = ByteBuffer.allocate(line.length);
        buffer.put(line);
        buffer.flip();
        try {
          channel.write(buffer);
        } catch (IOException e) {
          throw new UnbelievableException(e);
        }
      }
    } catch (IOException e) {
      throw new UnbelievableException(e);
    }
  }

  @ParameterSuggestion(parameter = "location")
  public List<Suggestion> suggestLocation(@InputParameter String location) {
    return suggester.suggest(location);
  }

}
