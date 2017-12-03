package filters

import (
	"regexp"
	"strings"

	"github.com/devnull-tools/sherlog-holmes/domain"
)

type Extractor func(entry *domain.Entry) []string

type Operation func(value string, extractor func(entry *domain.Entry) []string) func(entry *domain.Entry) bool

func All(entry *domain.Entry) bool {
	return true
}

func Level(entry *domain.Entry) []string {
	return []string{entry.Level}
}

func Category(entry *domain.Entry) []string {
	return []string{entry.Category}
}

func Origin(entry *domain.Entry) []string {
	return []string{entry.Origin}
}

func Message(entry *domain.Entry) []string {
	return []string{entry.Message()}
}

func Exception(entry *domain.Entry) []string {
	return entry.Exceptions
}

func Stacktrace(entry *domain.Entry) []string {
	return []string{entry.Stacktrace()}
}

func Equals(value string, extractor func(entry *domain.Entry) []string) func(entry *domain.Entry) bool {
	return func(entry *domain.Entry) bool {
		for _, v := range extractor(entry) {
			if v == value {
				return true
			}
		}
		return false
	}
}

func Contains(value string, extractor func(entry *domain.Entry) []string) func(entry *domain.Entry) bool {
	return func(entry *domain.Entry) bool {
		for _, v := range extractor(entry) {
			if strings.Contains(v, value) {
				return true
			}
		}
		return false
	}
}

func Matches(expression string, extractor func(entry *domain.Entry) []string) func(entry *domain.Entry) bool {
	exp := regexp.MustCompile(expression)
	return func(entry *domain.Entry) bool {
		for _, v := range extractor(entry) {
			if exp.MatchString(v) {
				return true
			}
		}
		return false
	}
}

func exception(expression string) func(entry *domain.Entry) bool {
	exp := regexp.MustCompile(expression)
	return func(entry *domain.Entry) bool {
		for _, exception := range entry.Exceptions {
			if exp.MatchString(exception) {
				return true
			}
		}
		return false
	}
}

func And(filterA func(entry *domain.Entry) bool, filterB func(entry *domain.Entry) bool) func(entry *domain.Entry) bool {
	return func(entry *domain.Entry) bool {
		return filterA(entry) && filterB(entry)
	}
}

func Or(filterA func(entry *domain.Entry) bool, filterB func(entry *domain.Entry) bool) func(entry *domain.Entry) bool {
	return func(entry *domain.Entry) bool {
		return filterA(entry) || filterB(entry)
	}
}

func Negate(filter func(entry *domain.Entry) bool) func(entry *domain.Entry) bool {
	return func(entry *domain.Entry) bool {
		return !filter(entry)
	}
}

func Filter(in <-chan *domain.Entry, out chan<- *domain.Entry, filter func(entry *domain.Entry) bool, max int64) {
	var filtered int64
	for e := range in {
		if filter(e) {
			out <- e
			filtered++
			if filtered != max {

			} else {
				break
			}
		}
	}
	close(out)
}

/*
func Parse(expression string) func(entry *domain.Entry) bool {
	if expression == "" {
		return func(entry *domain.Entry) bool {
			return true
		}
	}
	var filter func(entry *domain.Entry) bool
	var operator string
	for _, term := range strings.Split(expression, " ") {
		if strings.Contains(term, ":") {
			split := strings.Split(term, ":")
			attribute := split[0]
			value := split[1]
			if operator == "" {
				filter = parse(attribute, value)
			} else {
				switch operator {
				case "and":
					filter = and(filter, parse(attribute, value))
				case "or":
					filter = or(filter, parse(attribute, value))
				default:
					panic(errors.New("no such operator: " + operator))
				}
				operator = ""
			}
		} else {
			operator = term
		}
	}
	return filter
}

func parse(attribute string, value string) func(entry *domain.Entry) bool {
	var filter func(entry *domain.Entry) bool
	negate := false
	if string(value[0]) == "!" {
		negate = true
		value = value[1:]
	}
	switch attribute {
	case "level":
		filter = Level(value)
	case "category":
		filter = Category(value)
	case "origin":
		filter = Origin(value)
	case "message":
		filter = Message(value)
	case "exception":
		filter = Exception(value)
	case "stacktrace":
		filter = Stacktrace(value)
	default:
		panic(errors.New("no such attribute for filtering: " + attribute))
	}
	if negate {
		filter = Negate(filter)
	}
	return filter
}
*/
