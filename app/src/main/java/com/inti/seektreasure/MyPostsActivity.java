package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Create by OCF on 2019
 */

public class MyPostsActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private RecyclerView myPostsList;
    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef, UsersRef, LikesRef;
    private String currentUserID;

    Boolean LikeChecker = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        //store user information in Users parent node
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        //parent node to stored how much the user like
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        mToolbar = (Toolbar) findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Posts");


        myPostsList = (RecyclerView) findViewById(R.id.my_all_posts_list);
        //set fixed size and linear layout
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //display new post at the top , old post at the bottom
        //by using linearLayoutManager to accessing it
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);

        DisplayMyAllPosts();
    }

    private void DisplayMyAllPosts()
    {
        //order by child is to get uid (get specific firebase query
        Query myPostsQuery = PostsRef.orderByChild("uid")
                .startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(myPostsQuery,Posts.class).build();

        FirebaseRecyclerAdapter<Posts,MyPostsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull MyPostsViewHolder holder, int position, @NonNull Posts model)
            {

                final String PostKey = getRef(position).getKey(); // the key for the posts

                //from main activity
                holder.username.setText(model.getFullname());
                holder.time.setText(" " +model.getTime());
                holder.date.setText(" "+model.getDate());
                holder.description.setText("Description: "+model.getDescription());
                holder.pname.setText("Product: " + model.getPname());
                holder.category.setText("Category: " + model.getCategory());
                holder.price.setText("Price: " + model.getPrice());
                Picasso.get().load(model.getProfileimage()).into(holder.post_profile_image);
                Picasso.get().load(model.getPostimage()).into(holder.post_image);

                holder.setLikeButtonStatus(PostKey);


                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent clickPostIntent = new Intent (MyPostsActivity.this, ClickPostActivity.class);
                        //send post key from main to click post activity so we can retrieve it at clickpost
                        clickPostIntent.putExtra("PostKey",PostKey);
                        startActivity(clickPostIntent);
                    }
                });

//                        holder.setFullname(model.getFullname());
//                        holder.setDescription(model.getDescription());
//                        holder.setProfileImage(getApplicationContext(),model.getProfileimage());
//                        holder.setPostImage(getApplicationContext(),model.getPostimage());
//                        holder.setDate(model.getDate());
//                        holder.SetTime(model.getTime());

                holder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent commentsIntent = new Intent (MyPostsActivity.this, CommentsActivity.class);
                        //send post key from main to click post activity so we can retrieve it at clickpost
                        commentsIntent.putExtra("PostKey",PostKey);
                        startActivity(commentsIntent);

                    }
                });

                holder.LikepostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        //check user whether already like or not
                        LikeChecker = true; //when user click it will be try

                        //then will turn to red color love
                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if(LikeChecker.equals(true))
                                {
                                    if(dataSnapshot.child(PostKey).hasChild(currentUserID))
                                    {
                                        //check if the like are already exist then do this
                                        //if the post already if he like again , it will unlike the post
                                        LikesRef.child(PostKey).child(currentUserID).removeValue();
                                        LikeChecker = false; //if click again = unlike the post

                                    }
                                    else
                                    {
                                        //if like do not exist already then will set the value
                                        LikesRef.child(PostKey).child(currentUserID).setValue(true);
                                        LikeChecker = false; // if click again = unlike
                                    }
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

            @NonNull
            @Override
            public MyPostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i)
            {
                //all post layout had been created previously
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);

                MyPostsViewHolder viewHolder = new MyPostsViewHolder(view);

                return viewHolder;
            }
        };

        myPostsList.setAdapter(adapter);
        adapter.startListening();
    }



    public static class MyPostsViewHolder extends RecyclerView.ViewHolder
    {
        //copy from main activity taht already created last time
        TextView username,date,time,description, price, category, pname;
        CircleImageView post_profile_image;
        ImageView post_image;

        View mView;

        //initialise like button
        ImageButton LikepostButton, CommentPostButton;
        TextView DisplayNoOfLikes;
        int countLikes;
        String currentUserId; //stored current user id
        DatabaseReference LikesRef;


        //generate constructor
        public MyPostsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            mView = itemView;

            //get the items
            username = itemView.findViewById(R.id.post_user_name);
            date = itemView.findViewById(R.id.post_date);
            time = itemView.findViewById(R.id.post_time);
            description = itemView.findViewById(R.id.post_description);
            price = itemView.findViewById(R.id.post_price);
            category = itemView.findViewById(R.id.post_category);
            pname = itemView.findViewById(R.id.post_name);
            post_image = itemView.findViewById(R.id.post_image);
            post_profile_image = itemView.findViewById(R.id.post_profile_image);

            LikepostButton = itemView.findViewById(R.id.like_button);
            CommentPostButton = itemView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = itemView.findViewById(R.id.display_no_of_likes);


            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            //get current user id
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }


        public void setLikeButtonStatus(final String PostKey)
        {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.child(PostKey).hasChild(currentUserId))
                    {
                        //stored it as integer
                        //display the number of likes
                        //count the number of likes on the single post and will stored it in countLikes integer variable
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikepostButton.setImageResource(R.drawable.like);
                        DisplayNoOfLikes.setText(Integer.toString(countLikes) + (" Likes"));
                    }
                    else
                    {
                        //if user unlike the post
                        countLikes = (int) dataSnapshot.child(PostKey).getChildrenCount();
                        LikepostButton.setImageResource(R.drawable.dislike);
                        DisplayNoOfLikes.setText(Integer.toString(countLikes) + (" Likes"));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
            });
        }
    }
}
