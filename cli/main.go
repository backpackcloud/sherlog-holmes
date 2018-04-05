package cli

import (
	"github.com/urfave/cli"

	"os"
)

var VERSION = "0.8.0"

func Execute() {
	app := cli.NewApp()
	app.Name = "sherlog-holmes"
	app.Usage = "Sanitize your log files"

	app.Version = VERSION

	app.Flags = []cli.Flag{
		configFlag,
		layoutFlag,
	}
	app.Commands = []cli.Command{
		printCommand,
		countCommand,
	}
	if err := app.Run(os.Args); err != nil {
		os.Stderr.WriteString(err.Error())
		os.Stderr.WriteString("\n")
		os.Exit(1)
	}
}
