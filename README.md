# Sherlog Holmes

*Less data containing useful information is way better than lots of data containing a mess.*

Don't you hate those thousands of lines in a log blowing up with your troubleshooting? Lots of useless data that you
have to filter just to turn that 300 MB of madness into a 30 KB of useful information? If you need something that can
rip off useless entries so you can have a clue about what is going on with that application, you should give Sherlog
Holmes a try.

If you are a log detective, then Sherlog Holmes is your best companion!

## How it works

Sherlog works by grabbing every line of an input and applying a Regular Expression to create an `entry`. This `entry`
will be filtered based on a set of given rules and, if it is accepted, will be passed to a set of defined processors so
they can do something useful (like printing the output so you can redirect it to a sane log file).

The attributes are based on the named capture groups:

- Time: `time`
- Level: `level`
- Category: `category`
- Origin: `origin`
- Message: `message`

```regexp
(?P<level>\w+)\s(?P<category>\s+)\s(?P<message>.+)
```

Patterns for exception and stacktrace should be defined separately. The exception pattern is used only in the message
field and stacktrace. Here is a complete example of a pattern configuration:

```yaml
wildfly:
  entry: (?P<time>[0-9,.:]+)\s+(?P<level>\w+)\s+\[(?P<category>\S+)\]\s\((?P<origin>[^)]+)\)?\s?(?P<message>.+)
  exception: (?P<exception>\w+(\.\w+)+(Exception|Error))
  stacktrace: ^(\s+at)|(Caused by\:)|(\s+\.{3}\s\d+\smore)
```

The configuration should contain a unique id and at least a pattern for the log **entry**. Place you configuration file
in a `*.yml` file inside your `$HOME/.sherlog-holmes/patterns` directory and you're ready to go!

### Configuration Inheritance

You can create a base configuration and then override some values to create another one. In this case, you need to
specify the parent configuration with the `from` key:

```yaml
base.java:
  exception: (?P<exception>\w+(\.\w+)+(Exception|Error))
  stacktrace: ^(\s+at)|(Caused by\:)|(\s+\.{3}\s\d+\smore)
wildfly:
  from: base.java
  entry: (?P<time>[0-9,.:]+)\s+(?P<level>\w+)\s+\[(?P<category>\S+)\]\s\((?P<origin>[^)]+)\)?\s?(?P<message>.+)
```

## Usage

Shelog Holmes provides the command line tool `sherlog-holmes`. This tool expects a command and a set of arguments. Each
command can receive a filter to reduce the log entries. The commands are:

- `print`: prints the entries that passes the filter (prints everything if no filter is given)
- `count`: counts the occurrences of the attributes of the filtered entries in order to have a macro view of the log

You can use `sherlog-holmes help <command>` to see the list of available options for each command. Bellow are some
examples of use:

`sherlog-holmes print --layout wildfly --level ERROR server.log`

This is a simple command that will print any `ERROR` message in the `server.log` file. In case you need more that one
clause in the filter, just append another option:

`sherlog-holmes print --layout wildfly --level ERROR --exception java.lang.NullPointerException server.log`

This will print any entry that contains a `java.lang.NullPointerException` and is also an `ERROR` entry. If you want to
use the `or` clause, just append a `--or` before the filter clause:

`sherlog-holmes print --layout wildfly --level ERROR --or --exception java.lang.NullPointerException server.log`

This will print any entry that contains a `java.lang.NullPointerException` or is an `ERROR` entry. You can also specify
how the matching will be considered:

`sherlog-holmes print --layout wildfly --contains --message "Hi there!" server.log`

This will print any entry that contains `Hi there` in its message. To supply a regular expression, use the `--matches`
instead:

`sherlog-holmes print --layout wildfly --matches --message "\d{10}" server.log`

If you need to change the output format, use the `--format` option:

`sherlog-holmes print --layout wildfly --format "{{.Line}}: {{.RawContent}}" server.log`

This will print all entries appending the line number before each one. The following attributes can be used:

- `Line`: the line number
- `Time`: the timestamp
- `Level`: the log level
- `Category`: the category
- `Origin`: the origin
- `Exceptions`: the array containing all exceptions found
- `Stacktrace`: a string containing the stacktrace
- `RawContent`: the raw entry

Remember that the format is a Go Template.

## Built-in Patterns

Currently, Sherlog Holmes has the following built-in patterns:

- `base.java`: base pattern for Java outputs (contains patterns for exceptions and stacktraces only)
- `wildfly`: matches Wildfly | EAP logs
- `jboss-eap`: alias for `wildfly`

## License

The gem is available as open source under the terms of the [MIT License](http://opensource.org/licenses/MIT).
