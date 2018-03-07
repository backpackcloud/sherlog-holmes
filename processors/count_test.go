package processors_test

import (
	"testing"

	"github.com/devnull-tools/sherlog-holmes/domain"
	"github.com/devnull-tools/sherlog-holmes/test"
)

func TestCountProcessorForLevel(t *testing.T) {
	entry := func(level string) *domain.Entry {
		return &domain.Entry{Level: level}
	}
	countTest := test.CountTest{
		Group:    "level",
		NewEntry: entry,
		T:        t,
	}
	countTest.DoTest()
}

func TestCountProcessorForCategory(t *testing.T) {
	entry := func(category string) *domain.Entry {
		return &domain.Entry{Category: category}
	}
	countTest := test.CountTest{
		Group:    "category",
		NewEntry: entry,
		T:        t,
	}
	countTest.DoTest()
}

func TestCountProcessorForOrigin(t *testing.T) {
	entry := func(origin string) *domain.Entry {
		return &domain.Entry{Origin: origin}
	}
	countTest := test.CountTest{
		Group:    "origin",
		NewEntry: entry,
		T:        t,
	}
	countTest.DoTest()
}

func TestCountProcessorForException(t *testing.T) {
	entry := func(exception string) *domain.Entry {
		e := domain.Entry{}
		e.AddException(exception)
		return &e
	}
	countTest := test.CountTest{
		Group:    "exception",
		NewEntry: entry,
		T:        t,
	}
	countTest.DoTest()
}
