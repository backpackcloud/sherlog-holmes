package com.backpackcloud.sherlogholmes.domain.chart;

import java.util.List;

public interface Chart<X, Y> {

  List<Label<X>> labels();

  List<Series<X, Y>> series();

  List<Series<X, Y>> series(int maxSize);

}
