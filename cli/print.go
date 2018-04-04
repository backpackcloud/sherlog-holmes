package cli

import (
	"os"

	"github.com/devnull-tools/sherlog-holmes/commands"
	"github.com/devnull-tools/sherlog-holmes/filters"
	"github.com/devnull-tools/sherlog-holmes/mappers"
	"github.com/urfave/cli"
)

var printCommand = cli.Command{
	Name:  "print",
	Usage: "prints filtered entries",
	Flags: []cli.Flag{
		formatFlag,
		maxFlag,
		stacktraceSearchFlag,
		timeFilterFlag,
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
		configFile := c.GlobalString("config")
		if configFile != "" {
			if err := mappers.ParseYaml(configFile); err != nil {
				return err
			}
		}

		if Filter == nil {
			Filter = filters.All
		}

		return commands.PrintCommand{
			Filter:        Filter,
			Format:        c.String("format"),
			InputFileName: inputFileName,
			Layout:        c.GlobalString("layout"),
			MaxEntries:    c.Int64("max"),
			Writer:        os.Stdout,
		}.Execute()
	},
}
