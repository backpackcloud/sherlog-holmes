package processors

import (
	"encoding/json"
	"io"
)

// A formatter for json output
func Json(writer io.Writer, countMap map[string]EntryCount) {
	values := make(map[string]map[string]int64)
	for group, count := range countMap {
		values[group] = make(map[string]int64)
		for name, count := range count.Values {
			values[group][name] = count
		}
	}
	data, err := json.Marshal(values)
	if err != nil {
		panic(err)
	}
	writer.Write(data)
}
