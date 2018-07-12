'use strict';

const program = require('commander');

const launchCLI = () => {
  program
    .command('deploy')
    .description('Deploy Flow DSL into workflow engine.')
    .version('0.1.0')
    .parse(process.argv)

  let path = process.argv[process.argv.length - 1];

  return path
}

module.exports = launchCLI;