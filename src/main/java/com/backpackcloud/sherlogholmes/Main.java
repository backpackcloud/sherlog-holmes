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

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@QuarkusMain
public class Main {

  public static void main(String[] args) throws IOException {
    List<String> cliArgs = new ArrayList<>();
    boolean setCliArgs = false;

    for (String arg : args) {
      if ("--".equals(arg)) {
        setCliArgs = true;
      } else if (setCliArgs) {
        cliArgs.add(arg);
      } else {
        System.setProperty("sherlog.config.file", arg);
      }
    }

    System.out.println(new String(Main.class.getResourceAsStream("/META-INF/banner.txt").readAllBytes()));

    Quarkus.run(Application.class, cliArgs.toArray(new String[0]));
    Quarkus.waitForExit();
  }

}
