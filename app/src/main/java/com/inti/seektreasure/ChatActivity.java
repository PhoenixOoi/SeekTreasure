package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private Toolbar ChattoolBar;
    private ImageButton SendMessageButton, SendImagefileButton;
    private EditText userMessageInput;


    private RecyclerView userMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messageAdapter;

    private String messageReceiverID, messageReceiverName, messageSenderID,saveCurrentDate,saveCurrentTime;

    private TextView receiverName, userLastSeen;
    private CircleImageView receiverProfileImage;

    private DatabaseReference RootRef, UserRef;
    private FirebaseAuth mAuth; //to store message


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance().getReference(); // link to whole database so no need to access child
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");


        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("userName").toString();


        InitializeFields();

        DisplayReceiverInfo();

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendMessage();
            }
        });

        //FetchMessages();
    }

//add

    @Override
    protected void onStart() {
        super.onStart();


//    private void FetchMessages()
//    {
        //references to our messages node
        RootRef.child("Message").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
                    {
                        if(dataSnapshot.exists())
                        {
                            Messages messages = dataSnapshot.getValue(Messages.class);
                            messagesList.add(messages);
                            messageAdapter.notifyDataSetChanged(); //whenever got data it will notify the change

                            userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                        }

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void SendMessage()
    {
        //when ever send the message it should update our last seen and online offline status
        updateUserStatus("online");

        //get the message from the input field
        String messageText = userMessageInput.getText().toString();

        //validation
        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this,"Please type a message first",Toast.LENGTH_SHORT).show();
        }
        else
        {
            //stored message in database
            //create references , Message as parent node then sender id then receiver id and vice versa
            String message_sender_ref = "Message/" + messageSenderID + "/" + messageReceiverID;
            String message_receiver_ref = "Message/" + messageReceiverID + "/" + messageSenderID;

            //RootRef is seektreasure = 8cf4f
            //.push will create unique random key
            DatabaseReference user_message_key = RootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                    .child(messageReceiverID).push();

            String message_push_id = user_message_key.getKey();

            //copy from post activity
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy"); //get current date n stored in variable
            //format to string data type
            saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm ss"); //get current date n stored in variable
            //format to string data type
            saveCurrentTime = currentTime.format(calForDate.getTime());

            //save in database
            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);
            messageTextBody.put("type", "text"); //type of message
            messageTextBody.put("from", messageSenderID);


            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(message_sender_ref + "/" + message_push_id , messageTextBody); //display for sender
            //display for receiver too
            messageBodyDetails.put(message_receiver_ref + "/" + message_push_id , messageTextBody);

            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully", Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    } else {
                        String message = task.getException().getMessage();
                        Toast.makeText(ChatActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                        userMessageInput.setText("");
                    }

                }
            });

        }
    }

    //state will be online offline
    public void updateUserStatus(String state)
    {
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a"); //a will be am or pm
        saveCurrentTime = currentTime.format(calForTime.getTime());


        //save in database in Users node
        Map currentStateMap = new HashMap<>();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state); //pass parameter to it

        //already create the references for the user node
        //create another child for saving the online user information(online status and last seen info) under parent node
        UserRef.child(messageSenderID).child("userState")
                .updateChildren(currentStateMap);

    }



    private void DisplayReceiverInfo()
    {
        receiverName.setText(messageReceiverName);

        //retrieve image from database
        RootRef.child("Users").child(messageReceiverID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    final String profileImage = dataSnapshot.child("profileimage").getValue().toString(); //make sure same name as firebase

                    //for retrieving the last seen data (userstate)
                    final String type = dataSnapshot.child("userState").child("type").getValue().toString();
                    final String lastDate = dataSnapshot.child("userState").child("date").getValue().toString();
                    final String lastTime = dataSnapshot.child("userState").child("time").getValue().toString();

                    if (type.equals("online"))
                    {
                        userLastSeen.setText("online");
                    }
                    else
                    {
                        //if user is not online then can display the last seen for user friend
                        userLastSeen.setText("last seen: " + lastTime + " " + lastDate);
                    }

                    //display
                    Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(receiverProfileImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void InitializeFields()
    {
        ChattoolBar = (Toolbar)findViewById(R.id.chat_bar_layout);
        setSupportActionBar(ChattoolBar);


        //connect chat custom bar xml file to the chat activity
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = layoutInflater.inflate(R.layout.chat_custom_bar, null);
        actionBar.setCustomView(action_bar_view);


        userLastSeen = (TextView) findViewById(R.id.custom_user_last_seen);
        receiverName = (TextView) findViewById(R.id.custom_profile_name);
        receiverProfileImage = (CircleImageView) findViewById(R.id.custom_profile_image);

        SendMessageButton = (ImageButton) findViewById(R.id.send_message_button);
        SendImagefileButton = (ImageButton) findViewById(R.id.send_image_file_button);
        userMessageInput = (EditText) findViewById(R.id.input_message);

        //include recycler view
        messageAdapter = new MessagesAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.messages_list_users);
        linearLayoutManager = new LinearLayoutManager(this);
       // userMessagesList.setHasFixedSize(true);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


    }
}
