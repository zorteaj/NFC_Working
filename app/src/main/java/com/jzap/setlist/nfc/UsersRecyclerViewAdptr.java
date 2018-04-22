package com.jzap.setlist.nfc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by JZ_W541 on 4/3/2018.
 */

public class UsersRecyclerViewAdptr extends RecyclerView.Adapter<UsersRecyclerViewAdptr.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView userName;
        public TextView friendFlag;
        public CardView cardView;

        public ViewHolder(CardView cardView) {
            super(cardView);
            this.cardView = cardView;
            userName = (TextView) cardView.findViewById(R.id.userName);
            friendFlag = (TextView) cardView.findViewById(R.id.friendFlagTextView);
        }
    }

    private static final String TAG = Config.TAG_HEADER + "UsrRcyclrVApr";

    HashMap<String, User> mUsers;
    List<User> mUsersList;
    User mActiveUser;

    public UsersRecyclerViewAdptr(HashMap<String, User> users, Context context) {
        mUsers = users;
        mActiveUser = ActiveUser.getActiveUser(context, mUsers);
        mUsersList = new ArrayList<>(mUsers.values());
    }

    @Override
    public UsersRecyclerViewAdptr.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        CardView cardView = (CardView) inflater.inflate(R.layout.layout_users, parent, false);
        UsersRecyclerViewAdptr.ViewHolder vh = new UsersRecyclerViewAdptr.ViewHolder(cardView);
        return vh;
    }

    @Override
    public void onBindViewHolder(UsersRecyclerViewAdptr.ViewHolder holder, final int position) {
        holder.userName.setText(mUsersList.get(position).getUserName());
        if(mActiveUser.getContacts().contains(mUsersList.get(position).getCleanEmail())) {
            holder.friendFlag.setText("Friend");
            holder.friendFlag.setTextColor(Color.BLUE);
        } else if(mActiveUser.getCleanEmail().equals(mUsersList.get(position).getCleanEmail())) {
            holder.friendFlag.setText("Me");
            holder.friendFlag.setTextColor(Color.BLUE);
        } else {
            holder.friendFlag.setText("NOT a friend");
            holder.friendFlag.setTextColor(Color.GRAY);
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactIntent = new Intent(view.getContext(), ContactActivity.class);
                contactIntent.putExtra("CONTACT_KEY", mUsersList.get(position).getCleanEmail());
                view.getContext().startActivity(contactIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsersList.size();
    }
}
