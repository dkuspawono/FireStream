const firebase = require('firebase');
const FCM = require('fcm-push');

let serverKey = 'AIzaSyAOfX9IyRumoiK-daOJyYPM3AZY2wBWAEQ';
let fcm = new FCM(serverKey);

var parties = [];
exports.init = () => {
	firebase.initializeApp({
		apiKey: 'AIzaSyBDlzcTeK4FU7YZkUBO3hiI0_ZuhweKSXU',
		authDomain: 'firestream-4e998.firebaseapp.com',
		databaseURL: 'https://firestream-4e998.firebaseio.com'
	});

	// Subscribing to parties
	var ref = firebase.database().ref('parties');
	ref.on('child_added', function(data) {
		parties.push(data.val());
	});

	ref.on('child_changed', function(data) {
		for (var i = 0; i < parties.length; i++) {
			if (parties[i].id === data.val().id) {
				parties[i] = data.val();
				break;
			}
		}
	});

	ref.on('child_removed', function(data) {
		parties = parties.filter(function(elem) {
			return elem.id != data.val().id;
		});
	});

	// Increment song progress
    var interval = 5000;
	setInterval(function() {
        for (var i = 0; i < parties.length; i++) {
            var party = parties[i];
            if (party.isPlaying) {
                party.progress += interval;

                // Check if move to next song
                if (typeof(party.queue) !== 'undefined' && party.queue.length > 0 && party.progress >= party.queue[0].duration) {
                    party.progress = 0;
                    party.queue.push(party.queue.shift());
                }
            }

            if (party.hostToken && party.requests && party.requests.length > 0) {
                for (var j = 0; j < party.requests.length; j++) {
                    var data = party.requests[j];
                    data.partyId = party.id;

                    exports.sendMessage({
                        endpoint: party.hostToken,
                        title: "Song Request for Party '" + party.name + "'",
                        body: "The song: '" + party.requests[j].name + "' by " + party.requests[j].artist + " has been requested.",
                        songData: data
                    });
                }
                party.requests = [];
            }

            party.timestamp = new Date().getTime();
            parties[i] = party;
            firebase.database().ref('parties').child(party.id).set(party);
        }
	}, interval);
};


exports.sendMessage = (data) => {
    console.log('sending message: ' + JSON.stringify(data));
	var message = {
		to: data.endpoint,
		data: {
			title: data.title,
			body: data.body,
			data: data.songData,
            code: "0"
		},
		// notification: {
		// 	title: data.title,
		// 	body: data.body
		// }
	};

	//callback style
	fcm.send(message, function(err, response) {
		return !err
	});
};

exports.joinParty = (data, socketId) => {
    var partyId = data;
    for (var i = 0; i < parties.length; i++) {
        if (parties[i].id === partyId) {
            if (!parties[i].members)
                parties[i].members = [];
            if (parties[i].members.indexOf(socketId) === -1) {
                parties[i].members.push(socketId);
                parties[i].attendees = parties[i].members.length;
                parties[i].timestamp = new Date().getTime();
                firebase.database().ref('parties').child(partyId).set(parties[i]);
            }
            break;
        }
    }
};

exports.leaveParties = (socketId) => {
    for (var i = 0; i < parties.length; i++) {
        for (var j = 0; j < parties[i].members.length; j++) {
            if (parties[i].members[j] === socketId) {
                parties[i].members = parties[i].members.splice(j, 1);
                parties[i].attendees = parties[i].members.length;
                parties[i].timestamp = new Date().getTime();
                firebase.database().ref('parties').child(parties[i].id).set(parties[i]);
            }
        }
    }
};
