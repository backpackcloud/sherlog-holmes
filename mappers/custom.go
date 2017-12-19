package mappers

import (
	"io/ioutil"

	"errors"
	"regexp"

	"gopkg.in/yaml.v2"
)

// Struct that represents a configuration for mapping log entries
type Config struct {
	// Sets the base of this configuration to inherit its properties
	From string
	// Holds the entry pattern
	Entry string
	// Holds the exception pattern
	Exception string
	// Holds the stacktrace pattern
	Stacktrace string
}

// Parses the given yaml file and adds it to the RegexpMappers
func ParseYaml(filePath string) error {
	data, err := ioutil.ReadFile(filePath)
	if err != nil {
		panic(err)
	}
	var config map[string]Config

	if err := yaml.Unmarshal(data, &config); err != nil {
		panic(err)
	}

	for id, conf := range config {
		mapper := RegexpMapper{}
		if conf.From != "" {
			if base, ok := RegexpMappers[conf.From]; ok {
				mapper.Entry = base.Entry
				mapper.Exception = base.Exception
				mapper.Stacktrace = base.Stacktrace
			} else {
				panic(errors.New("no configuration with id: " + conf.From))
			}
		}
		if conf.Entry != "" {
			mapper.Entry = regexp.MustCompile(conf.Entry)
		} else {
			return errors.New("No entry expression mapped for " + id)
		}
		if conf.Exception != "" {
			mapper.Exception = regexp.MustCompile(conf.Exception)
		}
		if conf.Stacktrace != "" {
			mapper.Stacktrace = regexp.MustCompile(conf.Stacktrace)
		}

		RegexpMappers[id] = mapper
		RegisteredMappers[id] = mapper
	}

	return nil
}
