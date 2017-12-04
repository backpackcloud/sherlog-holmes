package cli

import (
	"github.com/urfave/cli"

	"github.com/devnull-tools/sherlog-holmes/commands"
	"github.com/devnull-tools/sherlog-holmes/domain"
	"github.com/devnull-tools/sherlog-holmes/filters"
	"github.com/devnull-tools/sherlog-holmes/mappers"
	"github.com/devnull-tools/sherlog-holmes/processors"

	"os"
)

var VERSION = "0.7.0"

// A flag that creates the filter based on the user inputs
type FilterFlag struct {
	Attribute filters.Extractor
	Operation filters.Operation
	Value     string
}

func (flag *FilterFlag) String() string {
	return flag.Value
}

func (flag *FilterFlag) Set(string string) error {
	if useMatches {
		flag.Operation = filters.Matches
		useMatches = false
	} else if useContains {
		flag.Operation = filters.Contains
		useContains = false
	}
	f := flag.Operation(string, flag.Attribute)
	if negate {
		f = filters.Negate(f)
		negate = false
	}
	if filter == nil {
		filter = f
	} else {
		if orOperation {
			filter = filters.Or(filter, f)
			orOperation = false
		} else {
			filter = filters.And(filter, f)
		}
	}
	return nil
}

var layout string
var formatOutput string
var maxEntries int64
var configFile string
var filter func(entry *domain.Entry) bool
var orOperation = false
var useMatches = false
var useContains = false
var negate = false

func Execute() {
	app := cli.NewApp()
	app.Name = "sherlog-holmes"
	app.Usage = "Sanitize your log files"

	app.Version = VERSION

	configFlag := cli.StringFlag{
		Name:        "config-file, c",
		Usage:       "loads pattern configurations from `FILE`",
		Destination: &configFile,
	}
	layoutFlag := cli.StringFlag{
		Name:        "layout, l",
		Usage:       "sets the layout for mapping log entries",
		Destination: &layout,
	}
	formatFlag := cli.StringFlag{
		Name:        "format, o",
		Usage:       "sets the format output",
		Value:       processors.FORMAT_RAW,
		Destination: &formatOutput,
	}
	maxFlag := cli.Int64Flag{
		Name:        "max, m",
		Usage:       "sets the maximum number of filtered entries",
		Value:       -1,
		Destination: &maxEntries,
	}
	stacktraceSearchFlag := cli.BoolFlag{
		Name:        "search-stacktrace, s",
		Usage:       "Searchs for exceptions on stacktrace elements",
		EnvVar:      "SHERLOG_SEARCH_STACKTRACE",
		Destination: &mappers.FindExceptionsOnStacktrace,
	}
	levelFilterFlag := cli.GenericFlag{
		Name:  "level",
		Usage: "sets a level filter",
		Value: &FilterFlag{Attribute: filters.Level, Operation: filters.Equals},
	}
	categoryFilterFlag := cli.GenericFlag{
		Name:  "category",
		Usage: "sets a category filter",
		Value: &FilterFlag{Attribute: filters.Category, Operation: filters.Equals},
	}
	originFilterFlag := cli.GenericFlag{
		Name:  "origin",
		Usage: "sets an origin filter",
		Value: &FilterFlag{Attribute: filters.Category, Operation: filters.Equals},
	}
	messageFilterFlag := cli.GenericFlag{
		Name:  "message",
		Usage: "sets a message filter",
		Value: &FilterFlag{Attribute: filters.Message, Operation: filters.Equals},
	}
	stacktraceFilterFlag := cli.GenericFlag{
		Name:  "stacktrace",
		Usage: "sets a stacktrace filter",
		Value: &FilterFlag{Attribute: filters.Stacktrace, Operation: filters.Contains},
	}
	exceptionFilterFlag := cli.GenericFlag{
		Name:  "exception",
		Usage: "sets an exception filter",
		Value: &FilterFlag{Attribute: filters.Exception, Operation: filters.Equals},
	}
	matchOperationFlag := cli.BoolFlag{
		Name:        "matches",
		Usage:       "sets the filter value to be a regular expression match",
		Destination: &useMatches,
	}
	containsOperationFlag := cli.BoolFlag{
		Name:        "contains",
		Usage:       "sets the filter value to be a contains match",
		Destination: &useContains,
	}
	logicOrFlag := cli.BoolFlag{
		Name:        "or",
		Usage:       "use OR logic operator with the next filters",
		Destination: &orOperation,
	}
	logicNotFlag := cli.BoolFlag{
		Name:        "not",
		Usage:       "use NOT logic operator with the next filters",
		Destination: &negate,
	}
	app.Commands = []cli.Command{
		{
			Name:  "print",
			Usage: "prints filtered entries",
			Flags: []cli.Flag{
				configFlag,
				layoutFlag,
				formatFlag,
				maxFlag,
				stacktraceSearchFlag,
				levelFilterFlag,
				categoryFilterFlag,
				originFilterFlag,
				messageFilterFlag,
				stacktraceFilterFlag,
				exceptionFilterFlag,
				matchOperationFlag,
				containsOperationFlag,
				logicOrFlag,
				logicNotFlag,
			},
			Action: func(c *cli.Context) error {
				inputFileName := c.Args().First()

				if configFile != "" {
					mappers.ParseYaml(configFile)
				}

				if filter == nil {
					filter = filters.All
				}

				return commands.PrintCommand{
					Filter:        filter,
					Format:        formatOutput,
					InputFileName: inputFileName,
					Layout:        layout,
					MaxEntries:    maxEntries,
				}.Execute()
			},
		},
		{
			Name:  "count",
			Usage: "counts filtered entries",
			Flags: []cli.Flag{
				configFlag,
				layoutFlag,
				maxFlag,
				stacktraceSearchFlag,
				levelFilterFlag,
				categoryFilterFlag,
				originFilterFlag,
				messageFilterFlag,
				stacktraceFilterFlag,
				exceptionFilterFlag,
				matchOperationFlag,
				containsOperationFlag,
				logicOrFlag,
				logicNotFlag,
			},
			Action: func(c *cli.Context) error {
				inputFileName := c.Args().First()

				if configFile != "" {
					mappers.ParseYaml(configFile)
				}

				if filter == nil {
					filter = filters.All
				}

				return commands.CountCommand{
					Filter:        filter,
					InputFileName: inputFileName,
					Layout:        layout,
					MaxEntries:    maxEntries,
				}.Execute()
			},
		},
	}
	if err := app.Run(os.Args); err != nil {
		os.Stderr.WriteString(err.Error())
		os.Stderr.WriteString("\n")
		os.Exit(1)
	}
}
