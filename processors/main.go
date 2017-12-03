package processors

import (
	"fmt"
	"os"
	"text/template"

	"github.com/devnull-tools/sherlog-holmes/domain"
)

const (
	FORMAT_RAW = "{{.RawContent}}"
)

func Print(format string) func(entry *domain.Entry) {
	if format == FORMAT_RAW || format == "" {
		return func(entry *domain.Entry) {
			fmt.Println(entry.RawContent())
		}
	}
	tmpl, err := template.New("Sherlog Holmes").Parse(format + "\n")
	if err != nil {
		panic(err)
	}
	return func(entry *domain.Entry) {
		err = tmpl.Execute(os.Stdout, entry)
		if err != nil {
			panic(err)
		}
	}
}

func Process(in <-chan *domain.Entry, out chan<- bool, action func(entry *domain.Entry)) {
	for entry := range in {
		action(entry)
	}
	out <- true
}
