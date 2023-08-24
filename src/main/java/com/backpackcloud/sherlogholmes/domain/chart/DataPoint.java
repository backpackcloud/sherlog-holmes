package com.backpackcloud.sherlogholmes.domain.chart;

public interface DataPoint<E> {

  Label label();

  E value();

}
