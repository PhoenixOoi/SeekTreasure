package com.inti.seektreasure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
{
    //initialise
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Toolbar mToolbar;

    //check the user authenticate or not
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        //store user information in Users parent node
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        //assign a title ,add the tool bar into main activity
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");


        drawerLayout = (DrawerLayout)findViewById(R.id.drawable_layout);

        //pass two string, one is drawer open , one is drawer close another is drawer layout since we use it in drawer layout
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity.this,drawerLayout,R.string.drawer_open,R.string.drawer_close);

        //add drawer listener on action bar drawer toggle
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState(); //sync it
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        navigationView = (NavigationView) findViewById(R.id.navigation_view);

        //add navigation header , storing the navigation header view inside navView variable
        View navView = navigationView.inflateHeaderView(R.layout.navigation_header);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                //access the navigation options in drawer layer
                UserMenuSelector(menuItem);
                return false;
            }
        });
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
            case R.id.nav_profile:
                Toast.makeText(this,"Profile", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_home:
                Toast.makeText(this,"Home", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_followers:
                Toast.makeText(this,"Followers", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_find_people:
                Toast.makeText(this,"Find People", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_messages:
                Toast.makeText(this,"Messages", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_settings:
                Toast.makeText(this,"Settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.nav_Logout:
                mAuth.signOut(); // sign out the user from firebase authentication
                //send user to login activity (logout)
                SendUserToLoginActivity();
                break;
        }
    }
}
