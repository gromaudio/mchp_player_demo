package com.gromaudio.simplifiedmediaplayer.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.widget.Toast;


public class BaseFragment extends Fragment {

    private Toast mToast;

    protected void setTitle(CharSequence title) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(title);
        }
    }

    public void onResumeFragment() {

    }

    public Context getContext() {
        return getActivity();
    }

    public void showToast(@NonNull CharSequence message) {
        if (mToast == null) {
            mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
        }
        mToast.setText(message);
        mToast.show();
    }
}
