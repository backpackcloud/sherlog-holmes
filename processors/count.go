package processors

import (
	"io"
	"text/template"

	"github.com/devnull-tools/sherlog-holmes/domain"
	"github.com/devnull-tools/sherlog-holmes/filters"
)

var CountPrinters map[string]Printer

type Printer func(writer io.Writer, countMap map[string]EntryCount)

func TemplatePrinter(templateString string) Printer {
	return func(writer io.Writer, counts map[string]EntryCount) {
		tmpl, err := template.New("Sherlog Holmes - Count").Parse(templateString)
		if err != nil {
			panic(err)
		}
		err = tmpl.Execute(writer, counts)
		if err != nil {
			panic(err)
		}
	}
}

func init() {
	CountPrinters = make(map[string]Printer)
	CountPrinters["default"] = Default
	CountPrinters["csv"] = Csv
	CountPrinters["json"] = Json
}

type countProcessor struct {
	Counters map[string]EntryCount
	printer  Printer
	writer   io.Writer
}

type EntryCount struct {
	extractor filters.Extractor
	Values    map[string]int64
}

func (processor countProcessor) Before() {

}

func (processor countProcessor) After() {
	processor.printer(processor.writer, processor.Counters)
}

func (processor countProcessor) Execute(entry *domain.Entry) {
	for _, counter := range processor.Counters {
		for _, value := range counter.extractor(entry) {
			counter.Values[value]++
		}
	}
}

func NewCountProcessor(groups []string, printer Printer, writer io.Writer) Processor {
	extractors := make(map[string]filters.Extractor)
	extractors["level"] = filters.Level
	extractors["category"] = filters.Category
	extractors["origin"] = filters.Origin
	extractors["exception"] = filters.Exception

	counters := make(map[string]EntryCount)

	for _, name := range groups {
		counters[name] = EntryCount{
			extractor: extractors[name],
			Values:    make(map[string]int64),
		}
	}
	return countProcessor{Counters: counters, printer: printer, writer: writer}
}
