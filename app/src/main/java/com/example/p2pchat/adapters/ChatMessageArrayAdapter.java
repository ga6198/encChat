package com.example.p2pchat.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.p2pchat.R;
import com.example.p2pchat.objects.ChatMessage;
import com.example.p2pchat.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatMessageArrayAdapter extends ArrayAdapter<ChatMessage> {

    private TextView chatText;
    private TextView dateText;
    private List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
    private Context context;

    @Override
    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public boolean contains(ChatMessage object){
        if (chatMessageList.contains(object)){
            return true;
        }
        return false;
    }

    public ChatMessageArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessageObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (chatMessageObj.getOwnMessage()) {
            row = inflater.inflate(R.layout.my_message, parent, false);
        } else {
            row = inflater.inflate(R.layout.their_message, parent, false);
        }

        //set the chat text
        chatText = (TextView) row.findViewById(R.id.message_body);
        chatText.setText(chatMessageObj.getMessage());

        //set the date text
        String dateString = DateUtils.dateStringWithTime(chatMessageObj.getTime());
        dateText = (TextView) row.findViewById(R.id.date_text);
        dateText.setText(dateString);

        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView date = (TextView) v.findViewById(R.id.date_text);
                //If the date is visible, make it nonvisible
                if(date.getVisibility() == View.VISIBLE){
                    date.setVisibility(View.GONE);
                }
                //if the date is not visible, make it visible
                else{
                    date.setVisibility(View.VISIBLE);
                }
            }
        });

        return row;
    }
}
