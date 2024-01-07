'use strict';

var usernamePage = document.querySelector('#username-page');
var managementPage = document.querySelector('#management-page');
var usernameForm = document.querySelector('#usernameForm');
var messageArea = document.querySelector('#messageArea');
var fleetLog = document.querySelector('#fleetLog');
var connectingElement = document.querySelector('.connecting');

var addRouteButton = document.getElementById('addRouteButton');
var removeRouteButton = document.getElementById('removeRouteButton');

var stompClient = null;
var username = null;

var colors = [
    '#2196F3', '#32c787', '#00BCD4', '#ff5652',
    '#ffc107', '#ff85af', '#FF9800', '#39bbb0'
];

/* Needed variables */

let selectedItem = null;
let selectedAgent = null;
let selectedTrip = null;
let agents = null;

let pathPoints = [];
const d = 50;

/* Canvas drawing variables */
const canvas = document.getElementById('canvas');
const ctx = canvas.getContext('2d');
let isDrawing = false;
let lastX = 0;
let lastY = 0;
let currentX = null
let currentY = null

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        managementPage.classList.remove('hidden');

        var socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}


function onConnected() {
    // Subscribe to the Public Topic
    stompClient.subscribe('/topic/public', onMessageReceived);
    stompClient.subscribe('/topic/admin.newAgent', onAgentConnected);
    stompClient.subscribe('/topic/admin.updateAgent', updateAgent);
    stompClient.subscribe(`/user/${username}/queue/agentStates`, updateAgentsList);

    /* Register the client at the server */
    stompClient.send("/app/agent.addUser",
        {},
        JSON.stringify({sender: username, type: 'JOIN'})
    )

    /* Request the server to send a list of currently connected agents */
    stompClient.send("/app/agent.getAgents",
        {},
        JSON.stringify({sender: username})
    )

    connectingElement.classList.add('hidden');

    setupAddRouteButton();
    setupRemoveRouteButton();
}


function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}

function sendTripCommand(agentId, route, delay, action) {
    var currentDate = new Date();
    var currentTimestamp = currentDate.getTime();
    var midnightToday = new Date(currentDate);
    midnightToday.setHours(0, 0, 0, 0);
    var midnightTimestamp = midnightToday.getTime();
    var departureTime = Math.floor((currentTimestamp - midnightTimestamp) / 1000) + delay;

    if (stompClient) {
        var command = {
            agentId: agentId,
            action: action,
            trip: {
                route: route,
                departureTime: departureTime
            }
        };
        pathPoints = [];
        stompClient.send("/app/command", {}, JSON.stringify(command));
    }
    event.preventDefault();
}


function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' connected.';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' disconnected.';
    } else {
        messageElement.classList.add('fleet-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);
        avatarElement.style['background-color'] = getAvatarColor(message.sender);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    messageArea.appendChild(messageElement);
    messageArea.scrollTop = messageArea.scrollHeight;
}

function onAgentConnected(payload) {
    var agent = JSON.parse(payload.body);
    agents.push(agent);
    populateAgentList();

    var messageElement = document.createElement('li');

    messageElement.classList.add('event-message');


    var textElement = document.createElement('p');
    var messageText = document.createTextNode(agent.id);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    fleetLog.appendChild(messageElement); // NULL
    fleetLog.scrollTop = fleetLog.scrollHeight;
}

function updateAgent(payload) {
    let updatedAgent = JSON.parse(payload.body);
    agents.map(agent => {
        if (agent.id === updatedAgent.id) {
            Object.keys(agent).forEach(key => {
                if (updatedAgent.hasOwnProperty(key) && updatedAgent[key] !== null) {
                    agent[key] = updatedAgent[key];
                }
            });
            displayAgentDetails(agent);
        }
        return agent;
    });
}

function updateAgentsList(payload) {
    agents = JSON.parse(payload.body);
    populateAgentList();
}


function getAvatarColor(messageSender) {
    var hash = 0;
    for (var i = 0; i < messageSender.length; i++) {
        hash = 31 * hash + messageSender.charCodeAt(i);
    }
    var index = Math.abs(hash % colors.length);
    return colors[index];
}

function initCanvas() {
    canvas.addEventListener('mousedown', startDrawing);
    canvas.addEventListener('mousemove', draw);
    canvas.addEventListener('mouseup', stopDrawing);
    canvas.addEventListener('mouseout', stopDrawing);
}

function drawPath() {
    if (canvas.getContext && pathPoints.length > 0) {
        ctx.beginPath();
        // Draw line

        ctx.moveTo(pathPoints[0].x, pathPoints[0].y); // Start from the first point
        for (let i = 1; i < pathPoints.length; i++) {
            ctx.lineTo(pathPoints[i].x, pathPoints[i].y); // Draw line to the next point
        }
        if (isDrawing && currentX !== null && currentY !== null) {
            ctx.lineTo(currentX, currentY);
        }
        ctx.stroke(); // Render the lines on the canvas
    }
}

function drawAgents() {
    if (canvas.getContext) {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        drawPath();
        if (agents !== null) {
            agents.forEach(agent => {
                // Set a larger radius for the agent that is selected
                const radius = (selectedAgent !== null && agent.id === selectedAgent.id) ? 5 : 3;
                const color = (agent.connectionStatus === 'CONNECTED' ? 'green' : 'red')
                // Draw each agent position
                ctx.beginPath();
                ctx.fillStyle = color;
                ctx.arc(agent.currentLocation.x, agent.currentLocation.y, radius, 0, Math.PI * 2); // Drawing a small circle for each position
                ctx.fill();
            });
        }
    }
}

function populateAgentList() {
    const listContainer = document.getElementById('listContainer');
    listContainer.innerHTML = '';
    agents.forEach(agent => {
        const agentItem = document.createElement('div');
        agentItem.classList.add('listItem');

        // Display ID and Connection Status
        agentItem.innerHTML = `<span>${agent.id}</span> <span class="${agent.connectionStatus === 'CONNECTED' ? 'connected' : 'disconnected'}" style="float:right">${agent.connectionStatus}</span>`;

        agentItem.onclick = function () {
            selectListItem(this)
            selectedAgent = agent
            displayAgentDetails(agent);
        };
        listContainer.appendChild(agentItem);
    });
}

function selectListItem(item) {
    if (selectedItem) {
        selectedItem.classList.remove('selected');
    }
    selectedItem = item;
    selectedItem.classList.add('selected');
}

function displayAgentDetails(agent) {
    if (agent.id !== selectedAgent.id) return;
    const detailsContainer = document.getElementById('agentDetails');
    detailsContainer.innerHTML = `
        <strong>Connection Status:</strong> ${agent.connectionStatus}<br>
        <strong>Speed:</strong> ${agent.speed}<br>
        <strong>Current Location:</strong> ${agent.currentLocation.x} ${agent.currentLocation.y}
    `;
    populateTripsList()
}

function populateTripsList() {
    const tripsContainer = document.getElementById('tripsContainer');
    tripsContainer.innerHTML = '';
    selectedAgent.upcomingTrips.forEach(trip => {
        const tripItem = document.createElement('div');
        tripItem.classList.add('listItem');

        let tripLength = 0;
        // Running the for loop
        for (let i = 1; i < trip.route.length; i++) {
            tripLength += distanceBetween(trip.route[0].x, trip.route[1].x, trip.route[0].y, trip.route[1].y);
        }
        // Display ID and Connection Status
        tripItem.innerHTML = `<span> Startposition: ${trip.route[0].x}, ${trip.route[0].y}</span>
                                <span> Endposition: ${trip.route[trip.route.length-1].x}, ${trip.route[trip.route.length-1].y}</span>
                                <span> Route points: ${trip.route.length}</span>
                                <span> Trip length: ${tripLength}</span>`;

        tripItem.onclick = function () {
            selectListItem(this)
            selectedTrip = trip;
            removeRouteButton.removeAttribute("disabled");
        };
        tripsContainer.appendChild(tripItem);
    });
}

function distanceBetween(x1, x2, y1, y2) {
    return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2))
}

