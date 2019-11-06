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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Create by OCF on 2019
 */

public class NearbyActivity extends AppCompatActivity
{

    private Toolbar mToolbar;

    private ImageButton NearbySearchButton;
    private EditText NearbySearchInputText;

    private RecyclerView NearbySearchResultList;

    private DatabaseReference allUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");


        mToolbar = (Toolbar) findViewById(R.id.find_nearby_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Search Seller Buyer");


        NearbySearchResultList = (RecyclerView) findViewById(R.id.nearby_search_result_list);
        NearbySearchResultList.setHasFixedSize(true);
        NearbySearchResultList.setLayoutManager(new LinearLayoutManager(this));

        NearbySearchButton = (ImageButton)findViewById(R.id.nearby_search_people_button);
        NearbySearchInputText = (EditText) findViewById(R.id.nearby_search_box_input);

        NearbySearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String searchBoxInput = NearbySearchInputText.getText().toString();
                String searchBoxInput1 = searchBoxInput.substring(0, 1).toUpperCase() + searchBoxInput.substring(1);

                NearbySearchSellerBuyer(searchBoxInput1);

            }
        });

    }
//using firebase recycler adapter to retrieving all the user information
    //create module (FindSellerBuyer) for first parameter to retrieve the username,status and country
    //second parameter is static class which is FindSellerBuyerViewHolder

    private void NearbySearchSellerBuyer(String searchBoxInput1)
    {
        Toast.makeText(this,"Searching...",Toast.LENGTH_SHORT).show();

        // Query searchSellerBuyerQuery = allUsersDatabaseRef.orderByChild("fullname")
        //     .startAt(searchBoxInput.endsWith(searchBoxInput + "\uf8ff");



        FirebaseRecyclerOptions<Nearby> options=new FirebaseRecyclerOptions.Builder<Nearby>()
                .setQuery(allUsersDatabaseRef.orderByChild("country").startAt(searchBoxInput1), Nearby.class).build();



        FirebaseRecyclerAdapter<Nearby, NearbyActivity.NearbyViewHolder> adapter=new FirebaseRecyclerAdapter<Nearby, NearbyActivity.NearbyViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull NearbyActivity.NearbyViewHolder holder, final int position, @NonNull Nearby model) {
                final String PostKey = getRef(position).getKey();
                holder.fullname.setText(model.getFullname());
                holder.status.setText(model.getStatus());
                holder.country.setText(model.getCountry());
                Picasso.get().load(model.getProfileimage()).into(holder.profileimage);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();

                        Intent profileIntent = new Intent(NearbyActivity.this, PersonProfileActivity.class);
                        profileIntent.putExtra("visit_user_id", visit_user_id);
                        startActivity(profileIntent);
                    }
                });

//                holder.itemView.setOnClickListener(new View.OnClickListener()
//                {
//                    @Override
//                    public void onClick(View v)
//                    {
//                        Intent findSellerBuyerIntent = new Intent(FindSellerBuyerActivity.this, FindSellerBuyerActivity.class);
//                        findSellerBuyerIntent.putExtra("PostKey", PostKey);
//                        startActivity(findSellerBuyerIntent);
//
//                    }
//                });
            }
            @NonNull
            @Override
            public NearbyActivity.NearbyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout,viewGroup,false);

                NearbyActivity.NearbyViewHolder viewHolder=new NearbyActivity.NearbyViewHolder(view);
                return viewHolder;
            }
        };

        NearbySearchResultList.setAdapter(adapter);
        adapter.startListening();
    }

    public class NearbyViewHolder extends RecyclerView.ViewHolder
    {
        TextView fullname, status, country;
        CircleImageView profileimage;
        View mView;

        public NearbyViewHolder(@NonNull View itemView)
        {
            super(itemView);
            mView = itemView;

            fullname = itemView.findViewById(R.id.all_users_profile_full_name);
            status = itemView.findViewById(R.id.all_users_status);
            country = itemView.findViewById(R.id.all_users_country);
            profileimage = itemView.findViewById(R.id.all_users_profile_image);
        }
    }
}
