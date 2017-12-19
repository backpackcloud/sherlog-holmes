package processors

import (
	"io"
	"text/template"

	"github.com/devnull-tools/sherlog-holmes/domain"
	"github.com/devnull-tools/sherlog-holmes/filters"
)

// Holds the registered formatters
var Formatters map[string]Formatter

// Defines a Formatter as a function that prints the given count map on the given writer
type Formatter func(writer io.Writer, countMap map[string]EntryCount)

// Returns a new formatter that uses the given go template for printing the count map
func TemplateFormatter(templateString string) Formatter {
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
	Formatters = map[string]Formatter{
		"default": Default,
		"csv":     Csv,
		"json":    Json,
	}
}

// Structure that represents a counter
type EntryCount struct {
	// The extractor for what is being counted
	extractor filters.Extractor
	// The counter grouped by values
	Values map[string]int64
}

// Defines a processor for counting entries
type countProcessor struct {
	Counters  map[string]EntryCount
	formatter Formatter
	writer    io.Writer
}

func (processor countProcessor) Before() {

}

// Prints the totals after the process
func (processor countProcessor) After() {
	processor.formatter(processor.writer, processor.Counters)
}

// Counts the entries as they arrive on the pipeline
func (processor countProcessor) Execute(entry *domain.Entry) {
	for _, counter := range processor.Counters {
		for _, value := range counter.extractor(entry) {
			counter.Values[value]++
		}
	}
}

// Creates a new processor that will count the entries and prints the total at the end of the process
// groups: an array of the groups to count (level, category, origin or exception)
// formatter: the component for printing the output in some format
// writer: the writer that will receive the output for printing
func NewCountProcessor(groups []string, formatter Formatter, writer io.Writer) Processor {
	extractors := map[string]filters.Extractor{
		"level":     filters.Level,
		"category":  filters.Category,
		"origin":    filters.Origin,
		"exception": filters.Exception,
	}

	counters := make(map[string]EntryCount)

	for _, name := range groups {
		counters[name] = EntryCount{
			extractor: extractors[name],
			Values:    make(map[string]int64),
		}
	}
	return countProcessor{Counters: counters, formatter: formatter, writer: writer}
}
