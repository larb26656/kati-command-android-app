package com.example.errortime.kati_command;

import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyCustomFirebaseInstanceIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        // When token expired token will refresh to get new token.
    }
}