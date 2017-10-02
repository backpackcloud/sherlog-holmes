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

require_relative 'entry'

module Sherlog
  class Parser

    def initialize(patterns = {}, filter = nil)
      @filter = filter
      @patterns = {
          exception: /^$/,
          stacktrace: /^$/
      }.merge patterns
      @filter ||= filter { |entry| true }
      @listeners = []
    end

    def collect
      result = Result::new
      on_new_entry do |entry|
        result << entry
      end
      result
    end

    def on_new_entry(listener = nil, &block)
      listener ||= block
      @listeners << listener
    end

    def filter(filter = nil, &block)
      @filter = filter if filter
      @filter = Filter::new &block if block
    end

    def patterns(config)
      @patterns.merge! config
    end

    def parse(input)
      entry = nil
      process = ParserProcess::new
      foreach input do |line|
        try_guess_pattern line unless @patterns[:entry]
        if @patterns[:entry] =~ line
          entry_data = Hash[Regexp.last_match.names.map { |k| [k.to_sym, Regexp.last_match[k].to_s.strip] }]
          # notify the last entry parsed
          notify entry if entry and @filter.accept? entry
          entry = Entry::new entry_data
          entry.process = process
          entry.raw_content = line.chomp
          entry.exceptions << Regexp.last_match[:exception] if @patterns[:exception] =~ entry.message
        else
          if entry
            if entry.exception? and @patterns[:stacktrace] =~ line
              entry.stacktrace << line.chomp
            else
              entry << line.chomp
            end
            entry.exceptions << Regexp.last_match[:exception] if @patterns[:exception] =~ line
            entry.raw_content << $/ << line.chomp
          end
        end
        break if process.stop_requested?
      end
      # notify the last entry parsed
      notify entry if entry and @filter.accept? entry
    end

    private

    def foreach(input, &block)
      if File.exist? input
        IO.foreach input, encoding: ENV['SHERLOG_FILE_ENCODE'], &block
      else
        input.each_line &block
      end
    end

    def notify(entry)
      return if entry.process.stop_requested?
      @listeners.each do |listener|
        listener.call entry
      end
    end

    def try_guess_pattern(line)
      key, patterns = Sherlog.loaded_patterns.find do |key, patterns|
        patterns[:entry].match line if patterns[:entry]
      end
      @patterns.merge! patterns if patterns
    end

  end

  class ParserProcess

    def stop
      @request_stop = true
    end

    def stop_requested?
      @request_stop
    end

  end
end