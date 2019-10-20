package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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

public class ProfileActivity extends AppCompatActivity
{
    //copy from settingsActivity declaration on edittext
    private TextView userName, userProfName, userStatus, userCountry, userGender, SellerBuyer, userDOB;
    private CircleImageView userProfileImage;

    private DatabaseReference profileUserRef;
    private FirebaseAuth mAuth;

    private String currentUserId;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        profileUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);

        userName = (TextView) findViewById(R.id.my_username);
        userProfName = (TextView) findViewById(R.id.my_profile_full_name);
        userStatus= (TextView) findViewById(R.id.my_profile_status);
        userCountry = (TextView) findViewById(R.id.my_country);
        userGender = (TextView) findViewById(R.id.my_gender);
        SellerBuyer = (TextView) findViewById(R.id.my_seller_buyer);
        userDOB = (TextView) findViewById(R.id.my_dob);
        userProfileImage = (CircleImageView) findViewById(R.id.my_profile_pic);

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
                    userCountry.setText("Country: " + myCountry);
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
}
