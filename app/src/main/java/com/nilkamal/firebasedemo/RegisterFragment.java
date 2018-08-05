package com.nilkamal.firebasedemo;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.nilkamal.MyUtil.Utilities;
import com.nilkamal.models.User;

import java.util.HashMap;
import java.util.Map;

import static com.nilkamal.MyUtil.Utilities.setFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class RegisterFragment extends Fragment implements View.OnClickListener {

    private View mView;
    private LoginFragment mLoginFragment;
    private FirebaseAuth mAuth;
    private EditText mail_et,pass_et,contact_et,name_et;
    private TextView alreadyRegistered;
    private String user_mail_str, pass_str,contact_str,username_str;
    private ProgressBar progressBar;
    private final int viewId = R.id.main_activity_frame;
//    private FirebaseFirestore db;




    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();
//        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
//                .setTimestampsInSnapshotsEnabled(true)
//                .build();
//        db.setFirestoreSettings(settings);
//        db = FirebaseFirestore.getInstance();
        mLoginFragment = new LoginFragment();
        loadUIComponents();
        alreadyRegistered.setOnClickListener(this);
        mView.findViewById(R.id.btn_sign_up_reg).setOnClickListener(this);
        return mView;
    }


    private void loadUIComponents(){
        mail_et = mView.findViewById(R.id.email_edit_txt_reg);
        pass_et = mView.findViewById(R.id.pass_edit_txt_reg);
        name_et = mView.findViewById(R.id.name_edit_txt_reg);
        contact_et = mView.findViewById(R.id.contact_edit_txt_reg);
        progressBar = mView.findViewById(R.id.reg_progress);
        alreadyRegistered = mView.findViewById(R.id.btn_login_reg);
    }

    @Override
    public void onClick(View v) {
        if (Utilities.hasInternetConnection(getActivity().getApplicationContext())) {
            switch (v.getId()) {
                case R.id.btn_login_reg:
                    setFragment(getActivity().getSupportFragmentManager(),viewId,mLoginFragment,null);
                    break;
                case R.id.btn_sign_up_reg:
                    signup();
                    break;
            }
        } else {
            toastMessage("Internet Not Connected. Check Internet Connection");
        }

    }


    private void signup(){
        user_mail_str = mail_et.getText().toString().trim();
        pass_str = pass_et.getText().toString().trim();
        contact_str =  contact_et.getText().toString().trim();
        username_str = name_et.getText().toString().trim();
        if(isValid(user_mail_str,pass_str,contact_str,username_str)){
            progressBar.setVisibility(View.VISIBLE);
            mView.findViewById(R.id.reg_progress_layout).setBackgroundColor(Color.parseColor("#C3000000"));
            mAuth.createUserWithEmailAndPassword(user_mail_str, pass_str)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("signup", "createUserWithEmail:success");

                                User user = new User(user_mail_str,username_str,contact_str);

                                FirebaseDatabase.getInstance().getReference("users")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        progressBar.setVisibility(View.GONE);
                                        mView.findViewById(R.id.reg_progress_layout).setBackgroundColor(Color.parseColor("#00FFFFFF"));
                                        if(task.isSuccessful()){
                                            toastMessage("Successfully Registered");
                                            Bundle args = new Bundle();
                                            args.putString("email",user_mail_str);
                                            args.putString("password",pass_str);
                                            setFragment(getActivity().getSupportFragmentManager(),viewId,mLoginFragment,args);
                                        }else{
                                            toastMessage(task.getException().getMessage());
                                        }
                                    }
                                });
//                                regsiterInfo(user.getUid());
                            } else {
                                // If sign in fails, display a message to the user.
                                progressBar.setVisibility(View.GONE);
                                mView.findViewById(R.id.reg_progress_layout).setBackgroundColor(Color.parseColor("#00FFFFFF"));
                                if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                    toastMessage("User Already Registered");
                                }else {
                                    Log.w("signup", "createUserWithEmail:failure", task.getException());
                                    toastMessage("Authentication failed.");
                                }
                            }

                            // ...
                        }
                    });
        }
    }

    private void regsiterInfo(String userId) {
        // Create a new user with a first and last name
        String fullname = name_et.getText().toString().trim();
        String contact = contact_et.getText().toString().trim();
        Map<String, Object> user = new HashMap<>();
        user.put("Name", fullname);
        user.put("Contact", contact);
        user.put("user-id", userId);

// Add a new document with a generated ID
//        db.collection("users/")
//                .add(user)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        toastMessage("DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        toastMessage("Error adding document"+e.getMessage());
//                    }
//                });
    }


    private void toastMessage(String message){
        Toast.makeText(getActivity().getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }




    private boolean isValid(String email,String password,String contact,String name){
        if(email.isEmpty()){
            mail_et.setError("Email Required");
            mail_et.requestFocus();
            return false;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            mail_et.setError("Invalid Email");
            mail_et.requestFocus();
            return false;
        }

        if(password.isEmpty()){
            pass_et.setError("Password Required");
            pass_et.requestFocus();
            return false;
        }

        if(password.length() < 6){
            pass_et.setError("Minimum 6 characters required");
            pass_et.requestFocus();
            return false;
        }

        if(contact.isEmpty()){
            contact_et.setError("Contact Required");
            contact_et.requestFocus();
            return false;
        }


        if(contact.length() != 10){
            contact_et.setError("Invalid number/ 10 digit required");
            contact_et.requestFocus();
            return false;
        }

        if(name.isEmpty()){
            name_et.setError("Name Required");
            name_et.requestFocus();
            return false;
        }

        return true;
    }
}
