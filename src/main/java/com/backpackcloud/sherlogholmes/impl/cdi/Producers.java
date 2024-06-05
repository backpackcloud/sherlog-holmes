/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2022 Marcelo Guimarães
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.backpackcloud.sherlogholmes.impl.cdi;

import com.backpackcloud.UnbelievableException;
import com.backpackcloud.cli.preferences.UserPreferences;
import com.backpackcloud.cli.ui.Theme;
import com.backpackcloud.configuration.Configuration;
import com.backpackcloud.configuration.UserConfigurationLoader;
import com.backpackcloud.serializer.Serializer;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.domain.FilterFactory;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import java.io.File;

@ApplicationScoped
public class Producers {

  @ConfigProperty(name = "sherlog.config.file", defaultValue = "__default__")
  String configFile;

  @Singleton
  @Produces
  public Config getConfig(UserPreferences preferences,
                          Theme theme,
                          DataRegistry registry,
                          FilterFactory filterFactory) {
    Serializer serializer = Serializer.yaml();

    serializer.addDependency(UserPreferences.class, preferences);
    serializer.addDependency(Theme.class, theme);
    serializer.addDependency(DataRegistry.class, registry);
    serializer.addDependency(FilterFactory.class, filterFactory);

    if ("__default__".equals(configFile)) {
      return new UserConfigurationLoader("sherlog")
        .resolve()
        .map(Configuration::read)
        .map(config -> serializer.deserialize(config, Config.class))
        .orElseThrow(UnbelievableException.because("Configuration is not supplied"));
    }

    return serializer.deserialize(new File(configFile), Config.class);
  }

}
