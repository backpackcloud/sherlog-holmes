package main

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
var filter func(entry *domain.Entry) bool
var orOperation = false
var useMatches = false
var useContains = false
var negate = false

func main() {
	app := cli.NewApp()
	app.Name = "sherlog-holmes"
	app.Usage = "Sanitize your log files"

	app.Version = VERSION

	app.Commands = []cli.Command{
		{
			Name:  "print",
			Usage: "prints filtered entries",
			Flags: []cli.Flag{
				cli.StringFlag{
					Name:        "layout, l",
					Usage:       "sets the layout for mapping log entries",
					Destination: &layout,
				},
				cli.StringFlag{
					Name:        "format, o",
					Usage:       "sets the format output",
					Value:       processors.FORMAT_RAW,
					Destination: &formatOutput,
				},
				cli.Int64Flag{
					Name:        "max, m",
					Usage:       "sets the maximum number of filtered entries",
					Value:       -1,
					Destination: &maxEntries,
				},
				cli.BoolFlag{
					Name:        "search-stacktrace, s",
					Usage:       "Searchs for exceptions on stacktrace elements",
					EnvVar:      "SHERLOG_SEARCH_STACKTRACE",
					Destination: &mappers.FindExceptionsOnStacktrace,
				},
				cli.GenericFlag{
					Name:  "level",
					Usage: "sets a level filter",
					Value: &FilterFlag{Attribute: filters.Level, Operation: filters.Equals},
				},
				cli.GenericFlag{
					Name:  "category",
					Usage: "sets a category filter",
					Value: &FilterFlag{Attribute: filters.Category, Operation: filters.Equals},
				},
				cli.GenericFlag{
					Name:  "origin",
					Usage: "sets an origin filter",
					Value: &FilterFlag{Attribute: filters.Category, Operation: filters.Equals},
				},
				cli.GenericFlag{
					Name:  "message",
					Usage: "sets a message filter",
					Value: &FilterFlag{Attribute: filters.Message, Operation: filters.Equals},
				},
				cli.GenericFlag{
					Name:  "stacktrace",
					Usage: "sets a stacktrace filter",
					Value: &FilterFlag{Attribute: filters.Stacktrace, Operation: filters.Contains},
				},
				cli.GenericFlag{
					Name:  "exception",
					Usage: "sets an exception filter",
					Value: &FilterFlag{Attribute: filters.Exception, Operation: filters.Equals},
				},
				cli.BoolFlag{
					Name:        "matches",
					Usage:       "sets the filter value to be a regular expression match",
					Destination: &useMatches,
				},
				cli.BoolFlag{
					Name:        "contains",
					Usage:       "sets the filter value to be a contains match",
					Destination: &useContains,
				},
				cli.BoolFlag{
					Name:        "or",
					Usage:       "use OR logic operator with the next filters",
					Destination: &orOperation,
				},
				cli.BoolFlag{
					Name:        "not",
					Usage:       "use NOT logic operator with the next filters",
					Destination: &negate,
				},
			},
			Action: func(c *cli.Context) error {
				inputFileName := c.Args().First()

				if filter == nil {
					filter = filters.All
				}

				return commands.Command{
					Filter:        filter,
					Format:        formatOutput,
					InputFileName: inputFileName,
					Layout:        layout,
					MaxEntries:    maxEntries,
				}.Print()
			},
		},
	}

	app.Run(os.Args)
}
