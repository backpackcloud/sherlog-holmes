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

require 'yummi'
require 'yaml'

require_relative 'sherlog_holmes/version'
require_relative 'sherlog_holmes/result'
require_relative 'sherlog_holmes/entry'
require_relative 'sherlog_holmes/filter'
require_relative 'sherlog_holmes/parser'
require_relative 'sherlog_holmes/listeners/print_listener'
require_relative 'sherlog_holmes/listeners/count_listener'
require_relative 'sherlog_holmes/listeners/occurrence_listener'

module Sherlog

  PATTERNS = {}

  def self.load_patterns(file)
    patterns = YAML::load_file file
    patterns.each do |id, config|
      pattern_config = {}
      pattern_config[:entry] = Regexp::new(config['entry']) if config['entry']
      pattern_config[:exception] = Regexp::new(config['exception']) if config['exception']
      pattern_config[:stacktrace] = Regexp::new(config['stacktrace']) if config['stacktrace']
      pattern_config = PATTERNS[config['from'].to_sym].merge pattern_config if config['from']
      PATTERNS[id.to_sym] = pattern_config
    end
  end

  Dir['%s/../conf/patterns/*.yml' % File.dirname(__FILE__)].each do |file|
    load_patterns file
  end

  Dir['%s/.sherlog/patterns/*.yml' % ENV['HOME']].each do |file|
    load_patterns file
  end

  def self.parser(pattern_id)
    Parser::new PATTERNS[pattern_id.to_sym]
  end

  def self.loaded_patterns
    PATTERNS
  end

end

ENV['SHERLOG_FILE_ENCODE'] ||= Encoding.default_external.name
