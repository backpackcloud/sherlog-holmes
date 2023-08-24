package com.backpackcloud.sherlogholmes.domain.chart;

import java.util.List;

public interface Series<E> {

  List<Label> labels();

  List<DataPoint<E>> data();

}
