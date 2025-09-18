# Sherlog Holmes

Sherlog Holmes is an application that helps you dig into log files. It offers a command line that enables filtering and
displaying entries, as well as a basic exporting functions and listening.

Sherlog Holmes aims to solve the problem of ad-hoc troubleshooting and analysis operations. If you have some log files
and need to get a better understanding of what's going on without having to toss them on indexing services and
visualization tools, then Sherlog Holmes will probably suit you well.

## Pre requisites

To use Sherlog Holmes, you will need:

- Java 21
- A terminal with a [Nerd Font](https://www.nerdfonts.com)

To build Sherlog Holmes, you will need:

- Java 21
- Maven 3.9+

## How to build

Easier than falling off a skateboard... drunk... blindfolded...

```shell
mvn package
```

After the process, you should see a file `sherlog-jar-with-dependencies.jar` inside the `target` folder. Just run it
with your `java -jar` command and you're good to go.

## How it works

Before allowing you to work with the log entries, Sherlog Holmes needs to pass them through a simple pipeline:

1- The data is read from the source
2- The read contents are parsed into the data structure Sherlog Holmes understands and can work with
3- Executes any additional process to enhance the data

## How to configure

The configuration file may contain the following sections.

### Preferences

Under the key `preferences`, you set how Sherlog will behave, the preferences are:

```yaml
preferences:
  # Removes the ANSI colors from the content before parsing it
  remove-ansi-colors: true
  # The charset to use for reading the inputs
  input-charset:      UTF-8
  # The charset to use for writing
  output-charset:     UTF-8
  # Enables data paging for commands that displays data
  result-paging:      true
  # Sets the number of entries shown on each page
  results-per-page:   15
  # Clears the console before changing pages
  clear-on-paging:    true
```

For a list of all the preferences and their current value, use the command `preferences list`.

### Models

Under the key `models`, you set the data models that Sherlog will use. Each entry needs to match a data model structure.

```yaml
models:
  # all data models should have an id for reference
  java:
    # Defines how this data model will be rendered in the command line
    # Attributes should be enclosed with "{ }", with the optional features as follows:
    #
    # - '#' uses the attribute's value and its correspondent icon
    # - '@' uses just the attribute's icon (if the attribute holds a value)
    # - '|' passes the attribute's value through a String.format() call using the given arguments in the right
    format:        '{@exception}{#$source}:{$line} {#timestamp} {#level|%-5s} {#category} {#thread} {#message}'
    export-format: '{timestamp} {level|%-5s} [{category}] ({thread}) {message}'
    # The list of attributes for this data model
    #
    # Attributes can be of the following types:
    # - time:            a java LocalTime (requires format configuration)
    # - datetime:        a java LocalDateTime (requires format configuration)
    # - zoned-datetime:  a java ZonedDateTime (requires format configuration)
    # - offset-datetime: a java OffsetDateTime (requires format configuration)
    # - text:            a java String
    # - number:          a java Integer
    # - decimal:         a java Double
    # - flag:            a java Boolean
    # - enum:            a set of possible String values
    #
    # Attributes might have multiple values (indicated with an '[]' after the type), which can be useful in situations
    # where you need a collection of values like exceptions that are shown in a stacktrace.
    #
    # For attributes requiring configuration, it's passed with an '|' after the type, followed by the configuration
    #
    # The order in which the attributes are declared is also the order that will be used to sort the entries. Attributes
    # from the metadata ($line and $source) will always be added, but without declaring, they will fall into the last
    # positions.
    attributes:
      # The console log often doesn't have full timestamps
      timestamp:
        - datetime | yyyy-MM-dd HH:mm:ss,SSS
        - time     | HH:mm:ss,SSS
      level:     enum | TRACE,DEBUG,FINE,INFO,WARN,WARNING,ERROR,SEVERE,FATAL
      category:  text
      thread:    text
      message:   text
      # One entry might have multiple exceptions
      exception: text[]
```

### Counters

Under the key `counters`, you define which attributes (from any model) Sherlog will keep a counter for each value
inspected. The memory impact of adding a counter would depend on how many different values were assigned to that
attribute.

```yaml
counters:
  - source
  - level
  - category
  - thread
  - exception
```

When an attribute is added to the counter, its count can appear in the prompt if the attribute has an icon assigned to
it. The counters are also used by the command `count` as a cache.

### Parsers

Under the key `parsers`, you define how to parse an input. There are 4 different ways of parsing an input:

- `csv`: parses each line as a CSV row
- `json`: parses each line as a JSON data
- `split`: splits each line using a specific regular expression
- `regex`: uses a regular expression to capture the data via its defined named capture groups

```yaml
parsers:
  java:
    # A regex parser exposes the named groups found in the pattern
    type:    regex
    # You can reuse patterns across the configuration by enclosing the names with '{{ }}'
    # the names enclosed must be defined in the 'patterns' section
    pattern: '{{ timestamp }} {{ level }}\s+{{ category }} {{ thread }} {{ message }}'
  infinispan:
    type:    regex
    pattern: '{{ timestamp }} {{ level }}\s+{{ thread }} {{ category }} {{ message }}'
```

Notice from the example the appearance of names around `{{` and `}}`. These are a reference to external defined patterns
that can be reused to compose the expressions. The external patterns are defined in its own key `patterns`:

```yaml
patterns:
  timestamp: '(?<$timestamp>(\\d{2,4}-\\d{2}-\\d{2,4} )?\\d{2}:\\d{2}:\\d{2},\\d{3})'
  level:     '(?<level>\\w+)'
  category:  '\\[(?<category>[^]]+]*)]'
  thread:    '\\((?<thread>[^)]+\\)*)\\)'
  message:   '(?<message>.+)'
```

Notice also how the `timestamp` capture group can capture both types defined in the `java` model above.

## How to use

Sherlog Holmes expects configuration files. Use the option `-c` to add a configuration file. Multiple files can be
added, and their configuration will be merged.

There's a default configuration, which is included by default if none is given or if you pass the plag `-d`, useful in
case you want to extend it. The default configuration includes the built-in one plus the following (if present):

- At `./sherlog.yml` (current working directory)
- At `$HOME/sherlog.yml`

If no configuration is supplied, the default ones are used by default.

### Inspecting Logs

The primary command for inspecting files is... `inspect`:

```shell
inspect <pipeline> <log-file>
```

The `pipeline` should be defined in the configuration file. The `location` might represent
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

