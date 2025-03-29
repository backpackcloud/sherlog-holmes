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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.function.BiConsumer;

public class SocketDataReader implements DataReader<Integer> {

  private final Charset charset;

  public SocketDataReader(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void read(Integer port, BiConsumer<Metadata, String> consumer) {
    try (
      ServerSocket serverSocket = new ServerSocket(port);
      Socket socket = serverSocket.accept();
      InputStream inputStream = socket.getInputStream()
    ) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, charset));
      int count = 0;
      String line;
      while ((line = reader.readLine()) != null) {
        consumer.accept(new Metadata("socket-" + port, ++count), line);
      }
      reader.close();
    } catch (IOException e) {
      throw new UnbelievableException(e);
    }
  }

}
