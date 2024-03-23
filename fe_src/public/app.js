const cors = require('cors');
const app = require('express')();

app.use(cors());


document.addEventListener('DOMContentLoaded', (event) => {
    var socket = new SockJS('http://localhost:8080/websocket'); // Adjust if necessary

    var client = new Stomp.Client({
        brokerURL: 'ws://localhost:8080/websocket',
        connectHeaders: {
          login: 'user',
          passcode: 'password',
        },
        debug: function (str) {
          console.log(str);
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });
      

    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);

        stompClient.subscribe('/topic/leaderboard', function(update) {
            var leaderboard = JSON.parse(update.body);
            var html = leaderboard.map(function(entry) {
                return '<p>' + entry.user.username + ': ' + entry.score + '</p>';
            }).join('');
            document.getElementById('leaderboard').innerHTML = html;
        });
    });
});
