/*

Copyright (C) 2016 Freshdesk, Inc.

This source code is a part of the Freshdesk SDK and is covered by the our license
terms. For details about this license, please read the LICENSE.txt which is
bundled with this source code.

*/

'use strict';

var sass = require('node-sass');
var fs   = require('fs');

module.exports = {
  // Compile scss to css and return as string
  compileStr: function(src) {
    var result = sass.renderSync({
      file: src
    });
    return result.css.toString('utf8');
  },
  // Compile scss file to css file
  compile: function(src, dest) {
    var result = sass.renderSync({
      file: src
    });
    fs.writeFileSync(dest, result.css);
  }
};
