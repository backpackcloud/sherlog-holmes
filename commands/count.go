package commands

import (
	"errors"

	"github.com/devnull-tools/sherlog-holmes/filters"
	"github.com/devnull-tools/sherlog-holmes/mappers"
	"github.com/devnull-tools/sherlog-holmes/processors"
	"github.com/devnull-tools/sherlog-holmes/readers"
)

// A structure that defines the print command
type CountCommand struct {
	Layout        string
	InputFileName string
	Filter        filters.EntryFilter
	MaxEntries    int64
	Groups        []string
}

// Prints the filtered entries
func (command CountCommand) Execute() error {
	if command.Layout == "" {
		return errors.New("no layout defined")
	}
	if command.InputFileName == "" {
		return errors.New("no file given")
	}

	reader := readers.FileReader{File: command.InputFileName}
	mapper := mappers.RegisteredMappers[command.Layout]
	processor := processors.NewCountProcessor(command.Groups)

	return Execute(command.MaxEntries, reader, mapper, command.Filter, processor)
}
