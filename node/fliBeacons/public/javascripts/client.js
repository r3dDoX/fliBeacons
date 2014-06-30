var socket = io.connect();
socket.emit('ready');
socket.on('talk', function (data) {
	console.log(data);
});
