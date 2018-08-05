package com.nilkamal.MyUtil;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Author : nilkamal,
 * Creation Date: 29/7/18.
 */
public class Utilities {


    public static boolean hasInternetConnection(Context context){
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return (connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null && connManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected())
                || (connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null &&
                connManager
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI)
                        .isConnected());
    }


    public static void setFragment(FragmentManager fragmentManager,int viewId,Fragment mFragment,Bundle args) {
//        fragmentManager.popBackStack();
        FragmentTransaction mFragmentTransaction = fragmentManager.beginTransaction();
        if(args!=null){
            mFragment.setArguments(args);
        }
        mFragmentTransaction.replace(viewId, mFragment);
        mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction.commit();
    }

}
