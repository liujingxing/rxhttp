package com.example.httpsender.entity;

/**
 * User: ljx
 * Date: 2019-12-04
 * Time: 12:13
 */
public class User {

    private static User mUser;

    private String token;

    public static User get() {
        if (mUser == null) {
            synchronized (User.class) {
                if (mUser == null)
                    mUser = new User();
            }
        }
        return mUser;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
