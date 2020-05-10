package edu.caltech.seva.activities.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.MainActivity;
import edu.caltech.seva.helpers.AWSLoginHandler;
import edu.caltech.seva.helpers.AWSLoginModel;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, AWSLoginHandler {

    AWSLoginModel awsLoginModel;
    private Button login, guest;
    private EditText editUsername, editPassword;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //instantiate awsLoginModel
        awsLoginModel = new AWSLoginModel(this, this);

        login = findViewById(R.id.loginButton);
        guest = findViewById(R.id.loginGuestButton);
        editUsername = findViewById(R.id.username);
        editPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        login.setOnClickListener(this);
        guest.setOnClickListener(this);
    }

    @Override
    public void onSignInSuccess() {
        LoginActivity.this.startActivity(new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    @Override
    public void onFailure(Exception exception) {
        exception.printStackTrace();
        progressBar.setVisibility(View.INVISIBLE);

        if (exception instanceof UserNotFoundException)
            Toast.makeText(LoginActivity.this, "User does not exist.", Toast.LENGTH_LONG).show();
        else if (exception instanceof NotAuthorizedException)
            Toast.makeText(LoginActivity.this, "Incorrect username or password", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(LoginActivity.this, "Unknown error. Check internet connection.", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginButton:
                userLogin();
                break;
            case R.id.loginGuestButton:
                awsLoginModel.signInGuest();
                break;
        }
    }

    private void userLogin() {
        final String username = editUsername.getText().toString().trim();
        final String password = editPassword.getText().toString().trim();

        if (username.isEmpty()) {
            editUsername.setError("Username is required");
            editUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            editPassword.setError("Password is required");
            editPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            editPassword.setError("Minimum length of a password should be 6");
            editPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        awsLoginModel.signInUser(username, password);
    }
}
