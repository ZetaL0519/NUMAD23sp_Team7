package edu.northeastern.numad23sp_team7;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.northeastern.numad23sp_team7.model.History;
import edu.northeastern.numad23sp_team7.model.User;

public class SendStickerActivity extends AppCompatActivity {

    private static final String TAG = "SendStickerActivity";
    static final String HISTORY_USERNAME_KEY = "logged-in-user";
    static final String HISTORY_SEND_OR_RECEIVE_FLAG = "send-or-receive";
    static final String HISTORY_SEND_VALUE = "send";
    static final String HISTORY_RECEIVE_VALUE = "receive";
    static final String HISTORY_IMAGE_MAP_KEY = "image-map";

    private DatabaseReference mDatabase;
    private TextView usernameText;

    private ImageView sticker1;
    private ImageView sticker2;
    private ImageView sticker3;
    private ImageView sticker4;
    private ImageView currentClickedSticker = null;
    private List<ImageView> stickerList = new ArrayList<>();

    private Spinner selectReceiverSpinner;
    private List<String> receiverList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;
    private String receiverUsername;

    private Button sendButton;
    private Button sendHistoryButton;
    private Button receiveHistoryButton;

    private TextView textCategory1;
    private TextView textCategory2;
    private TextView textCategory3;
    private TextView textCategory4;

    private Map<String, String> categoryMap;
    private Map<Integer, String> imageIdToFilenameMap = new HashMap<>();
    private String loggedInUsername;

    private final int NOTIFICATION_UNIQUE_ID = 7;
    private static int notificationGeneration = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        setContentView(R.layout.activity_send_sticker);


        // Connect with firebase
        mDatabase = FirebaseDatabase.getInstance().getReference(User.class.getSimpleName());

        // Get username from intent
        loggedInUsername = getIntent().getExtras().getString(HISTORY_USERNAME_KEY);
        usernameText = findViewById(R.id.username);
        usernameText.setText(loggedInUsername);


        // get all usernames except for the current user from database
        selectReceiverSpinner = findViewById(R.id.spinner_receiver);
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        selectReceiverSpinner.setAdapter(spinnerAdapter);

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> tempList = new ArrayList<>();
                // Loop through all users in the dataSnapshot
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    // Add the username to the list, if it is not the current loggedin user's username
                    if (!user.getUsername().equals(loggedInUsername)) {
                        tempList.add(user.getUsername());
                    }
                    spinnerAdapter.clear();
                    spinnerAdapter.addAll(tempList);
                    spinnerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
                Log.w("getErrorMsg", "onCancelled", databaseError.toException());
            }
        });

        // Get and set the selected receiver username from the spinner
        selectReceiverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedReceiver = parent.getItemAtPosition(position).toString();
                receiverUsername = selectedReceiver;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sticker1 = findViewById(R.id.sticker1);
        sticker2 = findViewById(R.id.sticker2);
        sticker3 = findViewById(R.id.sticker3);
        sticker4 = findViewById(R.id.sticker4);

        stickerList.add(sticker1);
        stickerList.add(sticker2);
        stickerList.add(sticker3);
        stickerList.add(sticker4);

        categoryMap = new HashMap<>();
        categoryMap.put("sticker1", "Food");
        categoryMap.put("sticker2", "Food");
        categoryMap.put("sticker3", "Drink");
        categoryMap.put("sticker4", "Food");

        textCategory1 = findViewById(R.id.textViewStickerCategory1);
        textCategory2 = findViewById(R.id.textViewStickerCategory2);
        textCategory3 = findViewById(R.id.textViewStickerCategory3);
        textCategory4 = findViewById(R.id.textViewStickerCategory4);

        imageIdToFilenameMap.put(sticker1.getId(), "sticker1");
        imageIdToFilenameMap.put(sticker2.getId(), "sticker2");
        imageIdToFilenameMap.put(sticker3.getId(), "sticker3");
        imageIdToFilenameMap.put(sticker4.getId(), "sticker4");
        textCategory1.setText(categoryMap.get("sticker1"));
        textCategory2.setText(categoryMap.get("sticker2"));
        textCategory3.setText(categoryMap.get("sticker3"));
        textCategory4.setText(categoryMap.get("sticker4"));

        for (int i = 0; i < 4; i++) {
            stickerList.get(i).setOnClickListener(view -> {
                if (currentClickedSticker != null) {
                    currentClickedSticker.setBackground(null);
                }
                view.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.sticker_border));
                currentClickedSticker = (ImageView) view;

            });
        }

        sendButton = findViewById(R.id.send_sticker);
        // send button
        sendButton.setOnClickListener(view -> {
            // determine if receiver or stickerId is null
            if (receiverUsername == null || currentClickedSticker == null) {
                Toast.makeText(getApplicationContext(), "Please choose a receiver and a sticker", Toast.LENGTH_LONG).show();
            } else {
                int stickerId = currentClickedSticker.getId();
                String imageFilename = imageIdToFilenameMap.get(stickerId);

                // update database
                updateReceiverHistory(mDatabase, imageFilename, loggedInUsername, receiverUsername);
                updateSenderHistory(mDatabase, imageFilename, loggedInUsername, receiverUsername);

                Log.d(TAG, "sent");
                Toast.makeText(getApplicationContext(), "Sticker Sent", Toast.LENGTH_LONG).show();
//                    sendNotification();
            }
        });
        sendHistoryButton = findViewById(R.id.buttonStickerSendHistory);
        sendHistoryButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, StickerHistoryActivity.class);
            intent.putExtra(HISTORY_USERNAME_KEY, loggedInUsername);
            intent.putExtra(HISTORY_SEND_OR_RECEIVE_FLAG, HISTORY_SEND_VALUE);
            intent.putExtra(HISTORY_IMAGE_MAP_KEY, (Serializable) imageIdToFilenameMap);
            startActivity(intent);
        });

        receiveHistoryButton = findViewById(R.id.buttonStickerReceiveHistory);
        receiveHistoryButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, StickerHistoryActivity.class);
            intent.putExtra(HISTORY_USERNAME_KEY, loggedInUsername);
            intent.putExtra(HISTORY_SEND_OR_RECEIVE_FLAG, HISTORY_RECEIVE_VALUE);
            startActivity(intent);
        });
