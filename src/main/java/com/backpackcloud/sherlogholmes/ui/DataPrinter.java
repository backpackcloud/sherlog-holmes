package com.backpackcloud.sherlogholmes.ui;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.cli.Writer;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.model.Attribute;
import com.backpackcloud.sherlogholmes.model.DataEntry;

public class DataPrinter {

  private final Config config;

  public DataPrinter(Config config) {
    this.config = config;
  }

  public void print(Writer writer, DataEntry entry) {
    String dataModelId = entry.attribute("data-model", String.class)
      .flatMap(Attribute::value)
      .orElseThrow(UnbelievableException.because("Entry has no data-model associated with"));
    String format = config.dataModel(dataModelId).displayFormat();
    entry.write(writer, format);
  }

}
