package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesActivity extends AppCompatActivity {

    private RecyclerView myMessagesList;
    private DatabaseReference MessagesRef, UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id; //get online user id

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);


        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        //we are searching for online user , only display specific user who is online
        MessagesRef = FirebaseDatabase.getInstance().getReference().child("Message").child(online_user_id);// make sure is praent node
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myMessagesList = (RecyclerView) findViewById(R.id.message_lists);
        myMessagesList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //display new post at the top , old post at the bottom
        //by using linearLayoutManager to accessing it
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myMessagesList.setLayoutManager(linearLayoutManager);

        DisplayAllMessages();
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
        UsersRef.child(online_user_id).child("userState")
                .updateChildren(currentStateMap);

    }


    @Override
    protected void onStart()
    {
        super.onStart();
        //when user come to this Friends activity, it mean online
        updateUserStatus("online");

    }

    @Override
    protected void onStop()
    {
        super.onStop();

        //when user minimize the app
        updateUserStatus("offline");

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //if app destroy or crashed then also change to offline
        updateUserStatus("offline");
    }

    private void DisplayAllMessages()
    {
        FirebaseRecyclerOptions<Messages> options = new FirebaseRecyclerOptions.Builder<Messages>()
                .setQuery(MessagesRef, Messages.class).build();

        FirebaseRecyclerAdapter<Messages, MessagesActivity.MessagesViewHolder> adapter = new FirebaseRecyclerAdapter<Messages, MessagesActivity.MessagesViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MessagesViewHolder holder, int position, @NonNull Messages model)
            {
                //holder.date.setText("Friends Since: "+ model.getDate());

                final String userIDs = getRef(position).getKey();

                UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            final String userName = dataSnapshot.child("fullname").getValue().toString(); //retrieve user name
                            final String profileImage = dataSnapshot.child("profileimage").getValue().toString(); //retrieve profile image
                            final String country = dataSnapshot.child("country").getValue().toString();
                            final String type;

                            if(dataSnapshot.hasChild("userState"))
                            {
                                type = dataSnapshot.child("userState").child("type").getValue().toString();

                                if(type.equals("online"))
                                {
                                    holder.onlineStatusView.setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    holder.onlineStatusView.setVisibility(View.INVISIBLE);
                                }
                            }

                            holder.setFullname(userName);
                            holder.setProfileimage(getApplicationContext(),profileImage);
                            holder.setCountry(country);

                            holder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v)
                                {
                                    //a dialog box option
                                    CharSequence options[] = new CharSequence[]
                                            {
                                                    userName  + "'s Profile",
                                                    "Send Message"
                                            };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MessagesActivity.this);
                                    builder.setTitle("Select Option");

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            if(which == 0)
                                            {
                                                //send user to profile which is the first option
                                                Intent profileintent = new Intent(MessagesActivity.this, PersonProfileActivity.class);
                                                //send user id
                                                profileintent.putExtra("visit_user_id",userIDs); // from person profile pic send user id
                                                startActivity(profileintent);
                                            }
                                            if(which == 1)
                                            {
                                                Intent Chatintent = new Intent(MessagesActivity.this, ChatActivity.class);
                                                //send user id
                                                Chatintent.putExtra("visit_user_id",userIDs); // from person profile pic send user id //sending to the chat activity
                                                Chatintent.putExtra("userName",userName);
                                                startActivity(Chatintent);
                                            }
                                        }
                                    });
                                    builder.show();


                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }

            @NonNull
            @Override
            public MessagesActivity.MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
                MessagesActivity.MessagesViewHolder viewHolder = new MessagesActivity.MessagesViewHolder(view);
                return viewHolder;

            }
        };

        myMessagesList.setAdapter(adapter);
        adapter.startListening();

    }


    public static class MessagesViewHolder extends RecyclerView.ViewHolder
    {
        TextView fullname, date;
        CircleImageView profileimage;
        View mView;
        ImageView onlineStatusView;


        public MessagesViewHolder(@NonNull View itemView)
        {
            super(itemView);

            mView = itemView;

            onlineStatusView = (ImageView)itemView.findViewById(R.id.all_user_online_icon);

            // fullname = itemView.findViewById(R.id.all_users_profile_full_name);
            //profileimage = itemView.findViewById(R.id.all_users_profile_image);
            date = itemView.findViewById(R.id.all_users_status);
        }
        public void setProfileimage (Context ctx, String profileimage)
        {
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(myImage);
        }
        public void setFullname(String fullname)
        {
            TextView myName = (TextView) mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }
        public void setCountry (String country)
        {
            TextView myCountry = (TextView) mView.findViewById(R.id.all_users_country);
            myCountry.setText(country);
        }
    }
}


