package mappers_test

import (
	"testing"

	"github.com/devnull-tools/sherlog-holmes/mappers"
	"github.com/devnull-tools/sherlog-holmes/test"
)

func TestWildflyMapperSimpleMessage(t *testing.T) {
	mapperTest := test.MapperTest{Mapper: mappers.WildflyMapper}

	entries := mapperTest.Test(`
2018-02-15 15:42:53,227 INFO  [org.jboss.modules] (main) JBoss Modules version 1.5.4.Final-redhat-1
2018-02-15 15:42:53,863 INFO  [org.jboss.msc] (main) JBoss MSC version 1.2.7.SP1-redhat-1
`)

	entry := <-entries
	if entry.Message() != "JBoss Modules version 1.5.4.Final-redhat-1" {
		t.Error("Unexpected entry message")
	}

	if entry.Time != "2018-02-15 15:42:53,227" {
		t.Error("Unexpected entry time")
	}

	if entry.Category != "org.jboss.modules" {
		t.Error("Unexpected entry category")
	}

	if entry.Origin != "main" {
		t.Error("Unexpected entry origin")
	}

	if entry.Level != "INFO" {
		t.Error("Unexpected entry level")
	}

	entry = <-entries
	if entry.Message() != "JBoss MSC version 1.2.7.SP1-redhat-1" {
		t.Error("Unexpected entry message")
	}

	if entry.Time != "2018-02-15 15:42:53,863" {
		t.Error("Unexpected entry time")
	}

	if entry.Category != "org.jboss.msc" {
		t.Error("Unexpected entry category")
	}

	if entry.Level != "INFO" {
		t.Error("Unexpected entry level")
	}
}

func TestWildflyMapperMessageWithException(t *testing.T) {
	mapperTest := test.MapperTest{Mapper: mappers.WildflyMapper}

	entries := mapperTest.Test("2018-02-15 15:42:53,227 INFO  [org.jboss.modules] (main) Meh: java.lang.RuntimeException")
	entry := <-entries

	if entry.Message() != "Meh: java.lang.RuntimeException" {
		t.Error("Unexpected entry message")
	}
	if len(entry.Exceptions) != 1 {
		t.Error("Unexpected number of exceptions")
	}
	if entry.Exceptions[0] != "java.lang.RuntimeException" {
		t.Error("Unexpected exception name")
	}
}
