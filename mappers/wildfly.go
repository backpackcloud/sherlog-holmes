package mappers

import "regexp"

var WildflyMapper RegexpMapper

func init() {
	WildflyMapper = RegexpMapper{
		Entry:      regexp.MustCompile(`(?P<time>(?P<date>\d{2,4}-\d{2}-\d{2,4}\s)?(\d{2}:\d{2}:\d{2},\d{3}))\s+\|?\s*(?P<level>\w+)\s+\|?\s*\[(?P<category>\S+)]\s+\|?\s*\((?P<origin>[^)]+)\)?\s?\|?\s?(?P<message>.+)`),
		Exception:  JavaMapper.Exception,
		Stacktrace: JavaMapper.Stacktrace,
	}

	RegisteredMappers["wildfly"] = WildflyMapper
	RegexpMappers["wildfly"] = WildflyMapper

	RegisteredMappers["jboss"] = WildflyMapper
	RegexpMappers["jboss"] = WildflyMapper
}
