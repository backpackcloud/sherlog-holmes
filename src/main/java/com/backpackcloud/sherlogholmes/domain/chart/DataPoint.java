package com.backpackcloud.sherlogholmes.domain.chart;

public interface DataPoint<X, Y> {

  Label<X> label();

  Y value();

}
