package com.backpackcloud.sherlogholmes.domain.chart;

import com.backpackcloud.sherlogholmes.domain.Attribute;
import com.backpackcloud.sherlogholmes.domain.DataEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChartBuilder<E> {

  private final List<Label> labels;

  public ChartBuilder(List<Label> labels) {
    this.labels = labels;
  }

  public Chart<E> build(Stream<DataEntry> data) {
    Map<Label, Long> collect = data.collect(
      Collectors.groupingBy(
        entry -> labels.stream().filter(label -> label.includes(entry)).findFirst().orElseThrow(),
        Collectors.counting()
      )
    );
  }


}
