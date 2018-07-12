'use strict';

const fs = require('fs');

const source = (path) => {
  return new Promise((resolve, reject) => {
    fs.readFile(path, 'utf8', (err, content) => {
      if (err) { reject(`Can't find source ${err}`); }
      resolve({ 'source': content + "\n" })
    })
  })
}

module.exports = source;