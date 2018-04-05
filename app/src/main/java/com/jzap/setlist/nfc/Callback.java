package com.jzap.setlist.nfc;

/**
 * Created by JZ_W541 on 4/5/2018.
 */

public interface Callback {
    public static final int USERS_UPDATED = 0;
    void call(int what, Object obj);
}