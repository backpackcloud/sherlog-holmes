package processors

import (
	"fmt"
	"os"
	"text/template"

	"github.com/devnull-tools/sherlog-holmes/domain"
)

type printProcessor struct {
	processFunction func(entry *domain.Entry)
}

func NewPrintProcessor(format string) Processor {
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
			err = tmpl.Execute(os.Stdout, entry)
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
