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

describe 'JBoss Patterns' do

  describe '#parse' do

    def exception(name)
      '18:50:42,129 ERROR  [org.jboss.modules] (main) %s' % name
    end

    before(:each) do
      @filter = double Filter
      allow(@filter).to receive(:accept?).and_return(true)

      @parser = Sherlog::parser 'jboss.wildfly'
      @parser.filter @filter
      @result = @parser.collect
    end

    it 'should parse the lines correctly' do
      @parser.parse '18:50:42,129 INFO  [org.jboss.modules] (main) JBoss Modules version 1.3.6.Final-redhat-1'

      entry = @result.entries[0]
      expect(entry.time).to eq('18:50:42,129')
      expect(entry.level).to eq('INFO')
      expect(entry.category).to eq('org.jboss.modules')
      expect(entry.origin).to eq('main')
      expect(entry.message).to eq('JBoss Modules version 1.3.6.Final-redhat-1')
    end

    it 'should accept the date if present' do
      @parser.parse '2015-12-01 18:50:42,129 INFO  [org.jboss.modules] (main) JBoss Modules version 1.3.6.Final-redhat-1'
      entry = @result.entries.first
      expect(entry.time).to eq('2015-12-01 18:50:42,129')
      expect(entry.level).to eq('INFO')
      expect(entry.category).to eq('org.jboss.modules')
      expect(entry.origin).to eq('main')
      expect(entry.message).to eq('JBoss Modules version 1.3.6.Final-redhat-1')
      expect(entry[:date]).to eq('2015-12-01')
    end

    it 'should accept entries that uses a | between attributes' do
      @parser.parse '2016-02-03 17:44:15,003 | INFO  | [org.jboss.as] | (MSC service thread 1-8:) | JBAS015899: Iniciando JBoss EAP 6.1.0.GA (AS 7.2.0.Final-redhat-8)'
      entry = @result.entries.first
      expect(entry.time).to eq('2016-02-03 17:44:15,003')
      expect(entry.level).to eq('INFO')
      expect(entry.category).to eq('org.jboss.as')
      expect(entry.origin).to eq('MSC service thread 1-8:')
      expect(entry.message).to eq('JBAS015899: Iniciando JBoss EAP 6.1.0.GA (AS 7.2.0.Final-redhat-8)')
      expect(entry[:date]).to eq('2016-02-03')
    end

  end

end