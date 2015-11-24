package com.adarshhasija.flows;

import android.app.Activity;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignupActivity extends ActionBarActivity {

    private EditText editTextFirstName;
    private EditText editTextLastName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPasswordConfirm;
    private Button buttonSignup;

    private boolean isValidEmail(String target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextFirstName = (EditText) findViewById(R.id.edit_text_first_name);
        editTextLastName = (EditText) findViewById(R.id.edit_text_last_name);
        editTextEmail = (EditText) findViewById(R.id.edit_text_email);
        editTextPassword = (EditText) findViewById(R.id.edit_text_password);
        editTextPasswordConfirm = (EditText) findViewById(R.id.edit_text_password_confirm);
        buttonSignup = (Button) findViewById(R.id.signup_button);
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = editTextFirstName.getText().toString();
                String lastName = editTextLastName.getText().toString();
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                String passwordConfirm = editTextPasswordConfirm.getText().toString();

                ConnectivityManager cm = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
                if(cm.getActiveNetworkInfo() == null) {
                    Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (firstName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "You have not entered a first name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (lastName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "You have not entered a last name", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean emailValid = isValidEmail(email);
                if (!emailValid) {
                    Toast.makeText(getApplicationContext(), "You have not entered an email, or it is invalid", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "You have not entered a password", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!passwordConfirm.equals(password)) {
                    Toast.makeText(getApplicationContext(), "The password you retyped does not match the original password", Toast.LENGTH_SHORT).show();
                    return;
                }

                firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1);
                lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1);
                ParseUser user = new ParseUser();
                user.setUsername(email);
                user.setPassword(password);
                user.setEmail(email);
                user.put("firstName", firstName);
                user.put("lastName", lastName);
                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_OK, null);
                        finish();
                    }
                });

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_signup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
