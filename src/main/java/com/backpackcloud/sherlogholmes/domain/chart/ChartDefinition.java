package com.backpackcloud.sherlogholmes.domain.chart;

import com.backpackcloud.sherlogholmes.domain.DataFilter;
import com.backpackcloud.sherlogholmes.domain.TimeUnit;

public record ChartDefinition(String config, DataFilter filter, TimeUnit unit, String field, String counter) {

}