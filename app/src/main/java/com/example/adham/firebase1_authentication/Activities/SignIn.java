package com.example.adham.firebase1_authentication.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adham.firebase1_authentication.Dialogs.ForgetPasswordDialog;
import com.example.adham.firebase1_authentication.R;
import com.example.adham.firebase1_authentication.Dialogs.ResendVerificationDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class SignIn extends AppCompatActivity {


    // Views
    EditText Email;
    EditText PASS;
    public TextView Register;
    public  Button SignIn;
    public  TextView ResednVerification;
    public ProgressBar progressBar;
    public TextView ForgetPassword;

    //Firebase
    public FirebaseAuth firebaseAuth;
    public FirebaseAuth.AuthStateListener authStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        firebaseAuth = FirebaseAuth.getInstance();
        Email = (EditText) findViewById( R.id.email_signIn );
        PASS = (EditText)findViewById( R.id.pass_signIn );
        progressBar = (ProgressBar)findViewById( R.id.progressBar2_Main);
        ForgetPassword = (TextView)findViewById( R.id.ForgertPass_text ) ;

        // resend verification

        ResednVerification = (TextView)findViewById( R.id.Resend_Verification_Email_text );
        ResednVerification.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResendVerificationDialog resendVerificationDialog = new ResendVerificationDialog();
                resendVerificationDialog.show( getSupportFragmentManager(),"dialog_fragment" );
            }
        } );


        // Setup
        SetUpAuthentication();


        // SignIn
        SignIn = (Button)findViewById( R.id.SignIn_Button );
        SignIn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogIn();
            }
        } );

        // Open SignUp Activity to Create Account and save firebase database
        Register = (TextView)findViewById( R.id.Register_text );
        Register.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( com.example.adham.firebase1_authentication.Activities.SignIn.this,SignUp.class );
                startActivity( intent );
            }
        } );


        // Password Reset if forgeted
        ForgetPasswordClick();


    }

    // forget password
    public void ForgetPasswordClick()
    {
        ForgetPassword.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ForgetPasswordDialog forgetPasswordDialog = new ForgetPasswordDialog();
                forgetPasswordDialog.show( getSupportFragmentManager(),"ResetPassword_id" );
            }
        } );
    }
    //-------------------------------------(3) SignIn Mechanism ----------------------------------------

    public void LogIn()
    {
        String  email = Email.getText().toString().trim();
        String Pass = PASS.getText().toString().trim();

        if (email.isEmpty()) {
            Email.setError( "Email is Required" );
            Email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Email.setError( "Enter Valid Email" );
            Email.requestFocus();
            return;
        }
        if (Pass.isEmpty()) {
            PASS.setError( "Password is Required" );
            PASS.requestFocus();
            return;
            }
        if (Pass.length() < 6) {
            PASS.setError( "Minimum lenght should be 6 character or numbers" );
            PASS.requestFocus();
            return;

        }
        progressBar.setVisibility( View.VISIBLE );

        //After performing some rudimentary validation on the email and password entered by the user,
        // email and password based authentication is then initiated by a call to the signInWithEmailAndPassword()
        // method of the FirebaseAuth instance, passing through the email address and password strings as arguments.
        // The sign-in process is performed asynchronously, requiring the addition of a completion handler to be called when the process is completed.
        // In the event that the sign-in failed, the user is notified via a Toast message.

        //User authentication using Firebase primarily consists of obtaining a reference to the FirebaseAuth instance or a FirebaseUser object
        // on which method calls are made to create accounts, sign-in users and perform account management tasks.
        // All of these methods perform the requested task asynchronously.
        // This essentially means that immediately after the method is called,
        // control is returned to the app while the task is performed in the background.
        // As is common with asynchronous tasks of this type, it is possible
        firebaseAuth.signInWithEmailAndPassword(email,Pass ).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                progressBar.setVisibility( View.GONE );
                if(task.isSuccessful())
                {
                    // When the user  attempts to sign into the app,
                    // code can be added to check that the user’s email address has been verified by calling the isEmailVerified()
                    // method of the FirebaseUser instance:
                    if (user.isEmailVerified()) {
                        OpenProfile();
                    }
                    else {
                        Toast.makeText( com.example.adham.firebase1_authentication.Activities.SignIn.this, "Check Your Email Inbox for a Verification link", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }

                }

            }
            // much the same way that a completion handler is called when an asynchronous task completes,
            // a failure handler is called when the task fails to complete due to a problem or error.
            // Failure listeners are added to Firebase authentication method calls via the addOnFailureListener() method
                                                          // Handle the Exceptions :
            // The type of exception thrown by the Firebase SDK in the event of an authentication failure can be used
            // to identify more information about the cause of the failure. In the previous section,
            // the built-in failure description was presented to the user,
            // but the app itself made no attempt to identify the cause of the failure.
            // While this may be acceptable for many situations, it is just as likely that the app will need more
            // information about what went wrong.
            //In the event of an authentication failure, the Firebase SDK will throw one the following types of exception:

            //•1) FirebaseAuthInvalidUserException – This exception indicates a problem with the email address entered by the user.
            // For example, the account does not exist or has been disabled in the Firebase console.

            //•2) FirebaseAuthInvalidCredentialsException – This exception is thrown when the password entered by the user does not match the email address.

            //•3) FirebaseAuthUserCollisionException – Thrown during account creation, this exception indicates a problem with the email address entered by the user.
            // The exact reason for the failure can be identified by accessing the error code.
            //
            //•4) FirebaseAuthWeakPasswordException – Indicates that the password specified during an account creation or password update operation is insufficiently strong.
            // A string describing the reason that the password is considered too weak can be obtain via a call to the getReason()
            // method of the exception object.

            //•5) FirebaseAuthRecentLoginRequiredException – The user has attempted to perform a security sensitive operation
            // but too much time has elapsed since signing in to the app. When this exception is
            // detected the user will need to be re-authenticated

            //With knowledge of the different exception types, the previous sample code can be modified to identify if the sign-in failure was due to an issue with the email or password entry. This can be achieved by finding out the type of the Exception object.
        } ).addOnFailureListener( new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText( SignIn.this, "Invalid password", Toast.LENGTH_SHORT ).show();
                } else if (e instanceof FirebaseAuthInvalidUserException) {
                   // Toast.makeText( SignIn.this, "Incorrect email address", Toast.LENGTH_SHORT ).show();
                  //  These error codes are provided in the form of string objects containing the error code name.
                    //Testing for an error, therefore, simply involves performing string comparisons against the error code.
                    String errorCode =((FirebaseAuthInvalidUserException) e).getErrorCode();
                    if (errorCode.equals("ERROR_USER_NOT_FOUND")) {
                        Toast.makeText( SignIn.this,  "No matching account found", Toast.LENGTH_SHORT ).show();
                    } else if (errorCode.equals("ERROR_USER_DISABLED")) {
                        Toast.makeText( SignIn.this, "User account has been disabled", Toast.LENGTH_SHORT ).show();
                    }
                } else {
                    Toast.makeText( SignIn.this, "Error", Toast.LENGTH_SHORT ).show();
                }
            }
        } );
        }
    public void SetUpAuthentication()
    {
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user!=null) {
                    if (user.isEmailVerified()) {
                        OpenProfile();
                    }
                    else {
                        Toast.makeText( com.example.adham.firebase1_authentication.Activities.SignIn.this, "Check Your Email Inbox for a Verification link", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                    }
                }
                else
                {
                    Log.v(  "NO AUTOHRIZATION","---------");
                }
            }
        };

    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener( authStateListener );

    }

    @Override
    protected void onStop() {
        super.onStop();
    if(authStateListener!=null)
    {
        FirebaseAuth.getInstance().removeAuthStateListener( authStateListener );
    }
    }
    public void OpenProfile()
    {
        Intent intent = new Intent( com.example.adham.firebase1_authentication.Activities.SignIn.this, Profile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
       // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();

    }
}
