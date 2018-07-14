#!/usr/bin/env node

'use strict';

const source = require('./lib/source');
const launchCLI = require('./lib/cli_parser');
const deploy = require('./lib/request_handler');

let parse = launchCLI();

console.log(`deploy to engine: http://${parse.engine.trim()}/deploy, source: ${parse.source}`);

source(parse.source).then((sourceObj) => {
  deploy(`http://${parse.engine}/deploy`, sourceObj)
    .then((body) => console.log(body))
    .catch((err) => console.error(err));
});
