package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.EventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationActivity extends AppCompatActivity {


    private Toolbar mToolbar;

    private String currentUserID;

    private RecyclerView NotificationList;

    private DatabaseReference allUsersDatabaseRef, FriendsReqRef;

    private FirebaseAuth mAuth;
    private Context context;
//    private int CountNotification = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendsReqRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests").child(currentUserID);


        mToolbar = (Toolbar) findViewById(R.id.notification_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("New Friend Request");


        NotificationList = (RecyclerView) findViewById(R.id.notification_result_list);
        NotificationList.setHasFixedSize(true);
        NotificationList.setLayoutManager(new LinearLayoutManager(this));

        DisplayAllFriendsReq();


    }
    private void DisplayAllFriendsReq()
    {
        FirebaseRecyclerOptions<FriendRequests> options = new FirebaseRecyclerOptions.Builder<FriendRequests>()
                .setQuery(FriendsReqRef.orderByChild("request_type").equalTo("received"), FriendRequests.class).build();

        FirebaseRecyclerAdapter<FriendRequests, NotificationActivity.NotificationViewHolder> adapter = new FirebaseRecyclerAdapter<FriendRequests, NotificationActivity.NotificationViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final NotificationViewHolder holder, int position, @NonNull FriendRequests model)
            {

//                holder.date.setText("Friends Since: "+ model.getDate())
                final String userIDs = getRef(position).getKey();


               allUsersDatabaseRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            final String userName = dataSnapshot.child("fullname").getValue().toString(); //retrieve user name
                            final String profileImage = dataSnapshot.child("profileimage").getValue().toString(); //retrieve profile image
                            final String country = dataSnapshot.child("country").getValue().toString();
//                            CountNotification = (int) dataSnapshot.getChildrenCount();
//                            MyPosts.setText(Integer.toString(CountNotification));
////                            final String type;

//                            if(dataSnapshot.hasChild("userState"))
//                            {
//                                type = dataSnapshot.child("userState").child("type").getValue().toString();
//
//                                if(type.equals("online"))
//                                {
//                                    holder.onlineStatusView.setVisibility(View.VISIBLE);
//                                }
//                                else
//                                {
//                                    holder.onlineStatusView.setVisibility(View.INVISIBLE);
//                                }
//                            }

                            holder.setFullname(userName);
                            holder.setProfileimage(getApplicationContext(),profileImage);
                            holder.setCountry("");
                            holder.date.setText(userName + " has send you a friend request, Please confirm by viewing her profile.");

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
                                    AlertDialog.Builder builder = new AlertDialog.Builder(NotificationActivity.this);
                                    builder.setTitle("Select Option");

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            if(which == 0)
                                            {
                                                //send user to profile which is the first option
                                                Intent profileintent = new Intent(NotificationActivity.this, PersonProfileActivity.class);
                                                //send user id
                                                profileintent.putExtra("visit_user_id",userIDs); // from person profile pic send user id
                                                startActivity(profileintent);
                                            }
                                            if(which == 1)
                                            {
                                                Intent Chatintent = new Intent(NotificationActivity.this, ChatActivity.class);
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
            public NotificationActivity.NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
                NotificationActivity.NotificationViewHolder viewHolder = new NotificationActivity.NotificationViewHolder(view);
                return viewHolder;

            }
        };

        NotificationList.setAdapter(adapter);
        adapter.startListening();

    }


    public static class NotificationViewHolder extends RecyclerView.ViewHolder
    {
        TextView fullname, date;
        CircleImageView profileimage;
        View mView;
//        ImageView onlineStatusView;


        public NotificationViewHolder(@NonNull View itemView)
        {
            super(itemView);

            mView = itemView;

//            onlineStatusView = (ImageView)itemView.findViewById(R.id.all_user_online_icon);

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


