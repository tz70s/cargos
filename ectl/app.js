'use strict';

const source = require('./lib/source');
const launchCLI = require('./lib/cli_parser');
const deploy = require('./lib/request_handler');

let path = launchCLI();

source(path).then((sourceObj) => {
  deploy('http://35.224.21.66/deploy', sourceObj)
    .then((body) => console.log(body))
    .catch((err) => console.error(err));
});
