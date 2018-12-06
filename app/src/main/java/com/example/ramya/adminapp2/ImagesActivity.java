package com.example.ramya.adminapp2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.http.ApolloHttpCache;
import com.apollographql.apollo.cache.http.DiskLruHttpCacheStore;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import okhttp3.OkHttpClient;

public class ImagesActivity extends AppCompatActivity implements ImageAdapter.OnItemClickListener{
    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    public ApolloClient apolloClient;
    private ProgressBar mProgressCircle;
    private FirebaseStorage mStorage;
    private DatabaseReference mDatabaseRef;
    private ValueEventListener mDBListener;
    private List<Upload> mUploads;
    private String names;
    private EditText ed;
    private Button bt;

    public void bto(View view){

        names = ed.getText().toString();
               ed.setText("");

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images);


        ed = findViewById(R.id.getname);
        mRecyclerView = findViewById(R.id.recycler_view);
        mProgressCircle = findViewById(R.id.progress_circle);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUploads = new ArrayList<>();

        mAdapter = new ImageAdapter(ImagesActivity.this,mUploads);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener(ImagesActivity.this);

        mStorage= FirebaseStorage.getInstance();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

//        Bundle bundle = getIntent().getExtras();
//
//        name = bundle.getString("name");

//        bt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                names = ed.getText().toString();
//                ed.setText("");
//            }
//        });
        mDBListener = mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUploads.clear();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    Upload upload = postSnapshot.getValue(Upload.class);
                    upload.setkey(postSnapshot.getKey());
                    mUploads.add(upload);
                }
                mAdapter.notifyDataSetChanged();

                mProgressCircle.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ImagesActivity.this,databaseError.getMessage(),Toast.LENGTH_SHORT).show();
                mProgressCircle.setVisibility(View.INVISIBLE);

            }
        });
    }



    @Override
    public void onItemClick(int position) {
        Toast.makeText(this,"Normal click at position"+position,Toast.LENGTH_SHORT).show();


    }

    @Override
    public void onWhatEverClick(int position) {
        Toast.makeText(this,"whatever click at position"+position,Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDeleteClick(int position) {
        final Upload selectedItem = mUploads.get(position);
        final String selectedKey = selectedItem.getKey();

        final StorageReference imageRef = mStorage.getReferenceFromUrl(selectedItem.getImageUrl());
        imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("url",selectedItem.toString());

               mDatabaseRef.child(selectedKey).removeValue();


               deletionfunction();
                Toast.makeText(ImagesActivity.this,"Image Deleted",Toast.LENGTH_SHORT).show();

            }
        });

    }

    void deletionfunction(){
        File file = new File(this.getCacheDir().toURI());
        //Size in bytes of the cache
        int size = 1024*1024;

        //Create the http response cache store
        DiskLruHttpCacheStore cacheStore = new DiskLruHttpCacheStore(file, size);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .build();

        apolloClient = ApolloClient.builder()
                .serverUrl("https://digicashserver.herokuapp.com/graphql")
                .httpCache(new ApolloHttpCache(cacheStore))
                .okHttpClient(okHttpClient)
                .build();
        DeletebannerMutation deletebannerMutation = DeletebannerMutation.builder()
                .bannerlink(names)
                .build();
        ApolloCall<DeletebannerMutation.Data> call = apolloClient.mutate(deletebannerMutation);
        call.enqueue(new ApolloCall.Callback<DeletebannerMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<DeletebannerMutation.Data> response) {
                DeletebannerMutation.Data res = response.data();

                ImagesActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(), "Banner deleted Successfully", Toast.LENGTH_SHORT).show();
                        //Toast.makeText(MainActivity.this, pmobileno2+pwallet2+pusername2, Toast.LENGTH_SHORT).show();

                    }
                });
            }
            @Override
            public void onFailure(@Nonnull ApolloException e) {
                Log.e("Fail", "onFailure: ",e );
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDatabaseRef.removeEventListener(mDBListener);
    }
}
