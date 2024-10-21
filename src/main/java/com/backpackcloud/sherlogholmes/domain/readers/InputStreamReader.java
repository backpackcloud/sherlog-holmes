package com.backpackcloud.sherlogholmes.domain.readers;

import com.backpackcloud.sherlogholmes.domain.DataReader;
import com.backpackcloud.sherlogholmes.domain.Metadata;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

public class InputStreamReader implements DataReader<InputStream> {

  private final Charset charset;

  public InputStreamReader(Charset charset) {
    this.charset = charset;
  }

  @Override
  public void read(InputStream location, BiConsumer<Metadata, String> consumer) {
    Scanner scanner = new Scanner(location, charset);
    AtomicInteger count = new AtomicInteger();

    while(scanner.hasNext()) {
      consumer.accept(new Metadata("input", count.getAndIncrement()), scanner.next());
    }
  }

}
