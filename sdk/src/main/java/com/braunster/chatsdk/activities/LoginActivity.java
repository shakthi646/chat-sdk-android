package com.braunster.chatsdk.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.braunster.chatsdk.R;
import com.braunster.chatsdk.Utils.Utils;
import com.braunster.chatsdk.interfaces.CompletionListener;
import com.braunster.chatsdk.interfaces.CompletionListenerWithDataAndError;
import com.braunster.chatsdk.network.BDefines;
import com.braunster.chatsdk.network.BFacebookManager;
import com.braunster.chatsdk.network.BNetworkManager;
import com.braunster.chatsdk.network.firebase.FirebasePaths;
import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;
import com.firebase.simplelogin.FirebaseSimpleLoginUser;
import com.firebase.simplelogin.enums.Provider;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by itzik on 6/8/2014.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static boolean DEBUG = true;

    private UiLifecycleHelper uiHelper;
    private LoginButton fbLoginButton;
    private Button btnLogin, btnReg, btnAnon;
    private EditText etName, etPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_sdk_activty_login);
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        initViews();

        // For registering the activity to facebook.
        String sha = Utils.getSHA(this, getPackageName());
        Log.i(TAG, "SHA: " + sha);
        Log.e(TAG, "INVALID: " + Provider.INVALID.ordinal()
                 + "FACEBOOK: " + Provider.FACEBOOK.ordinal()
                 + "TWITTER: " + Provider.TWITTER.ordinal()
                 + "PASSWORD: " + Provider.PASSWORD.ordinal()
                 + "GOOGLE: " + Provider.GOOGLE.ordinal()
                 + "ANONYMOUS: " + Provider.ANONYMOUS.ordinal());
    }

    private void initViews(){

        fbLoginButton = (LoginButton) findViewById(R.id.authButton);
        fbLoginButton.setOnErrorListener(new LoginButton.OnErrorListener() {
            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "error");
            }
        });
        fbLoginButton.setReadPermissions(Arrays.asList("email", "user_friends"));

        btnLogin = (Button) findViewById(R.id.chat_sdk_btn_login);
        btnAnon = (Button) findViewById(R.id.chat_sdk_btn_anon_login);
        btnReg = (Button) findViewById(R.id.chat_sdk_btn_register);
        etName = (EditText) findViewById(R.id.chat_sdk_et_mail);
        etPass = (EditText) findViewById(R.id.chat_sdk_et_password);
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Auto check for session state change if session is valid.
//        Session session = Session.getActiveSession();
//        if (session != null &&
//                (session.isOpened() || session.isClosed()) ) {
//            onSessionStateChange(session, session.getState(), null);
//        }

        /* Registering listeners.*/
        btnLogin.setOnClickListener(this);
        btnReg.setOnClickListener(this);
        btnAnon.setOnClickListener(this);

        uiHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private void onSessionStateChange(Session session, SessionState state, Exception exception){
        BFacebookManager.onSessionStateChange(session, state, exception, new CompletionListener() {
            @Override
            public void onDone() {
                if (DEBUG) Log.i(TAG, "FB and Firebase are connected");
                afterLogin();
            }

            @Override
            public void onDoneWithError() {
                if (DEBUG) Log.e(TAG, "Error connecting to FB or firebase");
                Toast.makeText(LoginActivity.this, "Failed connect to facebook.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(DEBUG) Log.v(TAG, "onActivityResult");
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    private void afterLogin(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
    }

    /* Exit Stuff*/
    @Override
    public void onBackPressed() {
        // Exit the app.
        // If logged out from the main activity pressing back in the LoginActivity will get me back to the Main so this have to be done.
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if (i == R.id.chat_sdk_btn_login) {

            if (!checkFields())
                return;

            Map<String, Object> data = FirebasePaths.getMap(new String[]{BDefines.Prefs.LoginTypeKey, BDefines.Prefs.LoginEmailKey, BDefines.Prefs.LoginPasswordKey },
                    BDefines.BAccountType.Password, etName.getText().toString(), etPass.getText().toString());
            BNetworkManager.sharedManager().getNetworkAdapter().authenticateWithMap(data, new CompletionListenerWithDataAndError<FirebaseSimpleLoginUser, Object>() {
                @Override
                public void onDone(FirebaseSimpleLoginUser firebaseSimpleLoginUser) {
                    afterLogin();
                }

                @Override
                public void onDoneWithError(FirebaseSimpleLoginUser firebaseSimpleLoginUser, Object o) {
                    Toast.makeText(LoginActivity.this, "Failed connect to Firebase.", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else if (i == R.id.chat_sdk_btn_anon_login) {

        }
        else if (i == R.id.chat_sdk_btn_register)
        {
            if (!checkFields())
                return;

            Map<String, Object> data = FirebasePaths.getMap(new String[]{BDefines.Prefs.LoginTypeKey, BDefines.Prefs.LoginEmailKey, BDefines.Prefs.LoginPasswordKey },
                    BDefines.BAccountType.Register, etName.getText().toString(), etPass.getText().toString());
            BNetworkManager.sharedManager().getNetworkAdapter().authenticateWithMap(data, new CompletionListenerWithDataAndError<FirebaseSimpleLoginUser, Object>() {
                @Override
                public void onDone(FirebaseSimpleLoginUser firebaseSimpleLoginUser) {
                    afterLogin();
                }

                @Override
                public void onDoneWithError(FirebaseSimpleLoginUser firebaseSimpleLoginUser, Object o) {
                    Toast.makeText(LoginActivity.this, "Failed connect to Firebase.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean checkFields(){
        if (etName.getText().toString().isEmpty())
        {
            Toast.makeText(LoginActivity.this, "Please enter username/email." , Toast.LENGTH_SHORT).show();
            return false;
        }

        if (etPass.getText().toString().isEmpty())
        {
            Toast.makeText(LoginActivity.this, "Please enter password." , Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}