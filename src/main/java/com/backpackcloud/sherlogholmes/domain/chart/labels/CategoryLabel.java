package com.backpackcloud.sherlogholmes.domain.chart.labels;

import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.chart.Label;

import java.util.Objects;

public class CategoryLabel implements Label<String> {

  private final String category;
  private final String value;

  public CategoryLabel(String category, String value) {
    this.category = category;
    this.value = value;
  }

  @Override
  public String value() {
    return value;
  }

  @Override
  public boolean includes(DataEntry entry) {
    return value.equals(entry.valueOf(category, String.class));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CategoryLabel that = (CategoryLabel) o;
    return Objects.equals(category, that.category) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, value);
  }

}
