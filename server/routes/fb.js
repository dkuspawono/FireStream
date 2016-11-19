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
  fcm.send(message, function(err, response){
    return !err
  });
};
