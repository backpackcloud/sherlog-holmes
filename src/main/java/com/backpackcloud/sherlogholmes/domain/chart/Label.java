package com.backpackcloud.sherlogholmes.domain.chart;

import com.backpackcloud.sherlogholmes.domain.DataEntry;

public interface Label<E> {

  E value();

  boolean includes(DataEntry entry);

}
