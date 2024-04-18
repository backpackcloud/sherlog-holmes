package com.backpackcloud.sherlogholmes.domain.types;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.sherlogholmes.domain.AttributeType;

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
