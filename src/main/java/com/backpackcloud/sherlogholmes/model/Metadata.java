package com.backpackcloud.sherlogholmes.model;

public record Metadata(String source, int line) {

  public void attachTo(DataEntry entry) {
    entry.addAttribute("$source", source);
    entry.addAttribute("$line", line);
  }

}
