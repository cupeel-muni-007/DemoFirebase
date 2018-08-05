package com.nilkamal.firebasedemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nilkamal.MyUtil.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static com.nilkamal.MyUtil.Utilities.hasInternetConnection;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int IMAGE_CHOOSE_CODE = 101;

    private FirebaseAuth mAuth;
//    private FirebaseFirestore db;
    private final String TAG = "HomeActivity";
    private TextView username_txt,mail_txt,contact_txt,updatePicView;
    private ImageView profilePicHolder;
    private Button logoutBtn;
    private View imageProgressHolder;
    private StorageReference mStorageRef;
    File localFile;
    CardView cardView;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
//        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        loadUIComponents();
        if(hasInternetConnection(this)){
            getUserInfo();
        }else{
            toastMessage("Internet Connection Not available. Some features may not work.");
        }
    }


    private void loadUIComponents() {


        username_txt = findViewById(R.id.username_holder);
        mail_txt = findViewById(R.id.user_mail_holder);
        contact_txt = findViewById(R.id.user_contactHolder);

        logoutBtn = findViewById(R.id.log_out_btn);
        logoutBtn.setOnClickListener(this);


        profilePicHolder = findViewById(R.id.profile_imgview);

        cardView = findViewById(R.id.cardview);
        cardView.setOnClickListener(this);

        updatePicView = findViewById(R.id.update_pic_button);
        updatePicView.setOnClickListener(this);

//        messageHolder = findViewById(R.id.home_message_holder);

        findViewById(R.id.home_activity_parent).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                updatePicView.setVisibility(View.GONE);
                if(localFile!=null) {
                    profilePicHolder.setImageURI(Uri.fromFile(localFile));
                }else{
                    profilePicHolder.setImageResource(R.drawable.default_user);
                }
                return true;

            }
        });


        imageProgressHolder = findViewById(R.id.image_progress);

    }


    @Override
    protected void onStart() {
        super.onStart();
        if(Utilities.hasInternetConnection(this)) {
            imageProgressHolder.setVisibility(View.VISIBLE);
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            mStorageRef = FirebaseStorage.getInstance().getReference("/profilepics/" + userId).child("profile.jpg");
            setProfilePicture(mStorageRef);
        }else{
            toastMessage("No Internet Connection");
            updateImgBack();
        }
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.log_out_btn:
                mAuth.signOut();
                finish();
                startActivity(new Intent(this,MainActivity.class));
                break;
            case R.id.update_pic_button:
                changePicture();
                break;
            case R.id.cardview:
                profilePicHolder.setImageDrawable(null);
                updatePicView.setClickable(true);
                updatePicView.setVisibility(View.VISIBLE);
                break;

        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If there's a download in progress, save the reference so you can query it later
        if (mStorageRef != null) {
            outState.putString("reference", mStorageRef.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // If there was a download in progress, get its reference and create a new StorageReference
        final String stringRef = savedInstanceState.getString("reference");
        if (stringRef == null) {
            return;
        }
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);

        // Find all DownloadTasks under this StorageReference (in this example, there should be one)
        List<FileDownloadTask> tasks = mStorageRef.getActiveDownloadTasks();
        if (tasks.size() > 0) {
            // Get the task monitoring the download
            FileDownloadTask task = tasks.get(0);

            // Add new listeners to the task using an Activity scope
            task.addOnSuccessListener(this, new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot state) {
                    setProfilePicture(state.getStorage());
                }
            });
        }
    }

    private void setProfilePicture(StorageReference reference) {
//        Glide.with(this /*context*/)
//                .load(reference)
//                .into(profilePicHolder);



        try {
            localFile =  File.createTempFile("images", "jpg");
            reference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    profilePicHolder.setImageURI(Uri.fromFile(localFile));
                    updateImgBack();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    if(exception instanceof StorageException){
                        StorageException error = (StorageException) exception;
                        if(error.getErrorCode() == -13010 && error.getHttpResultCode() == 404){
                            profilePicHolder.setImageResource(R.drawable.default_user);
                        }
                    }else{
                        toastMessage(exception.getMessage());
                    }
                    updateImgBack();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            updateImgBack();
        }
    }


    private void updateImgBack(){
        imageProgressHolder.setVisibility(View.GONE);
        updatePicView.setVisibility(View.GONE);

        if(imageUri != null){
            profilePicHolder.setImageURI(imageUri);
        }
        if(profilePicHolder.getDrawable() == null) {
            profilePicHolder.setImageResource(R.drawable.default_user);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_CHOOSE_CODE && resultCode == RESULT_OK && data != null && data.getData()!=null){
                imageUri = data.getData();
//                profilePicHolder.setImageURI(imageUri);
                updatePicView.setVisibility(View.GONE);
                imageProgressHolder.setVisibility(View.VISIBLE);
                uploadImageToFirebaseStorage();
        }
    }

    private void uploadImageToFirebaseStorage() {
//                after we get the imageuri we will upload the image to the firebase storage4

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference("/profilepics/"+userId).child("profile.jpg");

        if(imageUri != null){
            mStorageRef.putFile(imageUri)
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){
                                toastMessage("Successfully Updated Profile Picture");
                            }else{
                                toastMessage(task.getException().getMessage());
                            }
                        }
                    });
        }

    }

    private void changePicture() {
        Intent changePic = new Intent();
        changePic.setType("image/*");
        changePic.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(changePic,"Select Profile Picture"),IMAGE_CHOOSE_CODE);
    }


    private void getUserInfo(){

//        firestore example
//        db.collection("users")
//            .get()
//            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                    if (task.isSuccessful()) {
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            Log.d(TAG, document.getId() + " => " + document.getData());
//                        }
//                    } else {
//                        Log.w(TAG, "Error getting documents.", task.getException());
//                    }
//                }
//            });



//        using real time database
        FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        HashMap<String,String> userVal = (HashMap<String,String>) dataSnapshot.getValue();
                        username_txt.setText(userVal.get("name"));
                        contact_txt.setText("Phone: "+userVal.get("phone"));
                        mail_txt.setText("Email: "+userVal.get("email"));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }


}
