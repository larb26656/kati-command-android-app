package com.example.errortime.kati_command;

/**
 * Created by Error Time on 10/23/2017.
 */

public class VisibilityManager {
    private static boolean mIsVisible = false;

    public static void setIsVisible(boolean visible) {
        mIsVisible = visible;
    }

    public static boolean getIsVisible() {
        return mIsVisible;
    }
}
