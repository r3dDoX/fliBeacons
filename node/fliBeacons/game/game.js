var drones = [{uuid: 'B9407F30-F5F8-466E-AFF9-25556B57FE6D', major: '51881', minor: '16836'}, {uuid: '', major: '', minor: ''}],
    baseStations = [],
    activeStation = 0,
    gameState = {},
    activateEvent = 'activate',
    finishedEvent = 'finished',
    updateEvent = 'update',
    
    isRunning = function() {
        return gameState.isRunning;
    },
    
    initRanking = function() {
        return drones.map(function(element) {
            return {
                drone: element,
                points: 0
            };
        });
    },
    
    initHistory = function() {
        return ['Game started'];
    },
    
    addToHistory = function(text) {
        gameState.history.push(text);
    },
    
    initGameState = function() {
        gameState = {};
        gameState.isRunning = true;
        gameState.ranking = initRanking();
        gameState.history = initHistory();
    },
    
    startCourse = function(req, baseStationOrder) {
        baseStations = baseStationOrder;
        activeStation = 0;
        initGameState();
        
        if (baseStations.length > 0) {
            req.io.manager.sockets.emit(activateEvent, baseStations[activeStation]);
        }
    },
    
    updateRanking = function(drone) {
        gameState.ranking.map(function(element) {
            if (element.drone.uuid === element.uuid) {
                element.points += 1;
            }
            
            return element;
        }).sort(function(a, b) {
            return a.points > b.points;
        });
    },
    
    moveOn = function(req, drone) {
        var sockets = req.io.manager.sockets;
        updateRanking(drone);
        addToHistory('Drone ' + drone.beacon.uuid + ' got Station ' + baseStations[activeStation].id);
        
        if (activeStation === baseStations.length -1) {
            addToHistory('Game finished');
            sockets.emit(finishedEvent, gameState);
        } else {
            sockets.emit(activateEvent, baseStations[++activeStation]);
            sockets.emit(updateEvent, gameState);
        }
    },
    
    checkGameState = function(req, drone) {
        if (drone.baseStationId === baseStations[activeStation].id && 
            drone.proximity === 'near' || drone.proximity === 'immediate') {
            moveOn(req, drone);
        }
    };

exports.game = {
    startCourse: startCourse,
    checkGameState: checkGameState,
    isRunning: isRunning
};