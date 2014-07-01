var drones = [{uuid: 'B9407F30-F5F8-466E-AFF9-25556B57FE6D', major: '51881', minor: '16836'}, {uuid: '', major: '', minor: ''}],
    baseStations = [],
    activeStation = 0,
    gameState = {},
    activateEvent = 'activate',
    
    isRunning = function() {
        return gameState.isRunning;
    },
    
    initGameState = function() {
        gameState = {};
        gameState.isRunning = true;
        gameState.ranking = [];
        gameState.history = [];
    },
    
    startCourse = function(req, baseStationOrder) {
        baseStations = baseStationOrder;
        activeStation = 0;
        initGameState();
        
        if (baseStations.length > 0) {
            req.io.manager.sockets.emit(activateEvent, baseStations[activeStation]);
        }
    },
    
    checkGameState = function(req, drone) {
        if (drone.baseStationId === baseStations[activeStation].id && drone.proximity === 'immediate') {
            req.io.manager.sockets.emit(activateEvent, baseStations[++activeStation]);
        }
    };

exports.game = {
    startCourse: startCourse,
    checkGameState: checkGameState,
    isRunning: isRunning
};