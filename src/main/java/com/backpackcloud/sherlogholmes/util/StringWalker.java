package com.backpackcloud.sherlogholmes.util;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringWalker {

  public static final Pattern DEFAULT_INTERPOLATION_PATTERN = Pattern.compile("\\{(?<token>[^}]+)}");

  private final Pattern pattern;
  private final Consumer<String> textConsumer;
  private final Consumer<String> patternConsumer;

  public StringWalker(Consumer<String> textConsumer, Consumer<String> patternConsumer) {
    this(DEFAULT_INTERPOLATION_PATTERN, textConsumer, patternConsumer);
  }

  public StringWalker(Pattern pattern, Consumer<String> textConsumer, Consumer<String> patternConsumer) {
    this.pattern = pattern;
    this.textConsumer = textConsumer;
    this.patternConsumer = patternConsumer;
  }

  public void walk(String value) {
    if (value == null || value.isBlank()) return;

    Matcher matcher = pattern.matcher(value);

    int pos = 0;
    while (matcher.find()) {
      if (matcher.start() > pos) {
        textConsumer.accept(value.substring(pos, matcher.start()));
      }
      patternConsumer.accept(matcher.group("token"));
      pos = matcher.end();
    }

    if (pos < value.length()) {
      textConsumer.accept(value.substring(pos));
    }
  }

}