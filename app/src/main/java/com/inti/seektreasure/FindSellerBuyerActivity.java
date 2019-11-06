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
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Create by OCF on 2019
 */


public class FindSellerBuyerActivity extends AppCompatActivity
{
    private Toolbar mToolbar;

    private ImageButton SearchButton;
    private EditText SearchInputText;

    private RecyclerView SearchResultList;

    private DatabaseReference allUsersDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_seller_buyer);

        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");


        mToolbar = (Toolbar) findViewById(R.id.find_seller_buyer_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Search Seller Buyer");


        SearchResultList = (RecyclerView) findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(this));

        SearchButton = (ImageButton)findViewById(R.id.search_people_button);
        SearchInputText = (EditText) findViewById(R.id.search_box_input);

        SearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String searchBoxInput = SearchInputText.getText().toString();
                String searchBoxInput1 = searchBoxInput.substring(0,1).toUpperCase()+ searchBoxInput.substring(1);

                SearchSellerBuyer(searchBoxInput1);

            }
        });

    }
//using firebase recycler adapter to retrieving all the user information
    //create module (FindSellerBuyer) for first parameter to retrieve the username,status and country
    //second parameter is static class which is FindSellerBuyerViewHolder

    private void SearchSellerBuyer(String searchBoxInput1)
    {
        Toast.makeText(this,"Searching...",Toast.LENGTH_SHORT).show();

       // Query searchSellerBuyerQuery = allUsersDatabaseRef.orderByChild("fullname")
           //     .startAt(searchBoxInput.endsWith(searchBoxInput + "\uf8ff");

        FirebaseRecyclerOptions<FindSellerBuyer> options=new FirebaseRecyclerOptions.Builder<FindSellerBuyer>()
                .setQuery(allUsersDatabaseRef.orderByChild("fullname").startAt(searchBoxInput1), FindSellerBuyer.class).build(); //query build past the query to FirebaseRecyclerAdapter




        FirebaseRecyclerAdapter<FindSellerBuyer, FindSellerBuyerActivity.FindSellerBuyerViewHolder> adapter=new FirebaseRecyclerAdapter<FindSellerBuyer, FindSellerBuyerActivity.FindSellerBuyerViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull FindSellerBuyerActivity.FindSellerBuyerViewHolder holder, final int position, @NonNull FindSellerBuyer model) {
                final String PostKey = getRef(position).getKey();
                holder.fullname.setText(model.getFullname());
                holder.status.setText(model.getStatus());
                holder.country.setText(model.getCountry());
                Picasso.get().load(model.getProfileimage()).into(holder.profileimage);

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();

                        Intent profileIntent = new Intent(FindSellerBuyerActivity.this, PersonProfileActivity.class);
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
            public FindSellerBuyerActivity.FindSellerBuyerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
            {
                View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_users_display_layout,viewGroup,false);

                FindSellerBuyerActivity.FindSellerBuyerViewHolder viewHolder=new FindSellerBuyerActivity.FindSellerBuyerViewHolder(view);
                return viewHolder;
            }
        };

        SearchResultList.setAdapter(adapter);
        adapter.startListening();
    }

    public class FindSellerBuyerViewHolder extends RecyclerView.ViewHolder
    {
        TextView fullname, status, country;
        CircleImageView profileimage;
        View mView;

        public FindSellerBuyerViewHolder(@NonNull View itemView)
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
