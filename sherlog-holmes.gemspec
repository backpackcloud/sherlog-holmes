# coding: utf-8
lib = File.expand_path('../lib', __FILE__)
$LOAD_PATH.unshift(lib) unless $LOAD_PATH.include?(lib)
require 'sherlog_holmes/version'

Gem::Specification.new do |spec|
  spec.name          = 'sherlog-holmes'
  spec.version       = Sherlog::VERSION
  spec.authors       = ['Ataxexe']
  spec.email         = ['ataxexe@devnull.tools']

  spec.summary       = %q{The best companion of a log detective!}

  spec.homepage      = 'https://github.com/devnull-tools/sherlog-holmes'
  spec.license       = 'MIT'

  spec.files         = `git ls-files -z`.split("\x0").reject { |f| f.match(%r{^(test|spec|features)/}) }
  spec.bindir        = 'bin'
  spec.executables   = spec.files.grep(%r{^bin/}) { |f| File.basename(f) }
  spec.require_paths = ['lib']

  spec.required_ruby_version = '>= 2.0'

  spec.add_dependency 'yummi', '~> 0.9.5'

  spec.add_development_dependency 'bundler', '~> 1.10'
  spec.add_development_dependency 'rake', '~> 12.0'
  spec.add_development_dependency 'rspec', '~> 3.4.0'
end
