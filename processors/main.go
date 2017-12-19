package processors

import (
	"github.com/devnull-tools/sherlog-holmes/domain"
)

const (
	FORMAT_RAW = "{{.RawContent}}"
)

// Interface that defines an entry processor
type Processor interface {

	// Do something before the entire process
	Before()
	// Process the given entry
	Execute(entry *domain.Entry)
	// Do something after the entire process
	After()
}

// Process every entry from the input channel and tells the output channel about the end of the process
func Process(in <-chan *domain.Entry, out chan<- bool, processor Processor) {
	processor.Before()
	for entry := range in {
		processor.Execute(entry)
	}
	processor.After()
	out <- true
}
