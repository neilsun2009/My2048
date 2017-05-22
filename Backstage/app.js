// 返回的json格式
// {"username":"paper_crane","score":48999,"mode":1} 计时模式
// {"username":"paper_crane","score":48999,"mode":0} 普通模式

var express = require('express'),
    url = require('url'),
    path = require('path'),
    fs = require('fs'),
    bodyParser = require('body-parser'),
    MongoClient = require('mongodb').MongoClient;

var counterMinScore,
    normalMinScore,
    counterCount = 0,
    normalCount = 0,
    i,
    options = {
      encoding: 'utf8',
      flag: 'r'
    };

var app = express();

app.use(bodyParser());

// app.get('/index', getIndex);

// get Score
app.get('/score', getScore);

// post
app.post('/score', postScore);

// redirect all others to the index (HTML5 history)
app.get('*', redirect);

// Start server
app.listen(80, function(){
  console.log("Express server listening on port 80");
});

// 初始化
(function() {
  MongoClient.connect('mongodb://localhost/', function(err, db) {
    var myDB = db.db('RZ2048');
    myDB.collection('normal', function(err, doc) {
      doc.find(null, {sort:{'score': -1}, fields:{'_id':0}}, function(err, items) {
        items.toArray(function(err, itemArr) {
          if (itemArr.length != 0) {
            normalMinScore = itemArr[itemArr.length - 1].score;
            normalCount = itemArr.length;
          } else {
            normalMinScore = 10000000;
            normalCount = 0;
          }
        });
      });
    });
    myDB.collection('counter', function(err, doc) {
      doc.find(null, {sort:{'score': -1}, fields:{'_id':0}}, function(err, items) {
        items.toArray(function(err, itemArr) {
          if (itemArr.length != 0) {
            counterMinScore = itemArr[itemArr.length - 1].score;
            counterCount = itemArr.length;
          } else {
            counterMinScore = 10000000;
            counterCount = 0;
          }
        });
      });
    });
  });
})();

// 测试页面
function getIndex(req, res) {
  fs.readFile('index.html', options, function(err, data) {
    if (err) {
      res.send(err);
      res.end();
    } else {
      res.set('ContentType', 'text/html');
      res.status(200);
      res.send(data);
      res.end();
    }
  });
}

// 获取分数数据
function getScore(req, res) {
  var url_parts = url.parse(req.url, true);
  var query = url_parts.query;

  if (query.mode === '0') {
    handleGet(res, 'normal');
  } else {
    handleGet(res, 'counter');
  }
}

// 提交分数
function postScore(req, res) {
  if (req.body) {
    console.log(req.body);
    var username = req.body.username,
        score = parseInt(req.body.score),
        mode = parseInt(req.body.mode);

    if (typeof score === 'number' && typeof mode === 'number') {
      var item = {
        "username": username,
        "score": score,
      }
      handlePost(res, item, mode);
    } else {
      res.status('400');
      res.end('The type of score or mode is wrong');
    }
  } else {
    res.status('400');
    res.end('Lack of information');
  }
}

function redirect(req, res) {
  res.redirect('/score?mode=0');
}

function handlePost(res, item, mode) {
  if (mode === 1) {
    if (counterCount >= 10 && counterMinScore > item.score) {
      res.status(200);
      res.end();
    } else if (counterCount < 10) {
      // 直接写入
      DBPostOperation(res, item, 'counter', 0);
    } else {
      // 寻找最低分数，删除，然后写入
      DBPostOperation(res, item, 'counter', 1);
    }
  } else {
    if (normalCount >= 10 && normalMinScore > item.score) {
      res.status(200);
      res.end();
    } else if (normalCount < 10) {
      // 直接写入
      DBPostOperation(res, item, 'normal', 0);
    } else {
      // 寻找最低分数，删除，然后写入
      DBPostOperation(res, item, 'normal', 1);
    }
  }
}

function DBPostOperation(res, item, colName, opeMode) {
  MongoClient.connect('mongodb://localhost/', function(err, db) {
    var myDB = db.db('RZ2048');
    myDB.collection(colName, function(err, doc) {
      if (err) {
        res.status(500);
        res.end('DB Error');
      } else if (opeMode === 1) {
        // delete and insert
        var min = colName === 'normal' ? normalMinScore : counterMinScore;
        doc.findAndRemove({'score':min}, [], {w:1}, function(err, result) {
          if (err) {
            res.status(500);
            res.end('DB Error');
            return;
          }
          
          doc.insert(item, function(err, result) {

            if (err) {
              res.status(500);
              res.end('DB Error');
              return;
            }

            min = item.score;
            
            doc.find(function(err, items) {
              items.toArray(function(err, itemArr) {
                for (i = 0; i < itemArr.length; i += 1) {
                  if (itemArr[i].score < min) {
                    min = itemArr[i].score;
                  }
                }
                if (colName === 'normal') {
                  normalMinScore = min;
                } else {
                  counterMinScore = min;
                }
                res.status(200);
                res.end();
              });
            });

          });
        });
      } else {
        // insert
        doc.insert(item, function(err, result) {
          if (err) {
            res.status(500);
            res.end('DB Error');
          } else {
            if (colName === 'normal') {
              if (normalMinScore > item.score) {
                normalMinScore = item.score;
              }
              normalCount += 1;
            } else {
              if (counterMinScore > item.score) {
                counterMinScore = item.score;
              }
              counterCount += 1;
            }
            res.status(200);
            res.end();
          }
        });
      }
    });
  });
}

// 获取数据，返回
function handleGet(res, colName) {
  MongoClient.connect('mongodb://localhost/', function(err, db) {
    var myDB = db.db('RZ2048');
    myDB.collection(colName, function(err, doc) {
      if (err) {
        res.status(500);
        res.end('DB Error');
        return;
      }

      doc.find(null, {sort:{'score': -1}, fields:{'_id':0}}, function(err, items) {
        if (err) {
          res.status(500);
          res.end('DB Error');
          return;
        }

        items.toArray(function(err, itemArr) {
          if (err) {
            res.status(500);
            res.end('DB Error');
            return;
          }

          var obj = {
            scores: itemArr
          };
          res.status(200);
          res.json(obj);
          res.end();
        });
      });
    });
  });
}