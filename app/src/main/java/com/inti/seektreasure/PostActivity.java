package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;


public class PostActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ProgressDialog loadingBar;

    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription, PostPrice, PostCategory, ProductName;

    private static final int Gallery_Pick = 1;
    private Uri ImageUri;
    private String Description, Price, Category, PName;

    private StorageReference PostsImagesReference;
    private DatabaseReference UsersRef, PostsRef; //Postsref for Post node
    private FirebaseAuth mAuth;

    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id;
    private long countPosts = 0; // count number of post in database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid(); // get the current user id


        PostsImagesReference = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");


        SelectPostImage = (ImageButton)findViewById(R.id.select_post_image);
        UpdatePostButton = (Button)findViewById(R.id.update_post_button);
        ProductName = (EditText)findViewById(R.id.post_name);
        PostDescription = (EditText)findViewById(R.id.post_description);
        PostPrice = (EditText)findViewById(R.id.post_price);
        PostCategory = (EditText)findViewById(R.id.post_category);
        loadingBar = new ProgressDialog(this);


        mToolbar = (Toolbar)findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");


        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                OpenGallery();

            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
             ValidatePostInfo();
            }
        });
    }

    private void ValidatePostInfo()
    {
        //get the description...declare at outside so can use it at other places
        PName = ProductName.getText().toString();
        Description = PostDescription.getText().toString();
        Price = PostPrice.getText().toString();
        Category = PostCategory.getText().toString();


        if(ImageUri == null)
        {
            Toast.makeText(this,"Please select post image.",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(PName))
        {
            Toast.makeText(this,"Please write product name.",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(Description))
        {
            Toast.makeText(this,"Please write product description.",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(Price))
        {
            Toast.makeText(this,"Please write product price.",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(Category))
        {
            Toast.makeText(this,"Please write product category.",Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Add New Product");
            loadingBar.setMessage("Please wait , while we are updating your new product.");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            StoringImageToFirebaseStorage();
        }

    }

    private void StoringImageToFirebaseStorage()
    {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy"); //get current date n stored in variable
        //format to string data type
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm"); //get current date n stored in variable
        //format to string data type
        saveCurrentTime = currentTime.format(calForDate.getTime());

        //generate unique id for images
        postRandomName = saveCurrentDate + saveCurrentTime;




        //stored images in firebase storage , create a folder call Post Images
        //important , give a random name to the post images, need to assign a unique name for each user cuz everyone maybe will have same name
        //image name +  unique id to generate key and stored the image in a folder called Post Images
        StorageReference filepath = PostsImagesReference.child("Post Images").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");

//        filepath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
//            {
//                if(task.isSuccessful())
//                {
//                    downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
//
//                    //get the link and stored in firebase database
//                    Toast.makeText(PostActivity.this, "image uploaded successfully to Storage.",Toast.LENGTH_SHORT).show();
//
//                    SavingPostInformationToDatabase();
//
//                }
//                else
//                {
//                    String message = task.getException().getMessage();
//                    Toast.makeText(PostActivity.this,"Error Occurred: " + message,Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });
        //solution test
        filepath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Task<Uri>result = task.getResult().getMetadata().getReference().getDownloadUrl();
                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            final String downloadUrl = uri.toString();
                            PostsRef.child(current_user_id + postRandomName).child("postimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(PostActivity.this, "image uploaded successfully to Storage", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(PostActivity.this, "Image failed to upload. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });

                    Toast.makeText(PostActivity.this,
                            "Image uploaded successfully to storage", Toast.LENGTH_SHORT).show();
                    SavingPostInformationToDatabase();

                }
                else
                    {
                    String message = task.getException().getMessage();
                    Toast.makeText(PostActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                    }
            }
        });
    }

    private void SavingPostInformationToDatabase()
    {

        //PostsRef is link to post node in firebase
        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    //count the number of child (post) and stored it in countPost
                    countPosts = dataSnapshot.getChildrenCount();
                }
                else
                {
                    countPosts = 0; //initialise with o, first post start with 0
                    //solve the post in descending order

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //retrieve the profile photo and user fullname from the users node and store in the post ref
        //first get the current user online by create firebase auth
        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    //make sure child exists
                    //working on post node
                    //put same name with firebase, save fullname in userFullName
                    final String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    final String userProfileImage = dataSnapshot.child("profileimage").getValue().toString();

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", current_user_id); //current user id stored in uid title
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("pname", PName); //product name
                    postsMap.put("description", Description);
                    postsMap.put("price", Price);
                    postsMap.put("category",Category);
                    postsMap.put("postimage", downloadUrl);
                    postsMap.put("profileimage", userProfileImage);
                    postsMap.put("fullname", userFullName);
                    //add child for counter, this will save the value as long datatype
                    postsMap.put("counter",countPosts);


                    //save inside database
                    //combined current user id and random key to be more secured
                    PostsRef.child(current_user_id + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task Posttask)
                                {
                                    if(Posttask.isSuccessful())
                                    {
                                        SendUserToMainActivity();
                                        Toast.makeText(PostActivity.this,"New Post is added successfully",Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else
                                    {
                                        Toast.makeText(PostActivity.this,"Error Occured while updating your post", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }


                                }
                            });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void OpenGallery()
    {
        //direct user to the mobile phone gallery
        //pick a picture
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*"); //which type of file user want to pick
        startActivityForResult(galleryIntent,Gallery_Pick);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        //check the request code
        //display the user on the image button
        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data!= null)
        {
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();

        if(id == android.R.id.home)
        {
            //if user click on back button
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent (PostActivity.this,MainActivity.class);
        startActivity(mainIntent);

    }
}
