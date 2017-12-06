package processors

import (
	"encoding/csv"
	"io"
	"strconv"
)

func Csv(writer io.Writer, countMap map[string]EntryCount) {
	w := csv.NewWriter(writer)
	for _, count := range countMap {
		for name, total := range count.Values {
			if err := w.Write([]string{name, strconv.FormatInt(total, 10)}); err != nil {
				panic(err)
			}
		}
	}
	w.Flush()
}
