/* HashMap */
var JqMap = function(){
    this.map = new Object();
}
 
JqMap.prototype = {
    put: function (key, value) {
        this.map[key] = value;
    },
    get: function (key) {
        return this.map[key];
    },
    containsKey: function (key) {
        return key in this.map;
    },
    containsValue: function (value) {
        for (var prop in this.map) {
            if (this.map[prop] == value) {
                return true;
            }
        }
        return false;
    },
    clear: function () {
        for (var prop in this.map) {
            delete this.map[prop];
        }
    },
    remove: function (key) {
        delete this.map[key];
    },
    keys: function () {
        var arKey = new Array();
        for (var prop in this.map) {
            arKey.push(prop);
        }
        return arKey;
    },
    values: function () {
        var arVal = new Array();
        for (var prop in this.map) {
            arVal.push(this.map[prop]);
        }
        return arVal;
    },
    size: function () {
        var count = 0;
        for (var prop in this.map) {
            count++;
        }
        return count;
    }
}

// Setup basic express server
var express = require('express');
var app = express();
var server = require('http').createServer(app);
var io = require('socket.io')(server);
var port = process.env.PORT || 3000;

// MongoDB part
var MongoClient = require('mongodb').MongoClient;
var url = "mongodb://localhost/test";


server.listen(port, function () {
  console.log('Server listening at port %d', port);
});

// Routing
app.use(express.static(__dirname + '/public'));

// Chatroom

var oMap = new JqMap();
var numUsers = 0;

io.on('connection', function (socket) {
  var addedUser = false;

  // when the client emits 'new message', this listens and executes
  socket.on('new message', function (data) {
    console.log('[new message] socket.position = %s', socket.position);

    // save data to db
    MongoClient.connect (url,
	function (err, db) {
	    if(err){
		console.log ('Error :', err);
	    }
	    else {
	    	db.collection("message").insert({
		    username: socket.username,
		    body: data,
		    position: socket.position
		    }, function (e, result) {
		    	console.log(e);
			console.log(result);
		    	db.close();
		    });
	    }
	}
    );

    // we tell the client to execute 'new message'
    socket.broadcast.to(socket.position).emit('new message', {
      username: socket.username,
      message: data
    });
  });

  // when the client emits 'add user', this listens and executes
  socket.on('add user', function (username, position) {
    if (addedUser) return;

    // we store the username in the socket session for this client
    socket.username = username;
    socket.position = position;
    socket.join(position);
	console.log ('[add user] socket.position = %s', socket.position);
    if (oMap.containsKey (position)) {
	var num = oMap.get(position);
	oMap.remove(position);
	oMap.put(position, ++num);
    }
    else {
	oMap.put(position, 1);
    }
    ++numUsers;
    addedUser = true;

    // Get recent 20 messages and return to logined user
    MongoClient.connect(url,
	    function (err, db) {
		if(err) {
		     console.log ('Error :', err);
		}
		else{	    
		    db.collection("message").find({position: socket.position}).toArray(function (err, items) {
		    if (err) {
			console.log(err);
		    }
		    else {
			var cnt = 0;
		        for (var i in items) {
		    	    if (cnt++ > items.length-21) {
			    socket.emit('new message', {
			        username: items[i].username,
			        message: items[i].body
		    	    });
			    }
		        }
		    }
		    db.close();
		    });}
	    });


    socket.emit('login', {
      numUsers: oMap.get(socket.position)
    });
    // echo globally (all clients) that a person has connected
    socket.broadcast.to(socket.position).emit('user joined', {
      username: socket.username,
      numUsers: oMap.get(socket.position)
    });
  });

  // when the client emits 'typing', we broadcast it to others
  socket.on('typing', function () {
    socket.broadcast.to(socket.position).emit('typing', {
      username: socket.username
    });
  });

  // when the client emits 'stop typing', we broadcast it to others
  socket.on('stop typing', function () {
    socket.broadcast.to(socket.position).emit('stop typing', {
      username: socket.username
    });
  });

  // when the user disconnects.. perform this
  socket.on('disconnect', function () {
    if (addedUser) {
      --numUsers;
      var num = oMap.get(socket.position);
      oMap.remove(socket.position);
      oMap.put (socket.position, --num);
      socket.leave(socket.position);

      // echo globally that this client has left
      socket.broadcast.to(socket.position).emit('user left', {
        username: socket.username,
        numUsers: oMap.get(socket.position)
      });
    }
  });
});
