package processors

import (
	"github.com/devnull-tools/sherlog-holmes/domain"
)

const (
	FORMAT_RAW = "{{.RawContent}}"
)

type Processor interface {
	Before()
	Execute(entry *domain.Entry)
	After()
}

func Process(in <-chan *domain.Entry, out chan<- bool, processor Processor) {
	processor.Before()
	for entry := range in {
		processor.Execute(entry)
	}
	processor.After()
	out <- true
}
