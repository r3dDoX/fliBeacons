var socket = io.connect();
socket.emit('ready');
socket.on('ack', function (data) {
	console.log(data);
});
