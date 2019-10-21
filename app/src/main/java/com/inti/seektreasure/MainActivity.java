package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
{
    //initialise
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList; //recyler view to display user post
    private Toolbar mToolbar;

    //for navigation drawer header user profile
    private CircleImageView NavProfileImage;
    private TextView NavProfileUserName;
    private ImageButton AddNewPostButton;

    //check the user authenticate or not
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef, LikesRef;

    //get current user
    String currentUserID;

    Boolean LikeChecker = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        //store user information in Users parent node
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        //parent node to stored how much the user like
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        //assign a title ,add the tool bar into main activity
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        AddNewPostButton = (ImageButton) findViewById(R.id.add_new_post_button);


        drawerLayout = (DrawerLayout)findViewById(R.id.drawable_layout);

        //pass two string, one is drawer open , one is drawer close another is drawer layout since we use it in drawer layout
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);

        //add drawer listener on action bar drawer toggle
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState(); //sync it
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        postList = (RecyclerView) findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //display new post at the top , old post at the bottom
        //by using linearLayoutManager to accessing it
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);




        //add navigation header , storing the navigation header view inside navView variable
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage = (CircleImageView) navView.findViewById(R.id.nav_profile_image);
        NavProfileUserName = (TextView) navView.findViewById(R.id.nav_user_full_name);


        //get the username and image from the firebase database
        //need user references, the user online we only display his images
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    //if the database got user full name then display the name
                    if(dataSnapshot.hasChild("fullname"))
                    {
                        //get the name from the database and store in varibles
                        String fullname = dataSnapshot.child("fullname").getValue().toString();
                        //display on the navigation header
                        NavProfileUserName.setText(fullname);
                    }
                    if (dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(NavProfileImage);
                    }
                    else
                    {
                        Toast.makeText(MainActivity.this,"Profile name do not exists.",Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                //access the navigation options in drawer layer
                UserMenuSelector(menuItem);
                return false;
            }
        });

        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserToPostActivity();

            }
        });

        //inside the on create method, called this method
        DisplayAllUsersPosts();

    }

    //state will be online offline
    public void updateUserStatus(String state)
    {
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a"); //a will be am or pm
        saveCurrentTime = currentTime.format(calForTime.getTime());


        //save in database in Users node
        Map currentStateMap = new HashMap<>();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state); //pass parameter to it

        //already create the references for the user node
        //create another child for saving the online user information(online status and last seen info) under parent node
        UsersRef.child(currentUserID).child("userState")
                .updateChildren(currentStateMap);

    }


    private void DisplayAllUsersPosts()
    {
        //query to solve the post in descending order , refer to post actvity
        Query SortPostsInDescendingOrder = PostsRef.orderByChild("counter");


        FirebaseRecyclerOptions<Posts> options = new FirebaseRecyclerOptions.Builder<Posts>().setQuery(SortPostsInDescendingOrder,Posts.class).build();
        //use firebase recycler to retrieved all the post from firebase
        //add dependency
        //create module class which is Posts,second parameter is static class (created at below PostsViewHolder)
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull PostsViewHolder holder, int position, @NonNull Posts model)
                    {
                        final String PostKey = getRef(position).getKey();

                        holder.username.setText(model.getFullname());
                        holder.time.setText(" " +model.getTime());
                        holder.date.setText(" "+model.getDate());
                        holder.description.setText("Description: "+model.getDescription());
                        holder.pname.setText("Product: " + model.getPname());
                        holder.category.setText("Category: " + model.getCategory());
                        holder.price.setText("Price: " + model.getPrice());
                        Picasso.get().load(model.getProfileimage()).into(holder.post_profile_image);
                        Picasso.get().load(model.getPostimage()).into(holder.post_image);

                        //check the button status, turn red to grey love
                        holder.setLikeButtonStatus(PostKey);


                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                Intent clickPostIntent = new Intent (MainActivity.this, ClickPostActivity.class);
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
                                Intent commentsIntent = new Intent (MainActivity.this, CommentsActivity.class);
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
                    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout,parent,false);
                        PostsViewHolder viewHolder=new PostsViewHolder(view);
                        return viewHolder;
                    }
                };
        firebaseRecyclerAdapter.startListening();
        postList.setAdapter(firebaseRecyclerAdapter);

        //whenever the app run
        //call this method and pass the state
        //the state is online
        updateUserStatus("online");

    }

    //static class for recycler adapter
    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        TextView username,date,time,description, price, category, pname;
        CircleImageView post_profile_image;
        ImageView post_image;

        //initialise like button
        ImageButton LikepostButton, CommentPostButton;
        TextView DisplayNoOfLikes;
        int countLikes;
        String currentUserId; //stored current user id
        DatabaseReference LikesRef;

        View mView;

        public PostsViewHolder(@NonNull View itemView) {
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


//            public void setFullname (String fullname){
//            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
//            username.setText(fullname);
//        }
//
//            public void setProfileImage (Context ctx, String profileimage){
//            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
//            Picasso.get().load(profileimage).into(image);
//
//        }
//
//            public void SetTime (String time){
//            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
//            PostTime.setText("     "+time);
//        }
//
//            public void setDate (String date){
//            TextView postDate = (TextView) mView.findViewById(R.id.post_date);
//            postDate.setText("     "+date);
//        }
//
//            public void setDescription (String description){
//            TextView postDescription = (TextView) mView.findViewById(R.id.post_description);
//            postDescription.setText(description);
//        }
//
//            public void setPostImage (Context ctx1, String postImage){
//            ImageView postImages = (ImageView) mView.findViewById(R.id.post_image);
//            Picasso.get().load(postImage).into(postImages);

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





    private void SendUserToPostActivity()
    {

        Intent addNewPostIntent = new Intent(MainActivity.this,PostActivity.class);
        startActivity(addNewPostIntent);
    }


    @Override
    protected void onStart()
    {
        super.onStart();
        //current online user who will be using seek treasure
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //check user
        if(currentUser == null)
        {
            //send user to login if user is not authentic
            SendUserToLoginActivity();
        }
        else
        {
            //check user exists first then will allow user to stay at the main activity
            CheckUserExistence();
        }
    }

    private void CheckUserExistence()
    {
        //current user id is the id for the user who are going to online or login into the app
        final String current_user_id = mAuth.getCurrentUser().getUid();

        //create references for the realtime database
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //every user will have the unique id in realtime database (current_user_id)
                if(!dataSnapshot.hasChild(current_user_id))
                {
                    //means the user has not setup the things only authentic
                    //sending user to the setup activity
                    SendUserToSetupActivity();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void SendUserToSetupActivity()
    {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        //not allow user to go back to the main activity
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        //not allow user to go back to the main activity
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        //pass the item (object)
        //when user click on it, it will draw out the drawer
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UserMenuSelector(MenuItem menuItem)
    {
        switch (menuItem.getItemId())
        {
            // enable user to tab the option and directed to the page at drawer layout
            case R.id.nav_post:
                SendUserToPostActivity();
                break;
            case R.id.nav_profile:
                SendUserToProfileActivity();
                break;

            case R.id.nav_home:
                Toast.makeText(this,"Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_followers:
                SendUserToFollowersActivity();
                //Toast.makeText(this,"Followers", Toast.LENGTH_SHORT).show();
                break;

                //add nearby
            case R.id.nav_nearby:
                SendUserToNearbyActivity();
                //Toast.makeText(this,"Followers", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_people:
                SendUserToFindSellerBuyerActivity();
                Toast.makeText(this,"Find People", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_messages:
                SendUserToFollowersActivity();// will change to whom we talk for the last time
                Toast.makeText(this,"Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                SendUserToSettingsActivity();
                break;

            case R.id.nav_Logout:
                //when user logout then it become offline and also the last seen
                updateUserStatus("offline");

                mAuth.signOut(); // sign out the user from firebase authentication
                //send user to login activity (logout)
                SendUserToLoginActivity();
                break;
        }
    }

    private void SendUserToNearbyActivity()
    {
        Intent nearbyIntent = new Intent(MainActivity.this, NearbyActivity.class);
        startActivity(nearbyIntent);
    }

    private void SendUserToFollowersActivity()
    {
        Intent followersIntent = new Intent(MainActivity.this, FriendsActivity.class);
        startActivity(followersIntent);

    }

    private void SendUserToSettingsActivity()
    {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);

    }

    private void SendUserToFindSellerBuyerActivity()
    {
        Intent settingsIntent = new Intent(MainActivity.this, FindSellerBuyerActivity.class);
        startActivity(settingsIntent);

    }

    private void SendUserToProfileActivity()
    {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);

    }
}
