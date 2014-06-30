var sockets = {},
    ready = function(req) {
        sockets[req.socket.id] = req.socket;

        req.io.emit('ack', {
            type: 'ready',
            message: 'Hello there'
        });
    }, 
    
    disconnect = function(req) {
        delete sockets[req.socket.id];
    };

exports.handlers = {
	ready: ready,
    disconnect: disconnect
};