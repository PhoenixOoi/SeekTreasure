package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Person;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.auth.data.model.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity
{
    private RecyclerView myFriendList;
    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id; //get online user id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        //we are searching for online user , only display specific user who is online
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);// make sure is praent node
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myFriendList = (RecyclerView) findViewById(R.id.friend_lists);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //display new post at the top , old post at the bottom
        //by using linearLayoutManager to accessing it
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }

    private void DisplayAllFriends()
    {
        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(FriendsRef, Friends.class).build();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final Friends model)
            {
                holder.date.setText("Friends Since: "+ model.getDate());

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
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Option");

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which)
                                        {
                                            if(which == 0)
                                            {
                                                //send user to profile which is the first option
                                                Intent profileintent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                                //send user id
                                                profileintent.putExtra("visit_user_id",userIDs); // from person profile pic send user id
                                                startActivity(profileintent);
                                            }
                                            if(which == 1)
                                            {
                                                Intent Chatintent = new Intent(FriendsActivity.this, ChatActivity.class);
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
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout, parent, false);
                FriendsViewHolder viewHolder = new FriendsViewHolder(view);
                return viewHolder;

            }
        };

       myFriendList.setAdapter(adapter);
       adapter.startListening();

    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView fullname, date;
        CircleImageView profileimage;
        View mView;

        public FriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            mView = itemView;

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
