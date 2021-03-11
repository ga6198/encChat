package com.example.p2pchat.adapters;

import android.app.Activity;
import android.media.Image;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.p2pchat.R;
import com.example.p2pchat.objects.User;
import com.example.p2pchat.utils.CryptoHelper;

import org.w3c.dom.Text;

public class ProfileView {
    private Activity activity;

    private User user; //pass in a user's information to display

    //profile components
    private ImageView profileImageView;
    private TextView usernameView;
    private TextView keyTextView;

    public ProfileView(final Activity activity, User user){
        setActivity(activity);

        setUser(user);

        setProfileImageView((ImageView)this.activity.findViewById(R.id.profileImageView));
        setKeyTextView((TextView)this.activity.findViewById(R.id.keyTextView));
        setUsernameView((TextView)this.activity.findViewById(R.id.usernameView));

        //initialize the information in the profile views
        initializeProfileFields();
    }

    private void initializeProfileFields(){
        //display username text
        if(user.getUsername() != null && !user.getUsername().equals("")) {
            usernameView.setText(user.getUsername());
        }

        //display public key text
        String encodedPublicKey = user.getEncodedPublicKey();
        if(encodedPublicKey != null && !encodedPublicKey.equals("")){
            keyTextView.setText(encodedPublicKey);
        }
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ImageView getProfileImageView() {
        return profileImageView;
    }

    public void setProfileImageView(ImageView profileImageView) {
        this.profileImageView = profileImageView;
    }

    public TextView getUsernameView() {
        return usernameView;
    }

    public void setKeyTextView(TextView keyTextView) {
        this.keyTextView = keyTextView;
    }

    public TextView getKeyTextView() {
        return keyTextView;
    }

    public void setUsernameView(TextView usernameView) {
        this.usernameView = usernameView;
    }
}
