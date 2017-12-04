package processors

import "github.com/devnull-tools/sherlog-holmes/domain"

type CountProcessor struct {
	Groups []string
}

func (processor CountProcessor) After() {

}

func (processor CountProcessor) Before() {

}

func (processor CountProcessor) Execute(entry *domain.Entry) {

}
