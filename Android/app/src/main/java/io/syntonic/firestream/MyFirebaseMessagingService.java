package io.syntonic.firestream;

/**
 * Created by Andrew on 11/19/2016.
 */
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            Map<String, String> dataMap = remoteMessage.getData();
            sendNotification(dataMap.get("title"), dataMap.get("body"), dataMap.get("data"), Integer.parseInt(dataMap.get("code")));
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }


    }
    // [END receive_message]

    public static final int REQUEST_CODE_SONG_REQUEST_ADD_TO_FRONT = 0;
    public static final int REQUEST_CODE_SONG_REQUEST_SHUFFLE_IN = 1;
    public static final int REQUEST_CODE_SONG_REQUEST_REJECT = 2;
    public static final String EXTRA_KEY_SONG_REQUEST_ACTION = "EXTRA_KEY_SONG_REQUEST_ACTION";
    public static final String EXTRA_KEY_SONG_REQUEST = "EXTRA_KEY_SONG_REQUEST";
    public static final String EXTRA_NOTIFICATION_INT = "EXTRA_NOTIFICATION_INT";


    private void sendNotification(String title, String body, String data, int code) {
        Intent intent = new Intent(this, PartyActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        // Song request
        int randomInt = new Random().nextInt(1000000);
        if (code == 0) {

            Intent intentAddToFront = new Intent(this, PartyActivity.class);
            intentAddToFront.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intentAddToFront.putExtra(EXTRA_KEY_SONG_REQUEST, data);
            intentAddToFront.putExtra(EXTRA_KEY_SONG_REQUEST_ACTION, REQUEST_CODE_SONG_REQUEST_ADD_TO_FRONT);
            intentAddToFront.putExtra(EXTRA_NOTIFICATION_INT, randomInt);
            PendingIntent pendingIntentAddToFront = PendingIntent.getActivity(this, randomInt, intentAddToFront, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.addAction(new NotificationCompat.Action(R.drawable.ic_add_white_24dp, "Add to Front", pendingIntentAddToFront));

            Intent intentShuffle = new Intent(this, PartyActivity.class);
            intentShuffle.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intentShuffle.putExtra(EXTRA_KEY_SONG_REQUEST, data);
            intentShuffle.putExtra(EXTRA_KEY_SONG_REQUEST_ACTION, REQUEST_CODE_SONG_REQUEST_SHUFFLE_IN);
            intentShuffle.putExtra(EXTRA_NOTIFICATION_INT, randomInt);
            PendingIntent pendingIntentShuffleIn = PendingIntent.getActivity(this, randomInt + 1, intentShuffle, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.addAction(new NotificationCompat.Action(R.drawable.ic_shuffle_white_24dp, "Shuffle In", pendingIntentShuffleIn));

            Intent intentReject = new Intent(this, PartyActivity.class);
            intentReject.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intentReject.putExtra(EXTRA_KEY_SONG_REQUEST, data);
            intentReject.putExtra(EXTRA_KEY_SONG_REQUEST_ACTION, REQUEST_CODE_SONG_REQUEST_REJECT);
            intentReject.putExtra(EXTRA_NOTIFICATION_INT, randomInt);
            PendingIntent pendingIntentReject = PendingIntent.getActivity(this, randomInt + 2, intentReject, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.addAction(new NotificationCompat.Action(R.drawable.ic_delete_white_24dp, "Reject", pendingIntentReject));
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = notificationBuilder.build();
        notificationManager.notify(randomInt, notification);
    }
}