package com.backpackcloud.sherlogholmes.ui;

import com.backpackcloud.cli.CommandContext;
import com.backpackcloud.sherlogholmes.domain.FilterStack;
import io.quarkus.vertx.ConsumeEvent;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StackDisplay {

  private final FilterStack filterStack;

  public StackDisplay(FilterStack filterStack) {
    this.filterStack = filterStack;
  }

  @ConsumeEvent("command.stack")
  public void printStack(CommandContext context) {
    context.writer().writeln(filterStack);
  }

}
