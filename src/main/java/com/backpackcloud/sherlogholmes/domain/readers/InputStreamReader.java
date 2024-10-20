package com.backpackcloud.sherlogholmes.domain.readers;

import com.backpackcloud.sherlogholmes.domain.DataReader;
import com.backpackcloud.sherlogholmes.domain.Metadata;

import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class InputStreamReader implements DataReader<InputStream> {

  private final Charset charset;

  public InputStreamReader(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void read(InputStream location, BiConsumer<Metadata, String> consumer) {
    BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(location, charset));
    AtomicInteger count = new AtomicInteger();

    reader.lines().forEach(line -> consumer.accept(new Metadata("input", count.getAndIncrement()), line));
  }

}
