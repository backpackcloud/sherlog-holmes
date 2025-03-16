package com.backpackcloud.sherlogholmes.model.types;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.sherlogholmes.model.AttributeType;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlType implements AttributeType<URL> {

  @Override
  public String name() {
    return "url";
  }

  @Override
  public URL convert(String input) {
    try {
      return new URL(input);
    } catch (MalformedURLException e) {
      throw new UnbelievableException(e);
    }
  }

  @Override
  public int compare(URL o1, URL o2) {
    return o1.toString().compareTo(o2.toString());
  }

}
