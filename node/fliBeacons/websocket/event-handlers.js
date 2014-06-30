var sockets = {},
    ready = function(req) {
        sockets[req.socket.id] = req.socket;

        req.io.emit('ack', {
            type: 'ready',
            message: 'Hello there'
        });
    }, 
    
    close = function(req) {
        delete sockets[req.socket.id];
    };

exports.handlers = {
	ready: ready,
    close: close
};