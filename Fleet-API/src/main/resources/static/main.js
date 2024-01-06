'use strict';

var usernamePage = document.querySelector('#username-page');
var chatPage = document.querySelector('#chat-page');
var usernameForm = document.querySelector('#usernameForm');
var messageForm = document.querySelector('#messageForm');
var messageInput = document.querySelector('#message');
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
let agents = null;

function connect(event) {
    username = document.querySelector('#name').value.trim();

    if (username) {
        usernamePage.classList.add('hidden');
        chatPage.classList.remove('hidden');

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
}


function onError(error) {
    connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';
}


function sendMessage(event) {
    var messageContent = messageInput.value.trim();
    if (messageContent && stompClient) {
        var fleetMessage = {
            sender: username,
            content: messageInput.value,
            type: 'TRIP'
        };
        stompClient.send("/app/agent.sendMessage", {}, JSON.stringify(fleetMessage));
        messageInput.value = '';
    }
    event.preventDefault();
}


function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
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
    draw();
    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    messageElement.classList.add('event-message');


    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.id);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    fleetLog.appendChild(messageElement);
    fleetLog.scrollTop = fleetLog.scrollHeight;
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

function draw() {
    const canvas = document.getElementById("canvas");
    if (canvas.getContext) {
        const ctx = canvas.getContext("2d");

        ctx.beginPath();
        ctx.moveTo(75, 50);
        ctx.lineTo(100, 75);
        ctx.lineTo(100, 25);
        ctx.fill();
    }
}

function populateAgentList() {
    const listContainer = document.getElementById('listContainer');
    for (let i = 0; i < agents.length; i++) {
        const listItem = document.createElement('div');
        listItem.classList.add('listItem');
        listItem.textContent = agents[i].id;
        listItem.onclick = function() {
            selectListItem(this);
        };
        listContainer.appendChild(listItem);
    }
}

function selectListItem(item) {
    if (selectedItem) {
        selectedItem.classList.remove('selected');
    }
    selectedItem = item;
    selectedItem.classList.add('selected');
}

function setupAddRouteButton() {
    addRouteButton.onclick = function () {
        if (selectedItem) {
            // Execute some code based on the selected item
            alert('Button clicked with ' + selectedItem.textContent + ' selected.');
            // Add your custom logic here
        } else {
            alert('Please select an item first.');
        }
    };
}


usernameForm.addEventListener('submit', connect, true)
messageForm.addEventListener('submit', sendMessage, true)

//draw();