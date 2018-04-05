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

	lineChan := make(chan *domain.Line)
	entryChan := make(chan *domain.Entry)
	processChan := make(chan *domain.Entry)
	done := make(chan bool)

	go reader.Read(lineChan)
	go mapper.Map(lineChan, entryChan)
	go filters.Filter(entryChan, processChan, filter, maxEntries)
	go processors.Process(processChan, done, processor)

	<-done
	return nil
}
