package com.backpackcloud.sherlogholmes.model.steps;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexAnalyzer {

  private final Pattern pattern;

  public RegexAnalyzer(Pattern pattern) {
    this.pattern = pattern;
  }

  public Set<String> namedGroups() {
    Pattern groupPattern = Pattern.compile("\\?<(?<field>\\w+)>");
    Set<String> result = new HashSet<>();

    Matcher matcher = groupPattern.matcher(pattern.toString());
    while (matcher.find()) {
      result.add(matcher.group("field"));
    }

    return result;
  }

}