var expect = require('chai').expect;
var helper = require('./helper');
var fs   = require('fs');

describe("test pack", function(){
  before(function(){
    process.chdir(projectDir['name']);
  });

  it("should generate the package in dist/ dir.", function(done){
    require(__dirname + '/../lib/cli-pack').run();
    setTimeout(function(){
      fs.accessSync(process.cwd() + '/dist/freshapps_sdk.plg');
      done();
    },50);
  });

  it("should clean dirs", function(done) {
    require(__dirname + '/../lib/cli-clean').run();
    done();
  }); 
});