//        receiverUsername = loggedInUsername;
        getNotification();
    }

    // update Sent History to sender's sentRecords
    private void updateSenderHistory(
            DatabaseReference mDatabase,
            String stickerId,
            String senderUsername,
            String receiverUsername) {
        mDatabase.child(senderUsername).child("sentRecords").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                var currentSentRecords = (ArrayList<History>) mutableData.getValue();
                if (currentSentRecords == null) {
                    currentSentRecords = new ArrayList<>();
                }

                currentSentRecords.add(new History(stickerId, receiverUsername, categoryMap.get(stickerId)));
                mutableData.setValue(currentSentRecords);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(
                    DatabaseError databaseError,
                    boolean b,
                    DataSnapshot dataSnapshot) {
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });

    }

    // update Received History to receiver's receivedRecords
    private void updateReceiverHistory(
            DatabaseReference mDatabase,
            String stickerId,
            String senderUsername,
            String receiverUsername) {
        mDatabase.child(receiverUsername).child("receivedRecords").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                var currentReceiverHistory = (ArrayList<History>) mutableData.getValue();
                if (currentReceiverHistory == null) {
                    currentReceiverHistory = new ArrayList<>();
                }

                currentReceiverHistory.add(new History(stickerId, senderUsername, categoryMap.get(stickerId)));
                mutableData.setValue(currentReceiverHistory);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(
                    DatabaseError databaseError,
                    boolean b,
                    DataSnapshot dataSnapshot) {
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("send_sticker_channel_id", name, importance);
            channel.setDescription(description);
            channel.enableLights(true);
            channel.setLockscreenVisibility(android.R.color.holo_green_light);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void sendNotification(History history) {
//        Intent intent = new Intent(this, ReceiveStickerActivity.class);
        Intent intent = new Intent(this, StickerHistoryActivity.class);
        intent.putExtra(HISTORY_USERNAME_KEY, loggedInUsername);
        intent.putExtra(HISTORY_SEND_OR_RECEIVE_FLAG, HISTORY_RECEIVE_VALUE);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent moreIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE);
        String channelId = getString(R.string.channel_id);
//        History testHistory = new History("sticker1", "testUser", "testCategory");
        Integer receivedStickerId = getStickerIdFromMap(history.getStickerId());
        ImageView receivedSticker = findViewById(receivedStickerId);
        if (receivedSticker != null) {
            receivedSticker.setDrawingCacheEnabled(true);
            Bitmap largeIconBitmap = Bitmap.createBitmap(receivedSticker.getDrawingCache());
            receivedSticker.setDrawingCacheEnabled(false);

            receivedSticker.buildDrawingCache();
            Notification noti = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_yelp_foreground)
                    .setLargeIcon(largeIconBitmap)
                    .setContentTitle(history.getUsername())
                    .setContentText("You just received a sticker from " + history.getUsername() + " !")

                    .addAction(R.drawable.search, "More", moreIntent)
                    .setContentIntent(moreIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build();

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFICATION_UNIQUE_ID + notificationGeneration, noti);
        } else {
            Log.e("Sticker receiving error", "Users have different versions of the app");
        }
    }

    private void getNotification() {
        DatabaseReference currentUserRef = mDatabase.child(loggedInUsername).child("receivedRecords");
        currentUserRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                History history = snapshot.getValue(History.class);
                sendNotification(history);
            }

            @Override
            public void onChildChanged(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("Firebase", "Child changed: " + snapshot.getKey());
            }

            @Override
            public void onChildRemoved(@androidx.annotation.NonNull DataSnapshot snapshot) {
                Log.d("Firebase", "Child removed: " + snapshot.getKey());
            }

            @Override
            public void onChildMoved(@androidx.annotation.NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Log.d("Firebase", "Child moved: " + snapshot.getKey());
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {
                Log.e("Firebase", "Error listening for changes: " + error.getMessage());
            }
        });
    }


    private Integer getStickerIdFromMap(String imageName) {
        Integer stickerId = null;
        for (Map.Entry entry : imageIdToFilenameMap.entrySet()) {
            if (imageName.equals(entry.getValue())) {
                stickerId = (Integer) entry.getKey();
                break; //breaking because its one to one map
            }
        }
        return stickerId;
    }
}