package cli

import (
	"io/ioutil"

	"os"

	"github.com/devnull-tools/sherlog-holmes/commands"
	"github.com/devnull-tools/sherlog-holmes/filters"
	"github.com/devnull-tools/sherlog-holmes/mappers"
	"github.com/devnull-tools/sherlog-holmes/processors"
	"github.com/urfave/cli"
)

var countCommand = cli.Command{
	Name:  "count",
	Usage: "counts filtered entries",
	Flags: []cli.Flag{
		configFlag,
		layoutFlag,
		formatFlag,
		templateFlag,
		maxFlag,
		groupFlag,
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
		configFile := c.String("config")
		if configFile != "" {
			mappers.ParseYaml(configFile)
		}

		if Filter == nil {
			Filter = filters.All
		}

		printer := processors.CountPrinters["default"]

		output := c.String("format")
		if output != "" {
			printer = processors.CountPrinters[output]
		}
		templateFile := c.String("template")
		if templateFile != "" {
			if file, err := ioutil.ReadFile(templateFile); err == nil {
				printer = processors.TemplatePrinter(string(file))
			} else {
				return err
			}
		}

		return commands.CountCommand{
			Filter:        Filter,
			InputFileName: inputFileName,
			Layout:        c.String("layout"),
			MaxEntries:    c.Int64("max"),
			Groups:        c.StringSlice("groups"),
			Printer:       printer,
			Writer:        os.Stdout,
		}.Execute()
	},
}
