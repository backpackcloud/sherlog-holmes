package com.backpackcloud.sherlogholmes.model.steps;

import com.backpackcloud.sherlogholmes.model.Attribute;
import com.backpackcloud.sherlogholmes.model.DataEntry;
import com.backpackcloud.sherlogholmes.model.PipelineStep;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class AttributeRelationStep implements PipelineStep {

  private final String sourceAttribute;
  private final String targetAttribute;
  private final Map<String, String> assignMap;

  public AttributeRelationStep(@JsonProperty("source") String sourceAttribute,
                               @JsonProperty("target") String targetAttribute,
                               @JsonProperty("relation") Map<String, String> assignMap) {
    this.sourceAttribute = sourceAttribute;
    this.targetAttribute = targetAttribute;
    this.assignMap = assignMap;
  }

  @Override
  public void analyze(DataEntry dataEntry) {
    dataEntry.attribute(sourceAttribute)
      .flatMap(Attribute::formattedValue)
      .filter(assignMap::containsKey)
      .map(assignMap::get)
      .ifPresent(value -> dataEntry.attribute(targetAttribute)
        .ifPresent(target -> target.assignFromInput(value)));
  }

}
