'use strict';

const source = require('./lib/source');
const launchCLI = require('./lib/cli_parser');
const deploy = require('./lib/request_handler');

let path = launchCLI();

source(path).then((sourceObj) => {
  deploy('http://127.0.0.1:8080/deploy', sourceObj)
    .then((body) => console.log(body))
    .catch((err) => console.error(err));
});