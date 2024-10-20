package com.backpackcloud.sherlogholmes.commands.data;

import com.backpackcloud.cli.Action;
import com.backpackcloud.cli.AnnotatedCommand;
import com.backpackcloud.cli.CommandDefinition;
import com.backpackcloud.cli.PreferenceValue;
import com.backpackcloud.cli.Suggestions;
import com.backpackcloud.cli.Writer;
import com.backpackcloud.cli.ui.Suggestion;
import com.backpackcloud.cli.ui.impl.PromptSuggestion;
import com.backpackcloud.sherlogholmes.config.Config;
import com.backpackcloud.sherlogholmes.domain.DataReader;
import com.backpackcloud.sherlogholmes.domain.DataRegistry;
import com.backpackcloud.sherlogholmes.domain.Pipeline;
import com.backpackcloud.sherlogholmes.domain.readers.InputStreamReader;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@CommandDefinition(
  name = "read",
  description = "Reads the input stream and parses the entries",
  type = "Data"
)
@ApplicationScoped
@RegisterForReflection
public class ReadInputCommand implements AnnotatedCommand {

  private final ExecutorService executorService;

  private final DataRegistry registry;
  private final Config config;

  public ReadInputCommand(DataRegistry registry, Config config) {
    this.registry = registry;
    this.config = config;
    this.executorService = Executors.newCachedThreadPool();
  }

  @Action
  public void execute(@PreferenceValue("input-charset") String inputCharset,
                      Writer writer,
                      String pipelineId) {
    DataReader<InputStream> dataReader = new InputStreamReader(Charset.forName(inputCharset));
    Pipeline pipeline = config.pipelineFor(pipelineId);

    executorService.submit(() ->
      pipeline.run(dataReader, System.in, entry -> {
        writer.writeln(entry);
        registry.add(entry);
      })
    );
  }

  @Suggestions
  public List<Suggestion> execute(String pipelineId, String location) {
    if (location == null) {
      return config.pipelines().keySet()
        .stream().map(PromptSuggestion::suggest)
        .collect(Collectors.toList());
    }
    return Collections.emptyList();
  }


}
