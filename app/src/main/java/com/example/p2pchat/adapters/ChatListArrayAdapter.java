package com.example.p2pchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.p2pchat.R;
import com.example.p2pchat.objects.Chat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatListArrayAdapter extends ArrayAdapter<Chat> {
    private TextView usernameText;
    private TextView latestMessageText;
    private TextView dateText;
    private List<Chat> chatList = new ArrayList<Chat>();
    private Context context;

    @Override
    public void add(Chat object) {
        chatList.add(object);
        super.add(object);
    }

    public boolean contains(Chat object){
        if (chatList.contains(object)){
            return true;
        }
        return false;
    }
    
    public ChatListArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public int getCount() {
        return this.chatList.size();
    }

    public Chat getItem(int index) {
        return this.chatList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Chat ChatObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        row = inflater.inflate(R.layout.chat_preview, parent, false);

        //set text fields
        usernameText = (TextView) row.findViewById(R.id.usernameText);
        latestMessageText = (TextView) row.findViewById(R.id.latestMessageText);
        dateText = (TextView) row.findViewById(R.id.dateText);

        if (ChatObj.getLastUsername() == null) {
            usernameText.setText("Chat");
        } else {
            usernameText.setText(ChatObj.getLastUsername());
        }

        if (ChatObj.getLastMessage() == null) {
            latestMessageText.setText("Chatting");
        } else {
            latestMessageText.setText(ChatObj.getLastMessage());
        }

        if (ChatObj.getLastMessageTime() == null) {
            dateText.setText("Previous Date");
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date currentDateWithoutTime = sdf.parse(sdf.format(new Date()));
                Date chatDateWithoutTime = sdf.parse(sdf.format(ChatObj.getLastMessageTime()));

                if(currentDateWithoutTime.equals(chatDateWithoutTime)){
                    usernameText.setText("Today");
                }
                else{
                    usernameText.setText(chatDateWithoutTime.toString());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return row;
    }
}
