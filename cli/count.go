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
		formatFlag,
		templateFlag,
		maxFlag,
		groupFlag,
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

		formatter := processors.Formatters["default"]

		output := c.String("format")
		if output != "" {
			formatter = processors.Formatters[output]
		}
		templateFile := c.String("template")
		if templateFile != "" {
			if file, err := ioutil.ReadFile(templateFile); err == nil {
				formatter = processors.TemplateFormatter(string(file))
			} else {
				return err
			}
		}

		groups := c.StringSlice("group")

		// if user defines groups, the default one won't be erased
		// this will clean the default groups to avoid the odd behaviour
		var defaultGroupsSize = len(groupFlag.Value.Value())
		if len(groups) > defaultGroupsSize {
			groups = groups[defaultGroupsSize:]
		}

		return commands.CountCommand{
			Filter:        Filter,
			InputFileName: inputFileName,
			Layout:        c.GlobalString("layout"),
			MaxEntries:    c.Int64("max"),
			Groups:        groups,
			Formatter:     formatter,
			Writer:        os.Stdout,
		}.Execute()
	},
}
