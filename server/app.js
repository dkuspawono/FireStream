const app = require('express')();
const http = require('http').Server(app);
const io = require('socket.io')(http);
const fb = require('./routes/fb');

fb.init();

app.get('/', function(req, res) {
	res.send('<h1>FireStream Server</h1>');
});

var clients = [];
io.on('connection', function(socket) {
	console.log('a user connected');

	socket.on('sendNotification', (data) => {
		let success = fb.sendMessage(data);
		socket.emit('sendNotification', success);
	});
	socket.on('joinParty', (data) => {
        console.log('joinParty ' + socket.id);
		fb.joinParty(data, socket.id);
	});
	socket.on('disconnect', () => {
        console.log('leaveParties ' + socket.id);
        fb.leaveParties(socket.id);
	});
});

var port = process.env.PORT || 3000;
http.listen(port, function() {
	console.log('listening on *:' + port);
});
