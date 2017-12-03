package commands

import (
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
	reader := readers.FileReader{File: command.InputFileName}
	mapper := mappers.Components[command.Layout]
	processor := processors.Print(command.Format)

	return Execute(command.MaxEntries, reader, mapper, command.Filter, processor)
}

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
