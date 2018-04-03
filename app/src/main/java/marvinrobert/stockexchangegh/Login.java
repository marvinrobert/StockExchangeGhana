package marvinrobert.stockexchangegh;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private EditText emailEt;
    private EditText passwordEt;
    private EditText passwordTextInput;
    private Button loginBtn;
    private CheckBox rememberMeCb;
    private TextView signUpTv;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    boolean rememberMe = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(Login.this, FirstActivity.class));
            finish();
        }

        setContentView(R.layout.activity_login);
        initializeUi();

    }


    public void initializeUi(){
        emailEt = findViewById(R.id.emailField);
        passwordEt = findViewById(R.id.passwordField);
//        passwordTextInput = findViewById(R.id.passwordFieldTextInputLayout);
        loginBtn = findViewById(R.id.login);
        rememberMeCb = findViewById(R.id.rememberUser);
        signUpTv = findViewById(R.id.signUp);
        progressBar = findViewById(R.id.progressBar);

        signUpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSignUp();
            }
        });

        rememberMeCb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRememberMe(v);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEt.getText().toString();
                final String password = passwordEt.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    // there was an error
                                    passwordEt.setError("Password Invalid");
                                }
                                else {
                                    Intent intent = new Intent(Login.this, FirstActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });

    }

    public void doSignUp(){
        Intent intent = new Intent(this,SignUp.class);
        startActivity(intent);
    }


    public void setRememberMe(View v){
        CheckBox checkBox = (CheckBox)v;
        if(checkBox.isChecked()){
            rememberMe = true;
        }

        if(!checkBox.isChecked()){
           rememberMe = false;
        }
    }

    public static boolean validate(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX .matcher(emailStr);
        return matcher.find();
    }
}
