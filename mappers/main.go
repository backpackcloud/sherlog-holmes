package mappers

import (
	"regexp"

	"github.com/devnull-tools/sherlog-holmes/domain"
)

// Holds all the mappers registered in Sherlog
var RegisteredMappers = make(map[string]Mapper)
// Holds all regexp based mappers registered in Sherlog (used for parsing user configuration)
var RegexpMappers = make(map[string]RegexpMapper)
// Tells if Sherlog should scan stacktrace for exceptions (may slow down performance)
var FindExceptionsOnStacktrace = false

// Interface that defines a mapper
// Basically, a mapper is responsible for grabbing strings from a channel, mapping it to an Entry structure and
// passing it to the pipeline channel
type Mapper interface {

	// Maps all string from the input channel to an Entry structure and sends them to the pipeline channel
	Map(in <-chan *domain.Line, pipeline chan<- *domain.Entry)

}

// Struct that defines a mapper that uses regular expression
type RegexpMapper struct {

	// The pattern for the entry, the captured groups will be bound to the Entry object
	Entry      *regexp.Regexp
	// The pattern for exception names
	Exception  *regexp.Regexp
	// The pattern for the stacktrace, while this pattern occurs in subsequent inputs from the channel, the input will
	// be attached to the last entry as a stacktrace element
	Stacktrace *regexp.Regexp

}

// Implements the mapper interface by using the defined regular expressions against the input strings
func (mapper RegexpMapper) Map(in <-chan *domain.Line, out chan<- *domain.Entry) {
	var entry *domain.Entry
	for line := range in {
		if mapper.Entry.MatchString(line.Content) {
			match := mapper.Entry.FindStringSubmatch(line.Content)
			result := make(map[string]string)
			for i, name := range mapper.Entry.SubexpNames() {
				if i != 0 {
					result[name] = match[i]
				}
			}
			if entry != nil {
				out <- entry
			}
			entry = &domain.Entry{Line: line.Index, Filename:line.Filename}
			entry.SetContent(line.Content)
			if level, ok := result["level"]; ok {
				entry.Level = level
			}
			if category, ok := result["category"]; ok {
				entry.Category = category
			}
			if origin, ok := result["origin"]; ok {
				entry.Origin = origin
			}
			if time, ok := result["time"]; ok {
				entry.Time = time
			}
			if message, ok := result["message"]; ok {
				entry.SetMessage(message)
				if mapper.Exception != nil && mapper.Exception.MatchString(message) {
					match = mapper.Exception.FindStringSubmatch(message)
					result = make(map[string]string)
					if len(match) > 0 {
						for i, name := range mapper.Exception.SubexpNames() {
							if i != 0 {
								result[name] = match[i]
							}
						}
						if exception, ok := result["exception"]; ok {
							entry.AddException(exception)
						}
					}
				}
			}
		} else {
			if entry != nil {
				if mapper.Stacktrace != nil && mapper.Stacktrace.MatchString(line.Content) {
					entry.AddStacktrace(line.Content)
				} else {
					entry.Append(line.Content)
				}
				if FindExceptionsOnStacktrace && mapper.Exception != nil {
					match := mapper.Exception.FindStringSubmatch(line.Content)
					result := make(map[string]string)
					if len(match) > 0 {
						for i, name := range mapper.Exception.SubexpNames() {
							if i != 0 {
								result[name] = match[i]
							}
						}
						if exception, ok := result["exception"]; ok {
							entry.AddException(exception)
						}
					}
				}
			}
		}

	}
	if entry != nil {
		out <- entry
	}
	close(out)
}