function secondsUntilMidnight() {
    var now = new Date();
    var midnight = new Date(
        now.getFullYear(),
        now.getMonth(),
        now.getDate() + 1, // the next day
        0, 0, 0, 0 // set to midnight
    );
    var difference = midnight - now; // difference in milliseconds
    return Math.floor(difference / 1000); // convert to seconds
}

function setupAddRouteButton() {
    addRouteButton.onclick = function () {
        if (selectedItem) {
            // TODO Add log to console
            let inputValue = parseInt(document.getElementById("delayInput").value);
            if (isNaN(inputValue)) {
                inputValue = 1;
            } else {
                inputValue = Math.min(inputValue, secondsUntilMidnight());
            }
            sendTripCommand(selectedAgent.id, pathPoints, inputValue, 'ADD');
            populateTripsList();
            addRouteButton.setAttribute("disabled","disabled");
        } else {
            alert('Please select an item first.');
        }
    };
}

function setupRemoveRouteButton() {
    removeRouteButton.onclick = function () {
        if (selectedItem) {
            // TODO Add log to console
            sendTripCommand(selectedAgent.id, selectedTrip.route, 0, 'REMOVE');
        } else {
            alert('Please select an item first.');
        }
    };
    removeRouteButton.setAttribute("disabled","disabled");
}

/* Canvas drawing functions */

function getMousePos(canvas, evt) {
    const rect = canvas.getBoundingClientRect();
    return {
        x: evt.clientX - rect.left,
        y: evt.clientY - rect.top
    };
}

function startDrawing(evt) {
    isDrawing = true;
    const {x, y} = getMousePos(canvas, evt);
    lastX = x;
    lastY = y;
    pathPoints = [{x, y}];
}

function draw(evt) {
    if (!isDrawing) return;
    const {x, y} = getMousePos(canvas, evt);
    currentX = x;
    currentY = y;

    // Check distance
    if (Math.hypot(x - lastX, y - lastY) >= d) {
        lastX = x;
        lastY = y;
        pathPoints.push({x, y});
    }
}

function stopDrawing(evt) {
    const {x, y} = getMousePos(canvas, evt);
    if (isDrawing && pathPoints.length > 1) {
        pathPoints.push({x, y});
    }
    currentX = null;
    currentY = null;
    isDrawing = false;
    console.log("Drawn Points:", pathPoints); // Log the points

    if (pathPoints.length > 1 && selectedAgent !== null) {
        addRouteButton.removeAttribute('disabled');
    } else {
        addRouteButton.setAttribute("disabled","disabled");
    }
}

initCanvas();
setInterval(drawAgents, 100);
usernameForm.addEventListener('submit', connect, true);

