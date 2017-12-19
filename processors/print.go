package processors

import (
	"fmt"
	"text/template"

	"io"

	"github.com/devnull-tools/sherlog-holmes/domain"
)

// Struct that defines a processor that follows a template for printing the filtered entries
type printProcessor struct {
	processFunction func(entry *domain.Entry)
}

// Returns a new processor that will use the given template for formatting the output
func NewPrintProcessor(writer io.Writer, format string) Processor {
	if format == FORMAT_RAW || format == "" {
		return printProcessor{
			processFunction: func(entry *domain.Entry) {
				fmt.Println(entry.RawContent())
			},
		}
	}
	tmpl, err := template.New("Sherlog Holmes").Parse(format + "\n")
	if err != nil {
		panic(err)
	}
	return printProcessor{
		processFunction: func(entry *domain.Entry) {
			err = tmpl.Execute(writer, entry)
			if err != nil {
				panic(err)
			}
		},
	}
}

func (processor printProcessor) After() {

}

func (processor printProcessor) Before() {

}

func (processor printProcessor) Execute(entry *domain.Entry) {
	processor.processFunction(entry)
}
