package readers

import (
	"bufio"
	"os"
)

// Interface that defines a reader, which is capable of read strings from a source and sends them
// to the pipeline for further processing
type Reader interface {

	// Reads the input source and sends the contents to the given channel
	Read(out chan<- string)
}

// Defines a file reader
type FileReader struct {

	// The file name
	File string
}

// Reads the file and sends each line to the given channel
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
