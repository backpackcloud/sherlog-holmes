package readers

import (
	"bufio"
	"os"
)

type Reader interface {
	Read(out chan<- string)
}

type FileReader struct {
	File string
}

func (reader FileReader) Read(out chan<- string) {
	fileHandle, err := os.Open(reader.File)
	if err != nil {
		panic(err)
	}
	defer fileHandle.Close()
	fileScanner := bufio.NewScanner(fileHandle)
	for fileScanner.Scan() {
		out <- fileScanner.Text()
	}
	close(out)
}
