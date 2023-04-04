package com.example.uber;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("5PIVOC2O8GLcZF3gAbF7Bx6DYld9rynXZ7w3ctLR")
                // if defined
                .clientKey("6ufxNEcwhrcYHgxDLZSDAWlmAEbT06TP4gzJ6Z5V")
                .server("https://parseapi.back4app.com/")
                .build()
        );

    }
}
