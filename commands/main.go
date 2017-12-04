package commands

import (
	"errors"

	"github.com/devnull-tools/sherlog-holmes/domain"
	"github.com/devnull-tools/sherlog-holmes/filters"
	"github.com/devnull-tools/sherlog-holmes/mappers"
	"github.com/devnull-tools/sherlog-holmes/processors"
	"github.com/devnull-tools/sherlog-holmes/readers"
)

// Interface that defines the commands available
type Command interface {
	Execute() error
}

// A structure that defines the print command
type PrintCommand struct {
	Layout        string
	Format        string
	InputFileName string
	Filter        filters.EntryFilter
	MaxEntries    int64
}

// Prints the filtered entries
func (command PrintCommand) Execute() error {
	if command.Layout == "" {
		return errors.New("no layout defined")
	}

	reader := readers.FileReader{File: command.InputFileName}
	mapper := mappers.RegisteredMappers[command.Layout]
	processor := processors.Print(command.Format)

	return Execute(command.MaxEntries, reader, mapper, command.Filter, processor)
}

// Executes the workflow using the given parameters:
// maxEntries: the maximum number of processed entries
// reader: a reader component for reading the input source
// mapper: a mapper component for extracting the log entries
// filter: a filter component for filtering the log entries
// processor: a processor component for processing the filtered entries
func Execute(maxEntries int64,
	reader readers.Reader,
	mapper mappers.Mapper,
	filter filters.EntryFilter,
	processor processors.Processor) error {

	entryChan := make(chan *domain.Entry)
	lineChan := make(chan string)
	processChan := make(chan *domain.Entry)
	done := make(chan bool)

	go reader.Read(lineChan)
	go mapper.Map(lineChan, entryChan)
	go filters.Filter(entryChan, processChan, filter, maxEntries)
	go processors.Process(processChan, done, processor)

	<-done
	return nil
}
