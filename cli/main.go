package cli

import (
	"github.com/urfave/cli"

	"os"
)

var VERSION = "0.7.0.rc3"

func Execute() {
	app := cli.NewApp()
	app.Name = "sherlog-holmes"
	app.Usage = "Sanitize your log files"

	app.Version = VERSION

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
