package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity
{
    private RecyclerView CommentsList;
    private ImageButton PostCommentButton;
    private EditText CommentInputText;

    private DatabaseReference UsersRef, PostsRef; // already create on Main //postsref is for save comment things, already in post activity
    private FirebaseAuth mAuth; // to get user current id

    private String Post_Key, current_user_id;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        //retrieve from main activity
        Post_Key = getIntent().getExtras().get("PostKey").toString();

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid(); //get user id
        //Users node, need to fetch the user name from the users node and save it along side with user comments date, time and so on
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(Post_Key).child("Comments");


        CommentsList = (RecyclerView) findViewById(R.id.comments_list);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        CommentsList.setLayoutManager(linearLayoutManager);

        CommentInputText = (EditText) findViewById(R.id.comments_input);
        PostCommentButton = (ImageButton) findViewById(R.id.post_comment_btn);

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //referencing on the user who are going online so it is current_user_id to retrive it names ...
                UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            //retrieve user name and make sure the child name same as database
                            String userName = dataSnapshot.child("username").getValue().toString();

                            ValidateComment(userName);

                            CommentInputText.setText("");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
            }
        });

    }

    //create onStart method, inside this do firebase recycler adapter
    //1st parameter is comments model class
    //2st is commentsViewHolder


    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Comments> options = new FirebaseRecyclerOptions.Builder<Comments>()
                .setQuery(PostsRef, Comments.class).build();


        FirebaseRecyclerAdapter<Comments, CommentsViewHolder> adapter = new FirebaseRecyclerAdapter<Comments, CommentsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull CommentsViewHolder holder, int position, @NonNull Comments model) {

                holder.myUserName.setText("@" + model.getUsername()+" ");
                holder.myComment.setText(model.getComment());
                holder.myDate.setText("  Date: "+ model.getDate());
                holder.myTime.setText("  Time: "+ model.getTime());


            }

            @NonNull
            @Override
            public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_comments_layout, viewGroup, false);
                CommentsViewHolder viewHolder = new CommentsViewHolder(view);
                return viewHolder;
            }
        };

        CommentsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        TextView myComment, myDate, myTime, myUserName;
        //get the recycler view
        View mView;

        //generate constructor
        public CommentsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            mView = itemView;
            myUserName = itemView.findViewById(R.id.comment_username);
            myComment = itemView.findViewById(R.id.comment_text);
            myDate = itemView.findViewById(R.id.comment_date);
            myTime = itemView.findViewById(R.id.comment_time);


        }
    }

    private void ValidateComment(String userName)
    {
        //get the text from comment input field
        String commentText = CommentInputText.getText().toString();

        if(TextUtils.isEmpty(commentText))
        {
            Toast.makeText(this,"Please write text to comment.",Toast.LENGTH_SHORT).show();

        }
        else
        {
            //stored data
            //first get current time n date, go post activity to copy cuz already created
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy"); //get current date n stored in variable
            //format to string data type
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm"); //get current date n stored in variable
            //format to string data type
            final String saveCurrentTime = currentTime.format(calForDate.getTime());

            final String RandomKey = current_user_id + saveCurrentDate + saveCurrentTime;

            //stored it
            HashMap commentsMap = new HashMap();
            commentsMap.put("uid",current_user_id);
            commentsMap.put("comment",commentText);
            commentsMap.put("date",saveCurrentDate);
            commentsMap.put("time",saveCurrentTime);
            commentsMap.put("username",userName);

            PostsRef.child(RandomKey).updateChildren(commentsMap)
                    .addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task)
                        {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(CommentsActivity.this,"You have commented successfully.",Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(CommentsActivity.this,"Error Occurred. Try Again.",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });



        }
    }
}
