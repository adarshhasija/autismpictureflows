package com.adarshhasija.flows;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;

/**
 * Created by adarshhasija on 4/25/15.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(this, "Qifc8RSSzfzBZQZZKv5cG6hFeIo2TlE7tD7MdUk7", "rQkj5tLXOC0Ynr3NR5R600TFOCcTmH5zW527qApC");
        ParseACL defaultACL = new ParseACL();
        //ParseACL.setDefaultACL(defaultACL, true);
    }
}
