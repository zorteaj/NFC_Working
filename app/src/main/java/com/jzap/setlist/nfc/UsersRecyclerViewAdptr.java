package com.jzap.setlist.nfc;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by JZ_W541 on 4/3/2018.
 */

public class UsersRecyclerViewAdptr extends RecyclerView.Adapter<UsersRecyclerViewAdptr.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView userFirstName;
        public TextView userLastName;
        public TextView friendFlag;
        public CardView cardView;

        public ViewHolder(CardView cardView) {
            super(cardView);
            this.cardView = cardView;
            userFirstName = (TextView) cardView.findViewById(R.id.userFirstName);
            userLastName = (TextView) cardView.findViewById(R.id.userLastName);
            friendFlag = (TextView) cardView.findViewById(R.id.friendFlagTextView);
        }
    }

    HashMap<String, User> mUsers;
    List<User> mUsersList;
    User mThisUser;

    public UsersRecyclerViewAdptr(HashMap<String, User> users, Context context) {
        mUsers = users;
        mUsersList = new ArrayList<User>(mUsers.values());
        mThisUser = mUsers.get(SaveSharedPreference.getUserName(context));
        if(mThisUser == null) {
            Log.e(TAG, "Null user!"); // TODO: Throw exception?
        }
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
        holder.userFirstName.setText(mUsersList.get(position).getFirstName());
        holder.userLastName.setText(mUsersList.get(position).getLastName());
        if(mThisUser.getContacts().contains(mUsersList.get(position).getEmail())) {
            holder.friendFlag.setText("Friend");
        } else if(mThisUser.getEmail().equals(mUsersList.get(position).getEmail())) {
            holder.friendFlag.setText("Me");
        } else {
            holder.friendFlag.setText("NOT a friend");
        }
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactIntent = new Intent(view.getContext(), ContactActivity.class);
                contactIntent.putExtra("CONTACT_EMAIL", mUsersList.get(position).getEmail());
                view.getContext().startActivity(contactIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsersList.size();
    }
}
