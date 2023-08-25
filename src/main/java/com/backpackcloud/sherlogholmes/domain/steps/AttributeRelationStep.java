package com.backpackcloud.sherlogholmes.domain.steps;

import com.backpackcloud.sherlogholmes.domain.Attribute;
import com.backpackcloud.sherlogholmes.domain.DataEntry;
import com.backpackcloud.sherlogholmes.domain.PipelineStep;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Map;

@RegisterForReflection
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
