package edu.caltech.seva.activities.Login;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.StartupAuthResult;
import com.amazonaws.mobile.auth.core.StartupAuthResultHandler;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.NewPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.regions.Regions;

import edu.caltech.seva.R;
import edu.caltech.seva.activities.Main.MainActivity;
import edu.caltech.seva.helpers.AWSLoginHandler;
import edu.caltech.seva.helpers.AWSLoginModel;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, AWSLoginHandler {

    AWSLoginModel awsLoginModel;
    private Button login, help;
    private EditText editEmail, editPassword;
    private ProgressBar progressBar;

//    private static final String userPoolId = " us-east-1_ljOoCEH26"; //connects to SevaOperators userpool
//    private static final String clientId = "10q91tl5h3tv1l8pr7a4ngvtoj"; //connects userpool to the Seva app
//    private static final String identityPoolId = "us-east-1:c56fb4a5-f2c8-4bf6-bc11-bc91b0461b28"; //not currently used
//    private static final String clientSecret = "33p6njp8eme4igkej1bb3o5afj8cpic3agn6q5jnh6jk64nnhi6";
//    private static final Regions cognitoRegion = Regions.US_EAST_1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //instantiate awsLoginModel
        awsLoginModel = new AWSLoginModel(this, this);

        login = (Button) findViewById(R.id.loginButton);
        help = (Button) findViewById(R.id.loginHelpButton);
        editEmail = (EditText) findViewById(R.id.email);
        editPassword = (EditText) findViewById(R.id.password);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        login.setOnClickListener(this);
        help.setOnClickListener(this);
    }

    @Override
    public void onSignInSuccess() {
        LoginActivity.this.startActivity(new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        finish();
    }

    @Override
    public void onFailure(Exception exception) {
        exception.printStackTrace();
        Toast.makeText(LoginActivity.this, "Sign In: " + exception.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginButton:
                userLogin();
                break;
            case R.id.loginHelpButton:
                Toast.makeText(this, "Help Clicked...", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    private void userLogin() {
        final String email = editEmail.getText().toString().trim();
        final String password = editPassword.getText().toString().trim();

        if (email.isEmpty()) {
            editEmail.setError("Email is required");
            editEmail.requestFocus();
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
        awsLoginModel.signInUser(email, password);
    }
}
