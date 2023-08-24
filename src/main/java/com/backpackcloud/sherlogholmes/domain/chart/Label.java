package com.backpackcloud.sherlogholmes.domain.chart;

import com.backpackcloud.sherlogholmes.domain.DataEntry;

public interface Label {

  String name();

  boolean includes(DataEntry entry);

}
