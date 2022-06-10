package ru.coolsoft.alphatrainer;

import android.app.Application;

/**
 * Created by Bobby© on 19.04.2015.
 * Alphatrainer application class
 */
public class TrainerApplication extends Application {
    private static TrainerApplication _app;

    @Override
    public void onCreate() {
        super.onCreate();
        _app = this;
    }

    public static TrainerApplication app(){
        return _app;
    }
}
