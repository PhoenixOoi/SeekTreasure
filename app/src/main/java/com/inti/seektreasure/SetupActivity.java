package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity
{

    private EditText UserName, FullName, CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;


    String currentUserID;
    final static int Gallery_Pick = 1; // for get photo parameters

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);




        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        //pass the user current id and stored user informatiion
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("profile Images");


        UserName = (EditText)findViewById(R.id.setup_username);
        FullName = (EditText)findViewById(R.id.setup_full_name);
        CountryName = (EditText)findViewById(R.id.setup_country_name);
        SaveInformationButton = (Button)findViewById(R.id.setup_information_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);
        loadingBar = new ProgressDialog(this);

        SaveInformationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //call method
                SaveAccountSetupInformation();

            }
        });


        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //direct user to the mobile phone gallery
                //pick a picture
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*"); //which type of file user want to pick
                startActivityForResult(galleryIntent,Gallery_Pick);
            }
        });

        //user references , user who is online
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //if it has the child (user) already exist
                if(dataSnapshot.exists())
                {
                    //refer to database profileimage
                    String image = dataSnapshot.child("profileimage").getValue().toString();

                    //display image

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }



    //get the image from gallery
    //get the result

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        //check the data is not null
        super.onActivityResult(requestCode, resultCode, data);
        //if the code come from the gallery and it is not null
        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data !=null)
        {
            //get the image uri
            Uri ImageUri = data.getData();

            //add croping functionality
            //from here crop the image
            CropImage.activity()//display the cropping activity
            .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this); //click the crop btn

        }
        //if the user click the crop button get that crop image
        // the crop image activity request code stored in result
        if (requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {

                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait , while we are updating your profile image.");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);

                //if result is ok get the crop image uri
                Uri resultUri = result.getUri();

                //create user profile references first for database
                // save the photo with user unique id auth, typ of image
                //create a file path for the firebase
                //inside UserProfileImageRef create a child and by using the user unique id (currenUserId) to stored the jpg images
                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                //save the crop image into firebase storage  (resultUri), storing the images link
                //add on complete listener to check the task is successful or not
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful()) {
                            Toast.makeText(SetupActivity.this, "Profile Image stored successfuly to Firebase storage.", Toast.LENGTH_SHORT).show();

                            // get the link from the database
                            final String downloadUrl = filePath.getDownloadUrl().toString();
                            //check firebase node and have the references to stored the images into the database
                            //access firebase
                            UsersRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            //if the images successfully stored in firebase
                                            if (task.isSuccessful()) {

                                                //send the user back to setup intent
                                                Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                startActivity(selfIntent);

                                                Toast.makeText(SetupActivity.this, " Profile Image stored to Firebase Database Successfully.", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            } else {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                    }
                });
            }
            else
            {
                Toast.makeText(this, "Error Occured: Image cannot be cropped. Try Again.",Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void SaveAccountSetupInformation() {
        //get user information from the input field
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();

        //add validation
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Please write your username.", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(fullname)) {
            Toast.makeText(this, "Please write your full name.", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(country))
        {
            Toast.makeText(this, "Please write your country.", Toast.LENGTH_SHORT).show();
        }
        else
        {

            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait , while we are creating your new account.");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            //save information
            //first create references at the above
            HashMap userMap = new HashMap();
            userMap.put("username", username);
            userMap.put("fullname", fullname);
            userMap.put("country", country);
            userMap.put("status", "Hey there, I am using SeekTreasure, developed by OCF"); // a default value
            userMap.put("gender", "none");
            userMap.put("dob",""); //date of birth
            userMap.put("SellerBuyerStatus", "none");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) 
                {
                    if(task.isSuccessful())
                    {
                        //send user to main activity
                        SendUserToMainActivity();
                        Toast.makeText(SetupActivity.this,"Your Account is created Successfully.",Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                    else
                    {
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this, "Error Occurred: " + message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });

        }
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SetupActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
