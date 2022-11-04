# Sherlog Holmes

Sherlog Holmes is an application that helps you dig into log files. It offers a command line that enables
filtering and displaying entries, as well as a basic web chart for better visualization.

Sherlog Holmes aims to solve the problem of ad-hoc troubleshooting and analysis operations. If you have some
log files and need to get a better understanding of what's going on without having to toss them on indexing
services and visualization tools, then Sherlog Holmes will probably suit you well.

## Pre requisites

To use Sherlog Holmes, you will need:

- Linux or macOS (Windows users can use WSL with no issues)
- A terminal with a [Nerd Font](https://www.nerdfonts.com)

To build Sherlog Holmes, you will need:

- GraalVM 22.1 with Java 17
- Maven 3.8
- Patience (builds might take 3 minutes, depending on your hardware)

## How to build

Easier than falling off a skateboard... drunk... blindfolded...

```shell
mvn package
```

After the process, you should see a file `sherlog-holmes-runner` inside the `target` folder.

## How it works

Before allowing you to work with the log entries, Sherlog Holmes needs to pass them through a simple pipeline:

1- The data is read from the source
2- The read contents are parsed into a mid-layer structure representing each entry
3- Each structure is then mapped to the data structure Sherlog Holmes understands and can work with
4- Any additional process to the data structure are executed in order to enhance the data

## How to configure

Please take a look at the `examples` folder. It contains a dummy file explaining how to configure each step
of the pipeline, as well as some ready-to-use examples.

## How to use

Sherlog Holmes expects a configuration file, which can be:

- From the `SHERLOG_CONFIG_FILE` environment variable
- From the `sherlog.config.file` system property
- At `./sherlog.yml`
- At `$HOME/sherlog.yml`
- Passed via argument

### Inspecting Logs

The primary command for inspecting files is... `inspect`:

```shell
inspect <reader> <pipeline> <location>
```

Both `reader` and `pipeline` should be defined in the configuration file. The `location` might represent
different things depending on the reader. A `socket` reader expects it to be a port to receive the contents,
whereas a `file` reader expects a local file to read its contents.

Assuming you're inside the `examples/nexus` folder and started Sherlog Holmes from there, firing the following
command will parse all logs and provide you an overview in the prompt after the inspection:

```shell
inspect file log ./logs/*
```

If you want to have a clue about the exceptions present in the logs, you could run:

```shell
count exception
```

That should give you the following output:

```
java.lang.NoClassDefFoundError     3000  97.087%  11.676%
org.osgi.framework.BundleException   60   1.942%   0.234%
java.lang.NullPointerException       30   0.971%   0.117%
=   3090           12.026%
```

Now you know that our beloved NPE was found 30 times, to filter out only the entries holding such exception,
just run:

```shell
push exception == java.lang.NullPointerException
```

After that, the prompt remains the same. That's because we've just added a filter to the stack. Sherlog Holmes
uses a stack of filters that are combined to produce a single filter. Adding filters to the stack doesn't
reflect immediately on the entries. For that, you need to run the command `filter`.

After running that command, the prompt will be updated, showing that only 30 entries are now in the registry.
You can now browse them using the command `ls`.

Notice that the result will be paged, and the same icons found in the prompt are now displayed right next to
their correspondent attribute. Some icons are just a flag, like the bug present in the beginning of each entry.

Follow the arrows to navigate between the pages. You may also use the keys `r/p` to resume the listing or `q/c`
to quit the command.

To revert the changes to the registry, you can either:

- Run `pop` to remove top filter in the stack. Since we've only added one single filter, running
  this will leave no filter in the stack.
- Run `clear stack` to clear all the contents of the stack.

After that, remember to run `filter` to update the registry.

Back on the `inspect` command, it is possible to run it multiple times, even with different data models.
Let's try an example using a different source. Still in the current Sherlog Holmes session, run the following
command:

```shell
inspect socket audit 3000
```

That will cause Sherlog Holmes to open a socket on port `3000` and wait until a client connects. On another
terminal, navigate to the `examples/nexus/audit` folder and run:

```shell
nc localhost 3000 < *.log
```

After a couple of seconds, hit `CTRL+C` to exit `nc` and close the connection. You will notice that
the number of entries changed, and if you run `ls` to display the entries you will see different formats
in the output.

A nice usage for a socket reader is to redirect the output of a program directly to Sherlog Holmes. In that case,
you might also want to enable auto print for added entries with the following command:

```shell
preferences set show-added-entries true
```

### Filtering

Filters are a simple expression that analyses one attribute. The syntax is:

```shell
push <attribute> <operation> [parameter]
```

The supported operations are (you can use both names or symbols):

- `different` (`!=`)
- `greater_or_equal` (`>=`)
- `mismatches` (`!~=`)
- `equal` (`==`)
- `less` (`<`)
- `set` (`*`)
- `excludes` (`!%`)
- `less_or_equal` (`<=`)
- `unset` (`!`)
- `contains` (`%`)
- `greater` (`>`)
- `matches` (`~=`)

### Filter stack manipulation

It's possible to manipulate the filter stack by combining or transforming the top filters. The possible
manipulations are the following commands:

- `and`: Applies the boolean AND operation with the first two filters at the top of the stack
- `dup`: Duplicates the current filter
- `not`: Applies the boolean NOT operation to the top filter
- `or`: Applies the boolean OR operation with the first two filters at the top of the stack
- `pop`: Removes the current filter from the top of the stack

To view the stack, just use the command `stack`. A number indicating how many filters are present in the stack
is also shown in the prompt.

Now you could run the following commands to have a better view about how the stack works:

```shell
clear stack
push level == WARN
push exception unset
not
or
not
not
```

Notice how the stack changes after each command. All operations have a negative form, so negating them
produces simplified boolean expressions instead of a long and boring one.

All filters in the stack must match the entries. So, if you require a filter made only by `and` operations,
you might want to keep them individually in the stack.

