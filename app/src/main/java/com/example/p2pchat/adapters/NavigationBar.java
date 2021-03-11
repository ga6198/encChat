package com.example.p2pchat.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.p2pchat.R;
import com.example.p2pchat.activities.HomePageActivity;
import com.example.p2pchat.activities.ProfileActivity;
import com.example.p2pchat.objects.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationBar {
    private Activity activity;
    private BottomNavigationView bottomNavigationView;
    //private User userInfo; //user information to pass to next page

    public NavigationBar(final Activity activity){
        setActivity(activity);

        setBottomNavigationView((BottomNavigationView) this.activity.findViewById(R.id.bottomNavigationView));

        //TODO: write the intent switch code. Will need to pass in user info
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_chats:
                                Toast.makeText(activity, "Chats", Toast.LENGTH_SHORT).show();
                                openPage(bottomNavigationView.getContext(), HomePageActivity.class);
                                break;
                            case R.id.action_profile:
                                Toast.makeText(activity, "Profile", Toast.LENGTH_SHORT).show();
                                openPage(bottomNavigationView.getContext(), ProfileActivity.class);
                                break;
                        }
                        return true;
                    }
                }
        );
    }

    private void openPage(Context context, Class className){
        //get information from the current intent and pass it to the next
        Intent currentIntent = activity.getIntent();
        User currentUser = (User)currentIntent.getParcelableExtra("user");

        //start the new activity and pass and necessary parcelable information
        Intent newIntent = new Intent(context, className);
        newIntent.putExtra("user", currentUser);
        context.startActivity(newIntent);
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public BottomNavigationView getBottomNavigationView() {
        return bottomNavigationView;
    }

    public void setBottomNavigationView(BottomNavigationView bottomNavigationView) {
        this.bottomNavigationView = bottomNavigationView;
    }
}
