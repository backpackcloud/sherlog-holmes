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
type CountCommand struct {
	Layout     string
	InputFiles []string
	Filter     filters.EntryFilter
	MaxEntries int64
	Groups     []string
	Formatter  processors.Formatter
	Writer     io.Writer
}

// Prints the filtered entries
func (command CountCommand) Execute() error {
	if command.Layout == "" {
		return errors.New("no layout defined")
	}
	if len(command.InputFiles) == 0 {
		return errors.New("no file given")
	}

	reader := readers.FileReader{Files: command.InputFiles}
	mapper := mappers.RegisteredMappers[command.Layout]
	processor := processors.NewCountProcessor(command.Groups, command.Formatter, command.Writer)

	return Execute(command.MaxEntries, reader, mapper, command.Filter, processor)
}
