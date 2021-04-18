package com.example.p2pchat.adapters;


import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.p2pchat.R;

/**
 * Originally meant to allow manual key generation.
 * If you want to fully implement this, you need to use cloud functions to retrieve the key within a chat
 */
public class KeyButton {

    private Activity activity;
    private ImageButton keyButton;

    public KeyButton(Activity activity){
        setActivity(activity);

        setKeyButton((ImageButton) this.activity.findViewById(R.id.keyButton));

        onPress();
    }

    private void onPress(){
        keyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage("Regenerate");
            }
        });
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setKeyButton(ImageButton keyButton) {
        this.keyButton = keyButton;
    }

    public ImageButton getKeyButton() {
        return keyButton;
    }
}
