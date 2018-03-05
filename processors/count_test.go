package processors_test

import (
	"testing"

	"bytes"

	"github.com/devnull-tools/sherlog-holmes/domain"
	"github.com/devnull-tools/sherlog-holmes/processors"
)

type CountTest struct {
	Counter map[string]int64
	T       *testing.T
}

func (c *CountTest) expect(label string, count int64) {
	if n := c.Counter[label]; n != count {
		c.T.Errorf("Invalid count for %s. Expected: %d | Got: %d", label, count, n)
	}
}

func TestCountProcessorForLevel(t *testing.T) {
	entry := func(level string) *domain.Entry {
		return &domain.Entry{Level: level}
	}
	var buffer bytes.Buffer
	processor := processors.NewCountProcessor([]string{"level"}, processors.Default, &buffer)

	processor.Before()
	processor.Execute(entry("INFO"))
	processor.Execute(entry("INFO"))
	processor.Execute(entry("WARN"))
	processor.Execute(entry("DEBUG"))
	processor.Execute(entry("ERROR"))
	processor.Execute(entry("INFO"))
	processor.Execute(entry("ERROR"))
	processor.Execute(entry("WARN"))
	processor.Execute(entry("INFO"))
	processor.After()

	test := CountTest{
		Counter: processor.Counters["level"].Values,
		T:       t,
	}

	test.expect("INFO", 4)
	test.expect("WARN", 2)
	test.expect("DEBUG", 1)
	test.expect("ERROR", 2)
	test.expect("TRACE", 0)
}

func TestCountProcessorForCategory(t *testing.T) {
	entry := func(category string) *domain.Entry {
		return &domain.Entry{Category: category}
	}
	var buffer bytes.Buffer
	processor := processors.NewCountProcessor([]string{"category"}, processors.Default, &buffer)

	processor.Before()
	processor.Execute(entry("lorem"))
	processor.Execute(entry("bacon"))
	processor.Execute(entry("bacon"))
	processor.Execute(entry("lorem"))
	processor.Execute(entry("meh"))
	processor.Execute(entry("bacon"))
	processor.Execute(entry("meh"))
	processor.Execute(entry("lorem"))
	processor.Execute(entry("bacon"))
	processor.After()

	test := CountTest{
		Counter: processor.Counters["category"].Values,
		T:       t,
	}

	test.expect("lorem", 3)
	test.expect("meh", 2)
	test.expect("bacon", 4)
	test.expect("foo", 0)
}
