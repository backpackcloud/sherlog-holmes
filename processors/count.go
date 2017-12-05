package processors

import (
	"fmt"

	"github.com/devnull-tools/sherlog-holmes/domain"
	"github.com/devnull-tools/sherlog-holmes/filters"
)

type countProcessor struct {
	Counters []entryCount
}

type entryCount struct {
	Name      string
	Extractor filters.Extractor
	Values    map[string]int64
}

func (processor countProcessor) Before() {

}

func (processor countProcessor) After() {
	for _, counter := range processor.Counters {
		fmt.Println(counter.Name)
		for key, value := range counter.Values {
			fmt.Print(key)
			fmt.Print("\t\t\t")
			fmt.Println(value)
		}
	}
}

func (processor countProcessor) Execute(entry *domain.Entry) {
	for _, counter := range processor.Counters {
		for _, value := range counter.Extractor(entry) {
			counter.Values[value]++
		}
	}
}

func NewCountProcessor(groups []string) Processor {
	counters := make([]entryCount, 0)
	for _, name := range groups {
		switch name {
		case "level":
			counters = append(counters, entryCount{
				Name:      "Levels",
				Extractor: filters.Level,
				Values:    make(map[string]int64),
			})
		case "category":
			counters = append(counters, entryCount{
				Name:      "Categories",
				Extractor: filters.Category,
				Values:    make(map[string]int64),
			})
		case "origin":
			counters = append(counters, entryCount{
				Name:      "Origins",
				Extractor: filters.Origin,
				Values:    make(map[string]int64),
			})
		case "exception":
			counters = append(counters, entryCount{
				Name:      "Exceptions",
				Extractor: filters.Exception,
				Values:    make(map[string]int64),
			})
		}
	}
	return countProcessor{Counters: counters}
}
