package mappers

import "regexp"

// A Java mapper for use as a base
var JavaMapper RegexpMapper

func init() {
	JavaMapper = RegexpMapper{
		Entry:      regexp.MustCompile(`(?P<message>.+)`),
		Exception:  regexp.MustCompile(`(?P<exception>\w+(\.\w+)+(Exception|Error|Fault))`),
		Stacktrace: regexp.MustCompile(`^(\s+at)|(Caused by:)|(\s+\.{3}\s\d+\smore)`),
	}
	RegisteredMappers["base.java"] = JavaMapper
	RegexpMappers["base.java"] = JavaMapper
}
