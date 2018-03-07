package cli

import (
	"github.com/devnull-tools/sherlog-holmes/domain"
	"github.com/devnull-tools/sherlog-holmes/filters"
	"github.com/devnull-tools/sherlog-holmes/mappers"
	"github.com/urfave/cli"
)

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
	if Filter == nil {
		Filter = f
	} else {
		if orOperation {
			Filter = filters.Or(Filter, f)
			orOperation = false
		} else {
			Filter = filters.And(Filter, f)
		}
	}
	return nil
}

var orOperation = false
var useMatches = false
var useContains = false
var negate = false

var Filter func(entry *domain.Entry) bool

var configFlag = cli.StringFlag{
	Name:   "config, c",
	Usage:  "loads pattern configurations from `FILE`",
	EnvVar: "SHERLOG_CONFIG_FILE",
}
var layoutFlag = cli.StringFlag{
	Name:  "layout, l",
	Usage: "sets the layout for mapping log entries",
}
var formatFlag = cli.StringFlag{
	Name:  "format, o",
	Usage: "sets the format output",
}
var templateFlag = cli.StringFlag{
	Name:  "template, t",
	Usage: "sets the template file to use",
}
var groupFlag = cli.StringSliceFlag{
	Name:  "group, g",
	Usage: "add the given group to the count",
	Value: &cli.StringSlice{"level", "category", "exception"},
}
var maxFlag = cli.Int64Flag{
	Name:  "max, m",
	Usage: "sets the maximum number of filtered entries",
	Value: -1,
}
var stacktraceSearchFlag = cli.BoolFlag{
	Name:        "search-stacktrace, s",
	Usage:       "Searchs for exceptions on stacktrace elements",
	EnvVar:      "SHERLOG_SEARCH_STACKTRACE",
	Destination: &mappers.FindExceptionsOnStacktrace,
}
var levelFilterFlag = cli.GenericFlag{
	Name:  "level",
	Usage: "sets a level filter",
	Value: &FilterFlag{Attribute: filters.Level, Operation: filters.Equals},
}
var categoryFilterFlag = cli.GenericFlag{
	Name:  "category",
	Usage: "sets a category filter",
	Value: &FilterFlag{Attribute: filters.Category, Operation: filters.Equals},
}
var originFilterFlag = cli.GenericFlag{
	Name:  "origin",
	Usage: "sets an origin filter",
	Value: &FilterFlag{Attribute: filters.Category, Operation: filters.Equals},
}
var messageFilterFlag = cli.GenericFlag{
	Name:  "message",
	Usage: "sets a message filter",
	Value: &FilterFlag{Attribute: filters.Message, Operation: filters.Equals},
}
var stacktraceFilterFlag = cli.GenericFlag{
	Name:  "stacktrace",
	Usage: "sets a stacktrace filter",
	Value: &FilterFlag{Attribute: filters.Stacktrace, Operation: filters.Contains},
}
var exceptionFilterFlag = cli.GenericFlag{
	Name:  "exception",
	Usage: "sets an exception filter",
	Value: &FilterFlag{Attribute: filters.Exception, Operation: filters.Equals},
}
var matchOperationFlag = cli.BoolFlag{
	Name:        "matches",
	Usage:       "sets the filter value to be a regular expression match",
	Destination: &useMatches,
}
var containsOperationFlag = cli.BoolFlag{
	Name:        "contains",
	Usage:       "sets the filter value to be a contains match",
	Destination: &useContains,
}
var logicOrFlag = cli.BoolFlag{
	Name:        "or",
	Usage:       "use OR logic operator with the next filters",
	Destination: &orOperation,
}
var logicNotFlag = cli.BoolFlag{
	Name:        "not",
	Usage:       "use NOT logic operator with the next filters",
	Destination: &negate,
}
