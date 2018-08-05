package com.nilkamal.firebasedemo;

import android.content.Intent;
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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.nilkamal.MyUtil.Utilities;

import static com.nilkamal.MyUtil.Utilities.setFragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private View mView;
    private RegisterFragment mRegisterFragment;
    private FirebaseAuth mAuth;
    private EditText mail_et,pass_et;
    private String user_mail_str, pass_str;
    private final int viewId = R.id.main_activity_frame;


    private ProgressBar progressBar;

    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        mView = inflater.inflate(R.layout.fragment_login, container, false);
        mRegisterFragment = new RegisterFragment();
        mail_et = mView.findViewById(R.id.email_edit_txt_login);
        pass_et = mView.findViewById(R.id.pass_edit_txt_login);
        mView.findViewById(R.id.btn_log_login).setOnClickListener(this);
        progressBar = mView.findViewById(R.id.login_progress);
        TextView newUser = mView.findViewById(R.id.btn_sign_up_login);
        newUser.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        if(getArguments()!=null && !getArguments().getString("email").isEmpty()){
            user_mail_str = getArguments().getString("email");
            pass_str = getArguments().getString("password");
            mail_et.setText(user_mail_str);
            pass_et.setText(pass_str);
        }
        return mView;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_log_login:
                if (Utilities.hasInternetConnection(getActivity().getApplicationContext()) ) {
                    login();
                }else {
                    toastMessage("Internet Not Connected. Check Internet Connection");
                }
                break;
            case R.id.btn_sign_up_login:
                setFragment(getActivity().getSupportFragmentManager(),viewId,mRegisterFragment,null);
                break;
        }
    }

    private void login() {
        user_mail_str = mail_et.getText().toString().trim();
        pass_str = pass_et.getText().toString().trim();
        if(isValid(user_mail_str,pass_str)) {
            progressBar.setVisibility(View.VISIBLE);
            mView.findViewById(R.id.log_progress_layout).setBackgroundColor(Color.parseColor("#C3000000"));
            mAuth.signInWithEmailAndPassword(user_mail_str, pass_str)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            mView.findViewById(R.id.log_progress_layout).setBackgroundColor(Color.parseColor("#00FFFFFF"));
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d("Login()", "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                // If sign in fails, display a message to the user.

                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    toastMessage("Invalid Credentials");
                                }
                                else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                    toastMessage("User Not Registered");
                                }else{
                                    Log.w("Login()", "signInWithEmail:failure", task.getException());
                                    toastMessage("Authentication failed.");
                                }
                            }

                            // ...
                        }
                    });
        }

    }

    private void updateUI(FirebaseUser currentUser){
        if(currentUser!=null){
            getActivity().finish();
            startActivity(new Intent(getActivity().getApplicationContext(),HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        }
    }

    private void toastMessage(String message){
        Toast.makeText(getActivity().getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }


    private boolean isValid(String email,String password){
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
        return true;
    }

}
