package com.backpackcloud.sherlogholmes.domain.chart;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.sherlogholmes.domain.DataEntry;

public interface Label<E> extends Comparable<Label<E>> {

  E value();

  boolean includes(DataEntry entry);

  @Override
  default int compareTo(Label<E> o) {
    E thisValue = value();
    E thatValue = o.value();
    if (thisValue instanceof Comparable a && thatValue instanceof Comparable b) {
      return a.compareTo(b);
    }
    throw new UnbelievableException("Can't compare labels");
  }

}
