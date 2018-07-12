'use strict';

const request = require('request');

const deploy = (address, source) => {
  return new Promise((resolve, reject) => {
    request.post({ url: address, headers: { 'content-type': 'application/json' }, body: JSON.stringify(source) }, (err, response, body) => {
      if (err) reject(err);
      resolve(body);
    })
  })
}

module.exports = deploy