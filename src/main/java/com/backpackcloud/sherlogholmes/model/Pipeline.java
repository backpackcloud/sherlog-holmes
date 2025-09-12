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
import com.backpackcloud.preferences.UserPreferences;
import com.backpackcloud.sherlogholmes.Preferences;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Pipeline {

  private final DataModel dataModel;
  private final DataParser dataParser;
  private final List<PipelineStep> analysisSteps;
  private final UserPreferences preferences;

  public Pipeline(DataModel dataModel,
                  DataParser dataParser,
                  List<PipelineStep> analysisSteps,
                  UserPreferences preferences) {
    this.dataModel = dataModel;
    this.dataParser = dataParser;
    this.analysisSteps = analysisSteps;
    this.preferences = preferences;
  }

  private String normalize(String content) {
    if (preferences.isEnabled(Preferences.REMOVE_ANSI_COLORS)) {
      return content.replaceAll("\\x1B(?:[@-Z\\\\-_]|\\[[0-?]*[ -/]*[@-~])", "");
    }
    return content;
  }

  public <T> void run(DataReader<T> dataReader, T location, Consumer<DataEntry> consumer) {
    StagingArea stagingArea = new StagingArea(consumer);

    dataReader.read(location, (metadata, content) ->
    {
      Supplier<DataEntry> supplier = () -> {
        DataEntry entry = dataModel.dataSupplier().get();
        metadata.attachTo(entry);
        return entry;
      };
      dataParser.parse(supplier, metadata, normalize(content))
        .ifPresentOrElse(
          stagingArea::push,
          () -> stagingArea.push(content)
        );
    });

    stagingArea.push();
    stagingArea.close();
  }

  private class StagingArea {

    private StringBuilder extraLines;
    private DataEntry entry;
    private final Consumer<DataEntry> consumer;
    private final ExecutorService pusher;
    private final ExecutorService pushChecker;
    private boolean closed = false;
    private long lastPush;

    private StagingArea(Consumer<DataEntry> consumer) {
      this.consumer = consumer;
      this.pusher = Executors.newVirtualThreadPerTaskExecutor();
      this.pushChecker = Executors.newSingleThreadExecutor();
      this.pushChecker.submit(() -> {
        while (!closed) {
          if (System.currentTimeMillis() - lastPush >= 1000) {
            push();
          }
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            throw new UnbelievableException(e);
          }
        }
      });
    }

    public void close() {
      this.pusher.shutdown();
      try {
        this.pusher.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        throw new UnbelievableException(e);
      }
      this.closed = true;
      this.pushChecker.close();
    }

    public void push() {
      if (this.entry != null) {
        DataEntry struct = this.entry;
        if (!analysisSteps.isEmpty()) {
          pusher.execute(() -> {
            analysisSteps.forEach(step -> step.analyze(struct));
            consumer.accept(struct);
          });
        }
        this.entry = null;
        this.extraLines = null;
        this.lastPush = System.currentTimeMillis();
      }
    }

    public void push(String content) {
      if (this.extraLines == null) {
        this.extraLines = new StringBuilder(content);
      } else {
        this.extraLines.append("\n").append(content);
      }
    }

    public void push(DataEntry dataEntry) {
      if (this.entry != null && this.extraLines != null) {
        if (this.entry.hasAttribute("message")) {
          Attribute<String> message = this.entry.attribute("message", String.class).get();
          message.value().ifPresent(value -> this.extraLines.insert(0, "\n").insert(0, value));
          message.assignFromInput(this.extraLines.toString());
        }
      }
      push();
      this.entry = dataEntry;
    }

  }

}
