//	basicServer.js

var MongoClient = require('mongodb').MongoClient;

var url = "mongodb://localhost/test";

function callback(err, db){
	if(err){
		console.log('Unable to connect to the mongoDB server. Error:', err);
	} else {
		console.log("Connection established to ", url);
		
		//Get the documents collection 
		var collection = db.collection('users');

		//Create some users
		var user1 = {user_id: "ChangHyun", age: 35, status: "S"};
		var user2 = {user_id: "YeonJu", age: 35, status: "S"};

		//Insert some users
		collection.insert([user1, user2], function (err, result) {
			if(err){
				console.log(err);
			} else {
				console.log('Inserted documents into the "users" collection.');
			}
		});
		
		//Querying some users age is 35
		collection.find({age: 35}).toArray(function (err, result){
			if(err){
				console.log(err);
			} else if (result.length){
				console.log('Found: ', result);
				
			} else {
				console.log('No document(s) found with defined "find" criteria!');
			}

			//Close connection
			db.close();
		});

	}
}

MongoClient.connect(url, callback);

//Below is the Webserver part
var http = require('http');
http.createServer(function (req, res){
	res.writeHead(200, {'Content-Type': 'text/html'});
	res.end("Hello World hello too");
}).listen(1336);
console.log('Server running at http://127.0.0.1:1336/');
