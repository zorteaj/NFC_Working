package com.jzap.setlist.nfc;

import java.util.HashMap;

/**
 * Created by JZ_W541 on 4/5/2018.
 */

public class FirebaseDBUsersCallback implements Callback {
    HashMap<Integer, User> users;
    @Override
    public void call(int what, Object obj) {
        users = (HashMap<Integer, User>) obj;
    }
}
