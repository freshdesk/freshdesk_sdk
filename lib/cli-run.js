var fs = require('fs-extra');
var versions = require(__dirname + '/versions');
var manifest = require(__dirname + '/manifest');

var HTTP_PORT = 10001;
var EXTN_VERSION_HEADER = "FAExtnVersion";

exports.run = function() {
  if(global.verbose) {
    console.log('starting run...');
  }

  var express = require('express');
  var app = express();
  var http = require('http').Server(app);
  var WebSocketServer = require('ws').Server;
  var wss = new WebSocketServer({ server: http });

  // Register middleware:
  var bodyParser = require('body-parser');
  app.use(bodyParser.json());

  // assets:
  app.use('/assets', express.static('./assets'));

  // version.json:
  app.get('/version.json', function(req, res){
    res.json(
      {
        "sdk-version": "" + global.pjson.version,
        "platform-version": manifest.mf['platform-version']
      }
    );
  });

  app.get('/version/compatible/:extnVer', function(req, res){
    var extnVer = req.params.extnVer;
    console.log('version: ' + extnVer);
    if(extnVer) {
      if(versions.isCompatible(global.pjson.version, extnVer)) {
        res.status(200);
        res.send();
        return;
      }
    }
    res.status(400);
    res.send();
  });

  // code / config change notification:
  var watcher = require(__dirname + '/watcher');
  wss.on('connection', function(ws){
    var url = ws.upgradeReq.url;
    if(global.verbose) {
      console.log('[ws:/%s conn]', url);
    }
    ws.on('message', function incoming(message) {
      if(global.verbose) {
        console.log('[ws:/%s receive]: %s', url, message);
      }
    });
  });
  watcher.watch(function(data){
    wss.clients.forEach(function each(client) {
      client.send(data);
    });
  });

  // Plug path:
  var plugExec = function(req, res) {
    // header validation:
    var extnVer = req.get(EXTN_VERSION_HEADER);
    if(extnVer) {
      if(!versions.isCompatible(global.pjson.version, extnVer)) {
        console.error('Request from incompatible browser extn: ' + extnVer);
        res.status(400);
        res.send();
        return;
      }
    }
    else {
      console.error('Version header `' + EXTN_VERSION_HEADER + '\' not available in request.');
      res.status(400);
      res.send();
      return;
    }

    if(global.verbose) {
      console.log('### Plug request:');
      console.log(req.body);
    }

    res.send('<h2>Hello World!</h2>');
  }
  var PLUG_PATH = '/plug/*';
  app.get(PLUG_PATH, plugExec);
  app.post(PLUG_PATH, plugExec);

  // Finally, listen:
  http.listen(HTTP_PORT, function(){
    console.log('listening on *:' + HTTP_PORT);
  });
}