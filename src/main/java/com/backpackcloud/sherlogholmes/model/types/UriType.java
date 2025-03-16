package com.backpackcloud.sherlogholmes.model.types;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.sherlogholmes.model.AttributeType;

import java.net.URI;
import java.net.URISyntaxException;

public class UriType implements AttributeType<URI> {

  @Override
  public String name() {
    return "uri";
  }

  @Override
  public URI convert(String input) {
    try {
      return new URI(input);
    } catch (URISyntaxException e) {
      throw new UnbelievableException(e);
    }
  }

}
