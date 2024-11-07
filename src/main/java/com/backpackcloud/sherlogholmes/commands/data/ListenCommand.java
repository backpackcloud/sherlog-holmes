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
import com.backpackcloud.sherlogholmes.domain.readers.SocketDataReader;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@CommandDefinition(
  name = "listen",
  description = "Listen for log entries arriving on a given port",
  type = "Data"
)
@ApplicationScoped
@RegisterForReflection
public class ListenCommand implements AnnotatedCommand {

  private final ExecutorService executorService;

  private final DataRegistry registry;
  private final Config config;

  public ListenCommand(DataRegistry registry, Config config) {
    this.registry = registry;
    this.config = config;
    this.executorService = Executors.newCachedThreadPool();
  }

  @Action
  public void execute(@PreferenceValue("input-charset") String inputCharset,
                      Writer writer,
                      String pipelineId,
                      String portsInput) {
    List<Integer> ports = new ArrayList<>();

    String[] split = portsInput.split(",");
    for (String segment : split) {
      String[] range = segment.split("-", 2);
      int from = Integer.parseInt(range[0]);
      int to = range.length == 1 ? from : Integer.parseInt(range[1]);

      while(from <= to) {
        ports.add(from++);
      }
    }

    DataReader<Integer> dataReader = new SocketDataReader(Charset.forName(inputCharset));
    Pipeline pipeline = config.pipelineFor(pipelineId);

    ports.stream()
      .map(port -> (Runnable) () -> pipeline.run(dataReader, port, entry -> {
        registry.add(entry);
        writer.writeln(entry);
      }))
      .forEach(this.executorService::submit);
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
