package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

public class ClickPostActivity extends AppCompatActivity
{
    private ImageView PostImage;
    private TextView PostDescription, PostName, PostPrice, PostCategory;
    private Button DeletePostButton, EditPostButton;

    private DatabaseReference ClickPostRef;
    private FirebaseAuth mAuth;

    private String PostKey, currenUserID, databaseUserID; //currentUserID is the unique id of user who is online

    private String description, pname, price, category, postimage; //declared here becuz need to use manytime (used to in onDataChange())



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);

        //get the id of the user who will be online
        mAuth = FirebaseAuth.getInstance();
        currenUserID = mAuth.getCurrentUser().getUid(); //user who will be online


        //receive post key from main
        PostKey = getIntent().getExtras().get("PostKey").toString();
        ClickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);//searching for PostKey only in database Posts

        PostImage = (ImageView)findViewById(R.id.click_post_image);
        PostDescription = (TextView) findViewById(R.id.click_post_description);
        PostName = (TextView) findViewById(R.id.click_post_name);
        PostPrice = (TextView) findViewById(R.id.click_post_price);
        PostCategory = (TextView) findViewById(R.id.click_post_category);
        DeletePostButton = (Button) findViewById(R.id.delete_post_button);
        EditPostButton = (Button) findViewById(R.id.edit_post_button);


        DeletePostButton.setVisibility(View.INVISIBLE);
        EditPostButton.setVisibility(View.INVISIBLE);

        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
               //check for the child if the datasnapshot exits
                if (dataSnapshot.exists())
                {
                    description = dataSnapshot.child("description").getValue().toString();
                    pname = dataSnapshot.child("pname").getValue().toString();
                    category = dataSnapshot.child("category").getValue().toString();
                    price = dataSnapshot.child("price").getValue().toString();
                   // postimage = dataSnapshot.child("postimage").getValue().toString();

                    if(dataSnapshot.hasChild("postimage"))
                    {
                        //refer to database profileimage
                        String postimage = dataSnapshot.child("postimage").getValue(String.class);
                        Picasso.get().load(postimage).into(PostImage);
                    }
                    else
                    {
                        Toast.makeText(ClickPostActivity.this, "Error getting Images",Toast.LENGTH_SHORT).show();
                    }

                    databaseUserID = dataSnapshot.child("uid").getValue(String.class); // uid in the Posts (firebase database)

                    PostDescription.setText(description);
                    PostName.setText(pname);
                    PostPrice.setText(price);
                    PostCategory.setText(category);
                   // Picasso.get().load(postimage).into(PostImage);







                    //check whether the post belong to the user so that user can edit
                    if(currenUserID.equals(databaseUserID))
                    {
                        //visible the delete and edit button
                        DeletePostButton.setVisibility(View.VISIBLE);
                        EditPostButton.setVisibility(View.VISIBLE);
                    }

                    EditPostButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v)
                        {
                            //pass the variables to this method, so user can edit
                            EditCurrentPost(description,pname,price,category);

                        }
                    });

                }
                else
                {
                    Toast.makeText(ClickPostActivity.this, "Error Occured: failed to get image" , Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        DeletePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                DeleteCurrentPost();
            }
        });


    }

    private void EditCurrentPost(String description, String pname, String price, String category)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Product");

        LinearLayout layout = new LinearLayout(ClickPostActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputField = new EditText(ClickPostActivity.this);
        inputField.setText(description);
        layout.addView(inputField);

        final EditText inputField1 = new EditText(ClickPostActivity.this);
        inputField1.setText(pname);
        layout.addView(inputField1);

        final EditText inputField2 = new EditText(ClickPostActivity.this);
        inputField2.setText(price);
        layout.addView(inputField2);

        final EditText inputField3 = new EditText(ClickPostActivity.this);
        inputField3.setText(category);
        layout.addView(inputField3);

        builder.setView(layout);

        //create update and cancel button
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //makesure name same as database
                ClickPostRef.child("description").setValue(inputField.getText().toString());
                ClickPostRef.child("pname").setValue(inputField1.getText().toString());
                ClickPostRef.child("price").setValue(inputField2.getText().toString());
                ClickPostRef.child("category").setValue(inputField3.getText().toString());

                Toast.makeText(ClickPostActivity.this,"Post Updated successfully.",Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.darker_gray);

    }

    private void DeleteCurrentPost()
    {
        ClickPostRef.removeValue(); //delete post from firebase database not for storage
        SendUserToMainActivity();
        Toast.makeText(this,"Product has been deleted.", Toast.LENGTH_SHORT).show();

    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(ClickPostActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
