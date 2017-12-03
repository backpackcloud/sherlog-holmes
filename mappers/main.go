package mappers

import (
	"regexp"

	"github.com/devnull-tools/sherlog-holmes/domain"
)

var Components map[string]Mapper = make(map[string]Mapper)

func init() {
	Components["wildfly"] = Wildfly()
}

var FindExceptionsOnStacktrace = false

type Mapper interface {
	Map(lineChannel <-chan string, pipeline chan<- *domain.Entry)
}

type RegexpMapper struct {
	Entry      *regexp.Regexp
	Exception  *regexp.Regexp
	Stacktrace *regexp.Regexp
}

func (mapper RegexpMapper) Map(in <-chan string, out chan<- *domain.Entry) {
	var entry *domain.Entry
	var i int64
	for line := range in {
		i++
		if mapper.Entry.MatchString(line) {
			match := mapper.Entry.FindStringSubmatch(line)
			result := make(map[string]string)
			for i, name := range mapper.Entry.SubexpNames() {
				if i != 0 {
					result[name] = match[i]
				}
			}
			if entry != nil {
				out <- entry
			}
			entry = &domain.Entry{Line: i}
			entry.SetContent(line)
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
				if mapper.Exception.MatchString(message) {
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
				if mapper.Stacktrace.MatchString(line) {
					entry.AddStacktrace(line)
				} else {
					entry.Append(line)
				}
				if FindExceptionsOnStacktrace {
					match := mapper.Exception.FindStringSubmatch(line)
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
