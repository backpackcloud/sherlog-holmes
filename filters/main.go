package filters

import (
	"regexp"
	"strings"

	"github.com/devnull-tools/sherlog-holmes/domain"
)

// Defines an extractor of values to be used by the filter
type Extractor func(entry *domain.Entry) []string

// Defines the operation that will be used by the filter
// The operation will be used against the extracted values
type Operation func(value string, extractor func(entry *domain.Entry) []string) func(entry *domain.Entry) bool

// Defines a filter capable of selecting log entries to be processed
type EntryFilter func(entry *domain.Entry) bool

// Filters all entries, no matter its attributes
func All(entry *domain.Entry) bool {
	return true
}

// Extracts the level of a log entry
func Level(entry *domain.Entry) []string {
	return []string{entry.Level}
}

// Extracts the category of a log entry
func Category(entry *domain.Entry) []string {
	return []string{entry.Category}
}

// Extracts the origin of a log entry
func Origin(entry *domain.Entry) []string {
	return []string{entry.Origin}
}

// Extracts the message of a log entry
func Message(entry *domain.Entry) []string {
	return []string{entry.Message()}
}

// Extracts the exceptions of a log entry
func Exception(entry *domain.Entry) []string {
	return entry.Exceptions
}

// Extracts the stacktrace of a log entry
func Stacktrace(entry *domain.Entry) []string {
	return []string{entry.Stacktrace()}
}

// Checks if the extracted value equals the given one
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

// Checks if the extracted value contains the given one
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

// Checks if the extracted value matches the given regular expression
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

// Returns a new filter that uses the "&&" operation against the two given filters
func And(filterA func(entry *domain.Entry) bool, filterB func(entry *domain.Entry) bool) func(entry *domain.Entry) bool {
	return func(entry *domain.Entry) bool {
		return filterA(entry) && filterB(entry)
	}
}

// Returns a new filter that uses the "||" operation against the two given filters
func Or(filterA func(entry *domain.Entry) bool, filterB func(entry *domain.Entry) bool) func(entry *domain.Entry) bool {
	return func(entry *domain.Entry) bool {
		return filterA(entry) || filterB(entry)
	}
}

// Returns a new filter that returns the opposite of the given filter
func Negate(filter func(entry *domain.Entry) bool) func(entry *domain.Entry) bool {
	return func(entry *domain.Entry) bool {
		return !filter(entry)
	}
}

// Runs the filter function using entries from the "in" channel and passes the filtered entries to the
// "out" channel until the number of filtered entries reaches the given "max" number.
func Filter(in <-chan *domain.Entry, out chan<- *domain.Entry, filter func(entry *domain.Entry) bool, max int64) {
	var filtered int64
	for e := range in {
		if filter(e) {
			out <- e
			filtered++
			if filtered == max {
				break
			}
		}
	}
	close(out)
}
