package com.backpackcloud.sherlogholmes.domain.chart;

import java.util.List;

public interface Chart<E> {

  List<Label> labels();

  List<Series<E>> series();

  List<Series<E>> series(int maxSize);

}
