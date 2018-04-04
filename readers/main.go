package readers

import (
	"bufio"
	"os"
	"github.com/devnull-tools/sherlog-holmes/domain"
)

// Interface that defines a reader, which is capable of read strings from a source and sends them
// to the pipeline for further processing
type Reader interface {
	// Reads the input source and sends the contents to the given channel
	Read(out chan<- *domain.Line)
}

// Defines a file reader
type FileReader struct {
	// The file names
	Files []string
}

// Reads the file and sends each line to the given channel
func (reader FileReader) Read(out chan<- *domain.Line) {
	for _, file := range reader.Files {
		fileHandle, err := os.Open(file)
		if err != nil {
			panic(err)
		}
		fileScanner := bufio.NewScanner(fileHandle)
		var i int64 = 0
		for fileScanner.Scan() {
			i++
			out <- &domain.Line{
				Content:  fileScanner.Text(),
				Filename: file,
				Index:    i,
			}
		}
		fileHandle.Close()
	}
	close(out)
}
