package com.example.uber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

public class SignUp extends AppCompatActivity {

    enum State{
        SIGNUP, LOGIN;
    }

    private State state;
    private MenuItem item;
    private Button btnSignUp, btnOneTimeLogin;
    private EditText edtSignUpUsername, edtSignUpPassword, edtSignUpP_D;
    private RadioButton radioPassenger, radioDriver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        state = State.SIGNUP;

        btnSignUp = findViewById(R.id.btnSignUp);
        btnOneTimeLogin = findViewById(R.id.btnOneTimeLogin);
        edtSignUpUsername = findViewById(R.id.edtSignUpUsername);
        edtSignUpPassword = findViewById(R.id.edtSignUpPassword);
        edtSignUpP_D = findViewById(R.id.edtSignUpP_D);
        radioPassenger = findViewById(R.id.radioPassenger);
        radioDriver = findViewById(R.id.radioDriver);

        if(ParseUser.getCurrentUser() != null){
//            ParseUser.logOut();
            transitionToPassengerActivity();
            transitionToDriverRequestListActivity();
        }

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(state == State.SIGNUP){
                    if(edtSignUpUsername.getText().toString().equals("") || edtSignUpPassword.getText().toString().equals("")) {
                        FancyToast.makeText(SignUp.this, "Fields can't be empty!", FancyToast.LENGTH_SHORT,
                                FancyToast.ERROR, true).show();
                        return;
                    }
                    if(!radioDriver.isChecked() && !radioPassenger.isChecked()){
                        FancyToast.makeText(SignUp.this, "Are you a driver or passenger?", FancyToast.LENGTH_SHORT,
                                FancyToast.ERROR, true).show();
                        return;
                    }

                    ParseUser appUser = new ParseUser();
                    appUser.setUsername(edtSignUpUsername.getText().toString());
                    appUser.setPassword(edtSignUpPassword.getText().toString());

                    if(radioDriver.isChecked()){
                        appUser.put("as", "Driver");
                    } else if (radioPassenger.isChecked()) {
                        appUser.put("as", "Passenger");
                    }

                    appUser.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                FancyToast.makeText(SignUp.this, edtSignUpUsername.getText().toString() +
                                                " SignedUp Successfully", FancyToast.LENGTH_SHORT,
                                        FancyToast.SUCCESS, true).show();
                                transitionToPassengerActivity();
                                transitionToDriverRequestListActivity();
                            }
                        }
                    });
                }
                else if (state == State.LOGIN) {

                        ParseUser.logInInBackground(edtSignUpUsername.getText().toString(), edtSignUpPassword.getText().toString(),
                                new LogInCallback() {
                                    @Override
                                    public void done(ParseUser user, ParseException e) {
                                        if (user != null && e == null) {
                                            FancyToast.makeText(SignUp.this, edtSignUpUsername.getText().toString() +
                                                            " LoggedIn Successfully", FancyToast.LENGTH_SHORT,
                                                    FancyToast.SUCCESS, true).show();
                                            transitionToPassengerActivity();
                                            transitionToDriverRequestListActivity();
                                        }
                                        else {
                                            FancyToast.makeText(SignUp.this,e.getMessage(), FancyToast.LENGTH_SHORT
                                                    , FancyToast.ERROR, true).show();
                                        }

                                    }
                                });
                }
            }
        });

        btnOneTimeLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtSignUpP_D.getText().toString().equals("")){
                    FancyToast.makeText(SignUp.this, "Are you a driver or passenger?", FancyToast.LENGTH_SHORT,
                            FancyToast.ERROR, true).show();
                    return;
                }
                if(edtSignUpP_D.getText().toString().equalsIgnoreCase("Driver") ||
                        edtSignUpP_D.getText().toString().equalsIgnoreCase("Passenger")){
                    if(ParseUser.getCurrentUser() == null){
                        ParseAnonymousUtils.logIn(new LogInCallback() {
                            @Override
                            public void done(ParseUser user, ParseException e) {
                                if(user != null && e == null){
                                    FancyToast.makeText(SignUp.this, "You Are Login Anonymously", FancyToast.LENGTH_SHORT,
                                            FancyToast.SUCCESS, true).show();
                                    user.put("as", edtSignUpP_D.getText().toString());

                                    user.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if(e == null){
                                                transitionToPassengerActivity();
                                                transitionToDriverRequestListActivity();
                                            }
                                        }
                                    });
                                }
                            }
                        });
                    }
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        item = menu.findItem(R.id.menuLogIn);
        SpannableString s = new SpannableString("LOGIN");
        s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), 0);
        item.setTitle(s);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(state == State.SIGNUP) {
            state = State.LOGIN;
            item.setTitle("SIGNUP");
            btnSignUp.setText("LOGIN");
        }
        else if (state == State.LOGIN){
            state = State.SIGNUP;
            item.setTitle("LOGIN");
            btnSignUp.setText("SIGNUP");
        }
        return super.onOptionsItemSelected(item);
    }

    private void transitionToPassengerActivity(){
        if(ParseUser.getCurrentUser() != null){
            if(ParseUser.getCurrentUser().get("as").equals("Passenger")){
                Intent intent = new Intent(SignUp.this, PassengerMapActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void transitionToDriverRequestListActivity(){
        if(ParseUser.getCurrentUser() != null){
            if(ParseUser.getCurrentUser().get("as").equals("Driver")){
                Intent intent = new Intent(this, DriverRequestListActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

}