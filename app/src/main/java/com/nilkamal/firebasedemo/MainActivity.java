package com.nilkamal.firebasedemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.nilkamal.MyUtil.Utilities.setFragment;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private LoginFragment mLoginFragment;
    private final int viewId = R.id.main_activity_frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mLoginFragment = new LoginFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            finish();
            startActivity(new Intent(this,HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
            Intent.FLAG_ACTIVITY_NO_ANIMATION));
            overridePendingTransition(0, 0);
        }else{
            setFragment(getSupportFragmentManager(),viewId,mLoginFragment,null);
        }
    }


//    private void setFragment(Fragment mFragment){
//        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
//        mFragmentTransaction.replace(R.id.main_activity_frame,mFragment);
//        mFragmentTransaction.commit();
//    }

}
