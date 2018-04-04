package com.jzap.setlist.nfc;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by JZ_W541 on 4/3/2018.
 */

public class UsersRecyclerViewAdptr extends RecyclerView.Adapter<UsersRecyclerViewAdptr.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView userFirstName;
        public TextView userLastName;
        public CardView cardView;

        public ViewHolder(CardView cardView) {
            super(cardView);
            this.cardView = cardView;
            userFirstName = (TextView) cardView.findViewById(R.id.userFirstName);
            userLastName = (TextView) cardView.findViewById(R.id.userLastName);
        }
    }

    List<User> mUsers;

    public UsersRecyclerViewAdptr(List<User> users) {
        mUsers = users;
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
        holder.userFirstName.setText(mUsers.get(position).getFirstName());
        holder.userLastName.setText(mUsers.get(position).getLastName());
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactIntent = new Intent(view.getContext(), ContactActivity.class);
                contactIntent.putExtra("CONTACT_NAME", mUsers.get(position).getFirstName());
                view.getContext().startActivity(contactIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}
