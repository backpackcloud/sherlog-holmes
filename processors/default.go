package processors

import (
	"fmt"
	"io"
	"math"
	"strconv"
	"strings"
)

func Default(writer io.Writer, countMap map[string]EntryCount) {
	for group, count := range countMap {
		// determine the most lengthy value
		var maxLenKey int
		var maxLenValue int
		for name, count := range count.Values {
			maxLenKey = int(math.Max(float64(maxLenKey), float64(len(name))))
			maxLenValue = int(math.Max(float64(maxLenValue), float64(len(strconv.FormatInt(count, 10)))))
		}
		writer.Write([]byte(strings.Title(group)))
		writer.Write([]byte("\n"))
		format := fmt.Sprintf("%%-%ds: %%%dd", maxLenKey+1, maxLenValue)
		for name, count := range count.Values {
			writer.Write([]byte(fmt.Sprintf(format, name, count)))
			writer.Write([]byte("\n"))
		}
		writer.Write([]byte("\n"))
	}
}
