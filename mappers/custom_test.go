package mappers_test

import (
	"testing"

	"regexp"

	"github.com/devnull-tools/sherlog-holmes/mappers"
	"github.com/devnull-tools/sherlog-holmes/test"
)

func TestCustomMapperWithoutOptionals(t *testing.T) {
	mapperTest := test.MapperTest{Mapper: mappers.RegexpMapper{
		Entry:regexp.MustCompile("Entry: (?P<message>.+)"),
		Stacktrace:nil,
		Exception:nil,
	}}
	entries := mapperTest.Test("Entry: Bla bla bla\nMeh meh meh")

	if entry := <-entries; entry.Message() != "Bla bla bla" {
		t.Error("Unexpected entry message")
	}
}