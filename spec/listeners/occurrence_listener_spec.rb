#                         The MIT License
#
# Copyright (c) 2015 Marcelo "Ataxexe" Guimarães <ataxexe@devnull.tools>
#
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
# THE SOFTWARE.

require 'spec_helper'

describe OccurrenceListener do

  before(:each) do
    @max = 3
    @listener = OccurrenceListener::new @max
  end

  it 'should request stop after the number of entries' do
    process = double ParserProcess
    allow(process).to receive(:stop)
    expect(process).to receive(:stop).once

    entry = Entry::new
    entry.process = process

    @max.times do
      @listener.call entry
    end
  end

  it 'should never request stop if the number of entries is less than the configured value' do
    process = double ParserProcess
    allow(process).to receive(:stop)
    expect(process).to receive(:stop).never

    entry = Entry::new
    entry.process = process

    (@max - 1).times do
      @listener.call entry
    end
  end

  it 'should stop the parsing process after requesting stop' do
    parser = Parser::new
    parser.patterns entry: /(?<message>.+)/
    output = ''
    parser.on_new_entry @listener
    parser.on_new_entry PrintListener::new(output)
    parser.parse <<EOL
1- Lorem ipsum dolor sherlog amet
2- Lorem ipsum dolor sherlog amet
3- Lorem ipsum dolor sherlog amet
4- Lorem ipsum dolor sherlog amet
5- Lorem ipsum dolor sherlog amet
6- Lorem ipsum dolor sherlog amet
EOL
    expect(output).to eq <<EOL
1- Lorem ipsum dolor sherlog amet
2- Lorem ipsum dolor sherlog amet
3- Lorem ipsum dolor sherlog amet
EOL
  end

end
