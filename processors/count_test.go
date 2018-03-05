package processors_test

import (
	"testing"

	"bytes"

	"github.com/devnull-tools/sherlog-holmes/domain"
	"github.com/devnull-tools/sherlog-holmes/processors"
)

type CountTest struct {
	T        *testing.T
	Group    string
	NewEntry func(attr string) *domain.Entry
	values   map[string]int64
}

func (test *CountTest) expect(label string, count int64) {
	if n := test.values[label]; n != count {
		test.T.Errorf("Invalid count for %s. Expected: %d | Got: %d", label, count, n)
	}
}

func (test *CountTest) doTest() {
	var buffer bytes.Buffer
	processor := processors.NewCountProcessor([]string{test.Group}, processors.Default, &buffer)

	processor.Before()
	processor.Execute(test.NewEntry("lorem"))
	processor.Execute(test.NewEntry("bacon"))
	processor.Execute(test.NewEntry("bacon"))
	processor.Execute(test.NewEntry("lorem"))
	processor.Execute(test.NewEntry("meh"))
	processor.Execute(test.NewEntry("bacon"))
	processor.Execute(test.NewEntry("meh"))
	processor.Execute(test.NewEntry("lorem"))
	processor.Execute(test.NewEntry("bacon"))
	processor.After()

	test.values = processor.Counters[test.Group].Values

	test.expect("lorem", 3)
	test.expect("meh", 2)
	test.expect("bacon", 4)
	test.expect("foo", 0)
	test.expect("bar", 0)
}

func TestCountProcessorForLevel(t *testing.T) {
	entry := func(level string) *domain.Entry {
		return &domain.Entry{Level: level}
	}
	test := CountTest{
		Group:    "level",
		NewEntry: entry,
		T:        t,
	}
	test.doTest()
}

func TestCountProcessorForCategory(t *testing.T) {
	entry := func(category string) *domain.Entry {
		return &domain.Entry{Category: category}
	}
	test := CountTest{
		Group:    "category",
		NewEntry: entry,
		T:        t,
	}
	test.doTest()
}

func TestCountProcessorForOrigin(t *testing.T) {
	entry := func(origin string) *domain.Entry {
		return &domain.Entry{Origin: origin}
	}
	test := CountTest{
		Group:    "origin",
		NewEntry: entry,
		T:        t,
	}
	test.doTest()
}

func TestCountProcessorForException(t *testing.T) {
	entry := func(exception string) *domain.Entry {
		e := domain.Entry{}
		e.AddException(exception)
		return &e
	}
	test := CountTest{
		Group:    "exception",
		NewEntry: entry,
		T:        t,
	}
	test.doTest()
}
