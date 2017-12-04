package mappers

import "regexp"

func init() {
	Components["wildfly"] = Wildfly()
}

func Wildfly() Mapper {
	mapper := RegexpMapper{}
	mapper.Entry = regexp.MustCompile(`(?P<time>(?P<date>\d{2,4}-\d{2}-\d{2,4}\s)?(\d{2}:\d{2}:\d{2},\d{3}))\s+\|?\s*(?P<level>\w+)\s+\|?\s*\[(?P<category>\S+)]\s+\|?\s*\((?P<origin>[^)]+)\)?\s?\|?\s?(?P<message>.+)`)
	mapper.Exception = regexp.MustCompile(`(?P<exception>\w+(\.\w+)+(Exception|Error|Fault))`)
	mapper.Stacktrace = regexp.MustCompile(`^(\s+at)|(Caused by:)|(\s+\.{3}\s\d+\smore)`)
	return mapper
}
