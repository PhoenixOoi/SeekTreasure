package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Create by OCF on 2019
 */

public class ProfileActivity extends AppCompatActivity
{
    //copy from settingsActivity declaration on edittext
    private TextView userName, userProfName, userStatus, userCountry, userGender, SellerBuyer, userDOB;
    private CircleImageView userProfileImage;

    private DatabaseReference profileUserRef, FollowersRef, PostsRef; // folowersref is to count how many followers, postsref is to count post
    private FirebaseAuth mAuth;
    private Button MyPosts, MyFollowers;

    private String currentUserId;
    private int countFollowers= 0, countPosts = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);



        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();//get user id
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        FollowersRef = FirebaseDatabase.getInstance().getReference().child("Friends"); // to count the followers
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");// to apply firebase query for couting how much post

        userName = (TextView) findViewById(R.id.my_username);
        userProfName = (TextView) findViewById(R.id.my_profile_full_name);
        userStatus= (TextView) findViewById(R.id.my_profile_status);
        userCountry = (TextView) findViewById(R.id.my_country);
        userGender = (TextView) findViewById(R.id.my_gender);
        SellerBuyer = (TextView) findViewById(R.id.my_seller_buyer);
        userDOB = (TextView) findViewById(R.id.my_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.my_profile_pic);
        MyFollowers = (Button) findViewById(R.id.my_followers_button);
        MyPosts = (Button) findViewById(R.id.my_post_button);


        MyFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToFollowersActivity();
            }
        });

        MyPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToMyPostsActivity();
            }
        });

        //order by child is to get uid (get specific firebase query
        PostsRef.orderByChild("uid")
                .startAt(currentUserId).endAt(currentUserId + "\uf8ff")
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



        FollowersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
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



        profileUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //retrieve data
                if(dataSnapshot.exists())
                {
                    //make sure name same as database
                    String myProfileImage = dataSnapshot.child("profileimage").getValue().toString();
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
                    userCountry.setText("States: " + myCountry);
                    userGender.setText("Gender: " + myGender);
                    SellerBuyer.setText("Seller or Buyer: " + mySellerBuyer);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void SendUserToFollowersActivity()
    {
        Intent followersIntent = new Intent(ProfileActivity.this, FriendsActivity.class);
        startActivity(followersIntent);

    }

    private void SendUserToMyPostsActivity()
    {
        Intent followersIntent = new Intent(ProfileActivity.this, MyPostsActivity.class);
        startActivity(followersIntent);

    }
}
