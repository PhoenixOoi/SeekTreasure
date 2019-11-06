package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Create by OCF on 2019
 */

public class PersonProfileActivity extends AppCompatActivity
{
    //copy from profile activity
    private TextView userName, userProfName, userStatus, userCountry, userGender, SellerBuyer, userDOB;
    private CircleImageView userProfileImage;
    private Button SendFriendReqbutton, DeclineFriendRequestButton;

    private DatabaseReference FriendRequestRef, UsersRef, FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE, saveCurrentDate; //user who will be online will send friend request

    private String saveCurrentDateReq, SaveCurrentTimeReq;

    //add
    private DatabaseReference FollowersRef, PostsRef;

    //add
    private Button MyPosts, MyFollowers;
    private int countFollowers= 0, countPosts = 0;

    //add 6
    public static Bundle mMyAppsBundle = new Bundle();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);

        //add
        MyFollowers = (Button) findViewById(R.id.person_my_followers_button);
        MyPosts = (Button) findViewById(R.id.person_my_post_button);

        mAuth = FirebaseAuth.getInstance();
        //person who send the friend request
        senderUserId = mAuth.getCurrentUser().getUid();
        //receive from the findsellerbuyer activity
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        //retrieve information of the user when we click on the user
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");


        //add
        FollowersRef = FirebaseDatabase.getInstance().getReference().child("Friends"); // to count the followers
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");// to apply firebase query for couting how much post

        //add
        MyFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToFollowersActivity();
            }
        });
        //add
        MyPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToMyPostsActivity();
            }
        });


        //order by child is to get uid (get specific firebase query
        PostsRef.orderByChild("uid")
                .startAt(receiverUserId).endAt(receiverUserId + "\uf8ff")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.exists())
                        {
                            //if user have posts
                            countPosts = (int) dataSnapshot.getChildrenCount();
                            MyPosts.setText(Integer.toString(countPosts) + " Posts");
                        }
                        else
                        {
                            //if users dun have posts
                            MyPosts.setText("0 Posts");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });

        FollowersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    //if user has followers
                    //create integer variable to stored the count child
                    countFollowers = (int) dataSnapshot.getChildrenCount();
                    MyFollowers.setText(Integer.toString(countFollowers)+ " Followers");

                }
                else
                {
                    //if user has no followers
                    MyFollowers.setText("0 Followers");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });


        InitializeFields();

        //copy from profile activity
        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //retrieve data
                if(dataSnapshot.exists())
                {
                    //make sure name same as database
                    String myProfileImage = dataSnapshot.child("profileimage").getValue(String.class);
                    String myUserName = dataSnapshot.child("username").getValue().toString();
                    String myProfileName = dataSnapshot.child("fullname").getValue().toString();
                    String myProfileStatus = dataSnapshot.child("status").getValue().toString();
                    String myDOB = dataSnapshot.child("dob").getValue().toString();
                    String myCountry = dataSnapshot.child("country").getValue().toString();
                    String myGender = dataSnapshot.child("gender").getValue().toString();
                    String mySellerBuyer = dataSnapshot.child("SellerBuyerStatus").getValue().toString();

                    //displaying the above things
                    Picasso.get().load(myProfileImage).placeholder(R.drawable.profile).into(userProfileImage);

                    userName.setText("@" + myUserName);
                    userProfName.setText(myProfileName);
                    userStatus.setText(myProfileStatus);
                    userDOB.setText("Date of Birth: " + myDOB);
                    userCountry.setText("Country: " + myCountry);
                    userGender.setText("Gender: " + myGender);
                    SellerBuyer.setText("Seller or Buyer: " + mySellerBuyer);


                    //create a method
                    MaintainanceofButton();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });



        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestButton.setEnabled(false);

        //prevent user from sending friend request to himself
        if(!senderUserId.equals(receiverUserId))
        {
            SendFriendReqbutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    SendFriendReqbutton.setEnabled(false);
                    //if the two person is not friend then can send friend request
                    if(CURRENT_STATE.equals("not_friends"))
                    {
                        //allow user to send request
                        SendFriendRequestToaPerson();
                    }
                    if(CURRENT_STATE.equals("request_sent"))
                    {
                        //if the request is sent the user can cancel the friend request
                        CancelFriendRequest();
                    }
                    if(CURRENT_STATE.equals("request_received"))
                    {
                        //create a method accept the request
                        AcceptFriendRequest();
                    }
                    if(CURRENT_STATE.equals("friends"))
                    {
                        UnFriendAnExistingFriend();
                    }
                }
            });
        }
        else
        {
            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
            SendFriendReqbutton.setVisibility(View.INVISIBLE);
        }
    }

    //add
    private void SendUserToFollowersActivity()
    {
        Intent followersIntent = new Intent(PersonProfileActivity.this, FriendsActivity.class);
        startActivity(followersIntent);

    }
    //add
    private void SendUserToMyPostsActivity()
    {
        Intent followersIntent = new Intent(PersonProfileActivity.this, MyPostsActivity.class);
        startActivity(followersIntent);

    }



    private void UnFriendAnExistingFriend()
    {
        //user friend reference if the user click on unfriend then both will become unfriend each other
        FriendsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            //remove record from friend node
                            FriendsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFriendReqbutton.setEnabled(true);
                                                CURRENT_STATE = "not_friends"; //request send
                                                SendFriendReqbutton.setText("Send Friend Request");


                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });


    }

    private void AcceptFriendRequest()
    {
        //get the current date from which date the user become friend
        //copy from postactivity
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy"); //get current date n stored in variable
        //format to string data type
        saveCurrentDate = currentDate.format(calForDate.getTime());

        //stored the date
        //by using this references save the data
        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {

                        if(task.isSuccessful())
                        {
                            //save for receiver as well
                            FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                //if this task is successful it will create a parent node (Friends)
                                                //remove record from the FriendRequest because already successfully become friend
                                                FriendRequestRef.child(senderUserId).child(receiverUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
                                                                    FriendRequestRef.child(receiverUserId).child(senderUserId)
                                                                            .removeValue()  //remove the request from the database
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        SendFriendReqbutton.setEnabled(true);
                                                                                        CURRENT_STATE = "friends"; //two person become friends
                                                                                        SendFriendReqbutton.setText("Unfriend this Person");


                                                                                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                                        DeclineFriendRequestButton.setEnabled(false);
                                                                                    }

                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void CancelFriendRequest()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            FriendRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFriendReqbutton.setEnabled(true);
                                                CURRENT_STATE = "not_friends"; //request send
                                                SendFriendReqbutton.setText("Send Friend Request");



                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });
    }



    private void MaintainanceofButton()
    {
        //maintain record so when user pressed back to find the user the request button remain sent already condition
        FriendRequestRef.child(senderUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //validation
                if(dataSnapshot.hasChild(receiverUserId)) {
                    //store two type one is received one is sent
                    //linking to the firebase database and check for the request type
                    //retrieve the request type
                    if (dataSnapshot.child(receiverUserId).child("request_type").exists()) {
                        String request_type = dataSnapshot.child(receiverUserId).child("request_type")
                                .getValue().toString();


                        if (request_type.equals("sent")) {
                            CURRENT_STATE = "request_sent";
                            SendFriendReqbutton.setText("Cancel Friend Request");
                            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                            DeclineFriendRequestButton.setEnabled(false);
                        } else if (request_type.equals("received")) {
                            CURRENT_STATE = "request_received";
                            SendFriendReqbutton.setText("Accept Friend Request");

                            //for user to have the option to cancel the request
                            DeclineFriendRequestButton.setVisibility(View.VISIBLE);
                            DeclineFriendRequestButton.setEnabled(true);

                            DeclineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //cancel that friend request that the person sent
                                    CancelFriendRequest();
                                }
                            });
                        }
                    } else {
                        FriendsRef.child(senderUserId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        //the person who will received the friend request
                                        if (dataSnapshot.hasChild(receiverUserId)) {
                                            //maintain the data and button to show unfriend this person if the user come back to view the person
                                            CURRENT_STATE = "friends";
                                            SendFriendReqbutton.setText("Unfriend this Person");

                                            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                            DeclineFriendRequestButton.setEnabled(false);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void SendFriendRequestToaPerson()
    {
        //create a new node in firebase n store the data
        //inside parent node will be friendrequest there will be a child for sender
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {

                            //stored data
                            //first get current time n date, go post activity to copy cuz already created
                            Calendar calForDate = Calendar.getInstance();
                            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy"); //get current date n stored in variable
                            //format to string data type
                            final String saveCurrentDateReq = currentDate.format(calForDate.getTime());

                            Calendar calForTime = Calendar.getInstance();
                            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm"); //get current date n stored in variable
                            //format to string data type
                            final String saveCurrentTimeReq = currentTime.format(calForDate.getTime());


                            UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    if(dataSnapshot.exists())
                                    {
//                                        final String userName = dataSnapshot.child("fullname").getValue().toString();
                                        UsersRef.child(senderUserId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                            {
                                                final String userName = dataSnapshot.child("fullname").getValue().toString();

                                                //stored it
                                                HashMap FriendsReqMap = new HashMap();
                                                FriendsReqMap.put("date",saveCurrentDateReq);
                                                FriendsReqMap.put("time",saveCurrentTimeReq);
                                                FriendsReqMap.put("fullname",userName);
                                                FriendsReqMap.put("fromUserID",senderUserId);

                                                FriendRequestRef.child(receiverUserId).child(senderUserId).updateChildren(FriendsReqMap)
                                                        .addOnCompleteListener(new OnCompleteListener() {
                                                            @Override
                                                            public void onComplete(@NonNull Task task)
                                                            {
                                                                if(task.isSuccessful())
                                                                {
//                                                                                        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
//                                                                                            @Override
//                                                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//                                                                                            {
//                                                                                                final String userName = dataSnapshot.child("fullname").getValue().toString();
//
//                                                                                                //stored it
//                                                                                                HashMap FriendsReqMap = new HashMap();
//                                                                                                FriendsReqMap.put("date",saveCurrentDateReq);
//                                                                                                FriendsReqMap.put("time",saveCurrentTimeReq);
//                                                                                                FriendsReqMap.put("fullname",userName);
//                                                                                                FriendsReqMap.put("fromUserID",senderUserId);
//
//                                                                                                FriendRequestRef.child(senderUserId).child(receiverUserId).updateChildren(FriendsReqMap)
//                                                                                                        .addOnCompleteListener(new OnCompleteListener()
//                                                                                                        {
//                                                                                                            @Override
//                                                                                                            public void onComplete(@NonNull Task task)
//                                                                                                            {
//                                                                                                                if(task.isSuccessful())
//                                                                                                                {
//                                                                                                                    Log.d("save","save successfully");
//                                                                                                                }
//                                                                                                            }
//                                                                                                        });
//                                                                                            }
//
//                                                                                            @Override
//                                                                                            public void onCancelled(@NonNull DatabaseError databaseError)
//                                                                                            {
//
//                                                                                            }
//                                                                                        });
                                                                    Toast.makeText(PersonProfileActivity.this,"You have save request details successfully.",Toast.LENGTH_SHORT).show();

                                                                }
                                                                else
                                                                {
                                                                    Toast.makeText(PersonProfileActivity.this,"Error Occurred. Try Again.",Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });




                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError)
                                {

                                }
                            });

                            //data will be for the sender
                            //in the node there will be receiver ID first and then sender ID then request type in firebase
                            FriendRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                SendFriendReqbutton.setEnabled(true);
                                                CURRENT_STATE = "request_sent"; //request send
                                                SendFriendReqbutton.setText("Cancel Friend Request");

                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);


                                            }

                                        }
                                    });


                        }

                    }
                });
    }

    private void InitializeFields()
    {
        userName = (TextView) findViewById(R.id.person_username);
        userProfName = (TextView) findViewById(R.id.person_full_name);
        userStatus= (TextView) findViewById(R.id.person_profile_status);
        userCountry = (TextView) findViewById(R.id.person_country);
        userGender = (TextView) findViewById(R.id.person_gender);
        SellerBuyer = (TextView) findViewById(R.id.person_seller_buyer);
        userDOB = (TextView) findViewById(R.id.person_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.person_profile_pic);
        SendFriendReqbutton = (Button) findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendRequestButton = (Button) findViewById(R.id.person_declined_friend_request);

        CURRENT_STATE = "not_friends";
    }
}
