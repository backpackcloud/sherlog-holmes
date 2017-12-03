package domain

import "bytes"

// Represents a log entry. Log entries can have a lot of different fields, this struct tries to balance the most used
// fields for formatting and filtering
type Entry struct {
	Line       int64
	Time       string
	Level      string
	Category   string
	Origin     string
	Exceptions []string
	stacktrace bytes.Buffer
	message    bytes.Buffer
	raw        bytes.Buffer
}

// Returns the raw content of this entry
func (entry *Entry) RawContent() string {
	return entry.raw.String()
}

// Returns the message part of this entry
func (entry *Entry) Message() string {
	return entry.message.String()
}

// Returns the stacktrace part of this entry
func (entry *Entry) Stacktrace() string {
	return entry.stacktrace.String()
}

// Adds the given exception to this log entry
func (entry *Entry) AddException(exception string) {
	entry.Exceptions = append(entry.Exceptions, exception)
}

// Adds the given line as a stacktrace to this entry
func (entry *Entry) AddStacktrace(line string) {
	entry.stacktrace.WriteString("\n")
	entry.stacktrace.WriteString(line)
	entry.Append(line)
}

// Appends the given line as part of the raw content of the entry
func (entry *Entry) Append(line string) {
	entry.raw.WriteString("\n")
	entry.raw.WriteString(line)
}

// Sets the raw content of this entry to the given value
func (entry *Entry) SetContent(content string) {
	entry.raw = *bytes.NewBufferString(content)
}

// Sets the message of this entry to the given value
func (entry *Entry) SetMessage(message string) {
	entry.message = *bytes.NewBufferString(message)
}

// Appends the given text to the message of this entry
func (entry *Entry) AppendMessage(message string) {
	entry.message.WriteString(message)
}
