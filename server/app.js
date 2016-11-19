const app = require('express')();
const http = require('http').Server(app);
const io = require('socket.io')(http, { serveClient: false });
const fb = require('./routes/fb');

fb.init();

app.get('/', function(req, res) {
  res.send('<h1>FireStream Server</h1>');
});

io.on('connection', function(socket){
  socket.on('sendNotification', (data) => {
    let success = fb.sendMessage(data);
    socket.emit('sendNotification', success);
  });
  console.log('a user connected');
});

http.listen(3000, function(){
  console.log('listening on *:3000');
});
