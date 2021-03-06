package commands

import (
	"errors"

	"io"

	"github.com/devnull-tools/sherlog-holmes/filters"
	"github.com/devnull-tools/sherlog-holmes/mappers"
	"github.com/devnull-tools/sherlog-holmes/processors"
	"github.com/devnull-tools/sherlog-holmes/readers"
)

// A structure that defines the print command
type PrintCommand struct {
	Layout     string
	Format     string
	InputFiles []string
	Filter     filters.EntryFilter
	MaxEntries int64
	Writer     io.Writer
}

// Prints the filtered entries
func (command PrintCommand) Execute() error {
	if command.Layout == "" {
		return errors.New("no layout defined")
	}
	if len(command.InputFiles) == 0 {
		return errors.New("no file given")
	}

	reader := readers.FileReader{Files: command.InputFiles}
	mapper := mappers.RegisteredMappers[command.Layout]
	processor := processors.NewPrintProcessor(command.Writer, command.Format)

	return Execute(command.MaxEntries, reader, mapper, command.Filter, processor)
}
