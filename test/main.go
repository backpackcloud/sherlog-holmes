package test

import (
	"bufio"
	"strings"

	"github.com/devnull-tools/sherlog-holmes/domain"
	"github.com/devnull-tools/sherlog-holmes/mappers"
)

type MapperTest struct {
	Mapper mappers.Mapper
}

func (mapperTest *MapperTest) Test(lines string) chan *domain.Entry {
	lineChan := make(chan string)
	entryChan := make(chan *domain.Entry)

	go mapperTest.Mapper.Map(lineChan, entryChan)

	go func() {
		scanner := bufio.NewScanner(strings.NewReader(lines))
		for scanner.Scan() {
			lineChan <- scanner.Text()
		}
		close(lineChan)
	}()
	return entryChan
}
