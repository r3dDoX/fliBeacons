var ready = function(req) {
	req.io.emit('ack', {
		type: "ready",
		message: "acknowledged readyness of client"
	});
};

exports.handlers = {
	ready: ready
};