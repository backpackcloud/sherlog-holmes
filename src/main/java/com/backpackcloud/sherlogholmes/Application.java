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

package com.backpackcloud.sherlogholmes;

import com.backpackcloud.cli.CLI;
import com.backpackcloud.cli.Segments;
import com.backpackcloud.sherlogholmes.config.Config;
import io.quarkus.runtime.QuarkusApplication;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class Application implements QuarkusApplication {

  @Inject
  @Segments(left = {
    "data-count",
    "data-range",
    "filter-stack",
    "chart-count",
    "close",
    "new-line",
    "prompt"
  })
  CLI cli;

  @Inject
  Config config;

  @Override
  public int run(String... args) {
    List<String> commands = new ArrayList<>(config.commands());
    commands.addAll(List.of(args));
    config.macros().forEach(cli::registerMacro);
    cli.execute(commands.toArray(new String[config.commands().size() + args.length]));
    cli.start();
    return 0;
  }

}
