package test

import (
	"bufio"
	"strings"

	"bytes"
	"testing"

	"github.com/devnull-tools/sherlog-holmes/domain"
	"github.com/devnull-tools/sherlog-holmes/mappers"
	"github.com/devnull-tools/sherlog-holmes/processors"
)

type MapperTest struct {
	Mapper mappers.Mapper
}

func (mapperTest *MapperTest) Test(lines string) chan *domain.Entry {
	lineChan := make(chan *domain.Line)
	entryChan := make(chan *domain.Entry)

	go mapperTest.Mapper.Map(lineChan, entryChan)

	go func() {
		scanner := bufio.NewScanner(strings.NewReader(lines))
		for scanner.Scan() {
			lineChan <- &domain.Line{Content: scanner.Text()}
		}
		close(lineChan)
	}()
	return entryChan
}

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

func (test *CountTest) DoTest() {
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
