'use strict';

const program = require('commander');

const launchCLI = () => {
  let source;
  let engine;

  program
    .description('Deploy a Flow DSL source file into remote workflow engine.')
    .version('0.1.3')
    .option('-e, --engine [address]', "address of workflow engine")

  program
    .command('deploy [source]')
    .description('deploy a flow source file to remote engine.')
    .action((_source) => {
      source = _source
    })

  program.parse(process.argv)

  if (program.args.length < 1) {
    program.outputHelp();
    process.exit();
  }
  if (program.engine) {
    console.log(`specifying engine at ${program.engine}`);
    engine = program.engine;
  } else {
    engine = 'localhost:8080';
  }

  return { source, engine }
}

module.exports = launchCLI;