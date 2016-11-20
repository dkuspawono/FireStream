const firebase = require('firebase');
const FCM = require('fcm-push');

let serverKey = 'AIzaSyBzTq646tEIVVvwr-QLxjURAcAoFfX7F60';
let fcm = new FCM(serverKey);

exports.init = () => {
	firebase.initializeApp({
		apiKey: serverKey,
		authDomain: 'firestream-4e998.firebaseapp.com',
		databaseURL: 'https://firestream-4e998.firebaseio.com'
	});

	// Subscribing to parties
	var ref = firebase.database().ref('parties');
	var parties = [];
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
    var interval = 2500;
	setInterval(function() {
        for (var i = 0; i < parties.length; i++) {
            var party = parties[i];

            if (!party.isPlaying)
                continue;

            party.progress += interval;

            // Check if move to next song
            if (party.progress >= party.queue[0].duration) {
                party.progress = 0;
                party.queue.push(party.queue.shift());
            }

            party.timestamp = new Date().getTime();
            firebase.database().ref('parties').child(party.id).set(party);
        }
	}, interval);
};


exports.sendMessage = (data) => {
	var message = {
		to: data.endpoint,
		data: {
			songData: data.songData
		},
		notification: {
			title: data.title,
			body: data.body
		}
	};

	//callback style
	fcm.send(message, function(err, response) {
		return !err
	});
};
