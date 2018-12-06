package com.example.ramya.adminapp2;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import okhttp3.OkHttpClient;

import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.cache.http.ApolloHttpCache;
import com.apollographql.apollo.cache.http.DiskLruHttpCacheStore;
import com.apollographql.apollo.exception.ApolloException;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;

import javax.annotation.Nonnull;



public class MainActivity extends AppCompatActivity {

    public ApolloClient apolloClient;

    private static final int PICK_IMAGE_REQUEST=1;
    private Button mButtonChooseImage;
    private Button mButtonUpload;
    private TextView mTextViewShowUploads;
    private EditText mEditTextFileName;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private Uri mImageUri;
    private String url;
    private String name;

    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;

    private StorageTask mUploadTask;
    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonChooseImage = findViewById(R.id.button_choose_image);
        mButtonUpload = findViewById(R.id.button_upload);
        mTextViewShowUploads =findViewById(R.id.text_view_show_uploads);
        mEditTextFileName = findViewById(R.id.edit_text_file_name);
        mImageView = findViewById(R.id.image_view);
        mProgressBar = findViewById(R.id.progress_bar);

        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");


        mButtonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });
        mButtonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUploadTask!=null&&mUploadTask.isInProgress()){
                    Toast.makeText(MainActivity.this,"Upload is in Progress",Toast.LENGTH_SHORT).show();
                }else {
                    uploadFile();

                }
            }
        });
        mTextViewShowUploads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagesActivity();
            }
        });

    }

    private class Mytask extends AsyncTask<String,Integer,String> {

        @Override
        protected String doInBackground(String... strings) {
            callquery();

            return null;
        }
    }

    void callquery(){
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
        AddbannerMutation addbannerMutation = AddbannerMutation.builder()
                .imageurl(url)
                .bannerlink(name)
                .build();
        ApolloCall<AddbannerMutation.Data> call = apolloClient.mutate(addbannerMutation);
        call.enqueue(new ApolloCall.Callback<AddbannerMutation.Data>() {
            @Override
            public void onResponse(@Nonnull Response<AddbannerMutation.Data> response) {
                AddbannerMutation.Data res = response.data();

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplication(), "User registered Successfully", Toast.LENGTH_SHORT).show();
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
    private void openFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode== RESULT_OK
                && data!= null && data.getData()!= null ){
            mImageUri = data.getData();
            Picasso.with(this).load(mImageUri).into(mImageView);

        }
    }
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getMimeTypeFromExtension(cR.getType(uri));
    }

    private void uploadFile(){
        if (mImageUri!=null){
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis()+"."+
                getFileExtension(mImageUri));
            mUploadTask = fileReference.putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        public static final String TAG = "MainActivity" ;

                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setProgress(0);
                                }
                            },100);
                            Toast.makeText(MainActivity.this,"Image Uploaded",Toast.LENGTH_SHORT).show();
                            Upload upload = new Upload(mEditTextFileName.getText().toString().trim(),
                                    taskSnapshot.getDownloadUrl().toString());
                            String uploadId = mDatabaseRef.push().getKey();
                            mDatabaseRef.child(uploadId).setValue(upload);
                            url = taskSnapshot.getDownloadUrl().toString();
                            name = mEditTextFileName.getText().toString();
                            new Mytask().execute();
                            Log.d(TAG, url +""+name);
//                            Intent i = new Intent(MainActivity.this, ImagesActivity.class);
//                            Bundle bundle = new Bundle();
////Add your data from getFactualResults method to bundle
//                            bundle.putString("name", name);
////Add the bundle to the intent
//                            i.putExtras(bundle);
                            mEditTextFileName.setText(null);
                            mImageView.setImageDrawable(null);
//                            startActivity(i);


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/
                                taskSnapshot.getTotalByteCount());
                            mProgressBar.setProgress((int)progress);
                        }
                    });

        }else {
            Toast.makeText(this,"No Image Selected",Toast.LENGTH_SHORT).show();
        }

    }
    private void openImagesActivity(){
        Intent intent = new Intent(this,ImagesActivity.class);
        startActivity(intent);

    }
    @Override
    public void onBackPressed(){

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
