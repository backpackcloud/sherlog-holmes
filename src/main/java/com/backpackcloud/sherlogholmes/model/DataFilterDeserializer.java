package com.backpackcloud.sherlogholmes.model;

import com.backpackcloud.UnbelievableException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataFilterDeserializer extends JsonDeserializer<DataFilter> {

  private final FilterFactory filterFactory;

  public DataFilterDeserializer() {
    this.filterFactory = new FilterFactory();
  }

  @Override
  public DataFilter deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    List<DataFilter> predicates = new ArrayList<>();

    JsonNode jsonNode = context.readTree(parser);

    if (jsonNode.isArray()) {
      for (JsonNode item : jsonNode) {
        predicates.add(filterFactory.create(item.at("").asText()));
      }
    } else {
      jsonNode.at("").asText().lines()
        .map(filterFactory::create)
        .forEach(predicates::add);
    }

    if (predicates.isEmpty()) {
      throw new UnbelievableException("Unable to create a Data Filter");
    }

    DataFilter first = predicates.removeFirst();

    return predicates.stream().reduce(first, DataFilter::and);
  }

}
