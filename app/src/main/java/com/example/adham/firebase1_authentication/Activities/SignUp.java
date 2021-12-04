package com.example.adham.firebase1_authentication.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adham.firebase1_authentication.R;
import com.example.adham.firebase1_authentication.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {


    //Views
    EditText Email;
    EditText PASS;
    EditText PASS_CONFIRM;
    Button RegisterButton;
    public ProgressBar progressBar;
    TextView textView;
    EditText User_Name;
    EditText Phone_Number;


    public static final String DOMAIN_NAME ="doma.ca";
    public static final String TAG ="SignUp";

   // Firebase
    public FirebaseAuth firebaseAuth;
    public FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_sign_up );


        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = (ProgressBar)findViewById( R.id.progressBar ) ;
        User_Name = (EditText)findViewById( R.id.NameOfUser );
        Phone_Number = (EditText)findViewById( R.id.User_Phone );
        Email= (EditText)findViewById( R.id.email_signUp );
        PASS = (EditText)findViewById( R.id.pass_signUp );
        PASS_CONFIRM = (EditText)findViewById( R.id.pass_confirm_signUp );
        RegisterButton = (Button)findViewById( R.id.Register_Button );

        textView = (TextView)findViewById( R.id.AlreadyHas_Account );
        textView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( SignUp.this,SignIn.class );
                startActivity( intent );
            }
        } );

        RegisterButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RegisterNewEmail();


            }
        });




    }

    public void SetEmpty()
    {
        Email.setText( "" );
        PASS.setText( "" );
        PASS_CONFIRM.setText( "" );
    }
    // Matching between Password and Confirmation password
    // retrun true if the two are equals
    public boolean MatchingPasswords(String PASS, String Confirm_pass)
    {
        return PASS.equals( Confirm_pass );
    }

    //-------------------------------------(2) Create Account Mechanism ----------------------------------------
   // The user will create an account for the app by entering an email address and a password
    //into the two EditText fields and tapping on the Create Account button. Within the user interface layout, this button was configured
   // to call a method named createAccount() when clicked. Add this method to the main activity class file now so that it reads as follows:
    //Mechaism:
    //The method begins by obtaining the email address and password entered by the user.
    // When using the Firebase SDK to authenticate users,
    // and unlike the FirebaseUI Auth approach to authentication,
    // it is the responsibility of the app to verify that a valid
    // email address has been entered and that the password meets general security guidelines.
    // For the purposes of this example, however,
    // it will suffice to ensure something has been entered into the email address field and that the password selected by the user exceeds 5 characters.
    // If either of these requirements are not met,
    // the setError() method of the EditText class is called to prompt the user to correct the issue before the method returns.
    public void RegisterNewEmail() {
        String email = Email.getText().toString().trim();
        String Pass = PASS.getText().toString().trim();
        String Pass_confirm = PASS_CONFIRM.getText().toString().trim();

        if (email.isEmpty()) {
            Email.setError( "Email is Required" );
            Email.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher( email ).matches()) {
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
        if (!MatchingPasswords( Pass, Pass_confirm )) {
            PASS_CONFIRM.setError( "Password is not matching , try Again" );
            PASS_CONFIRM.requestFocus();
            return;
        }

        progressBar.setVisibility( View.VISIBLE );
        //1_
        //The user account creation process is then initiated via a call to the createUserWithEmailAndPassword() method of the FirebaseAuth instance,
        // passing through the email address and password string as arguments.
        //The account creation process is performed asynchronously,
        // requiring that a completion handler be provided to be called when the process completes. In this case,
        // the completion handler checks that the task was complete and displays a Toast message in the case of a failure.
        firebaseAuth.createUserWithEmailAndPassword( email, Pass ).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility( View.GONE );
                if (task.isSuccessful()) {
                    // send Verification Email
                    EmailVerification();
                    // Save User Information ----------------------------------------------------------------------
                    User user = new User(  );
                    user.setName( User_Name.getText().toString().trim() );
                    user.setPhone( Phone_Number.getText().toString().trim() );
                    user.setProfile_image( "" );
                    user.setSecurity_level( "1" );
                    user.setUser_id( FirebaseAuth.getInstance().getCurrentUser().getUid() );

                    FirebaseDatabase.getInstance().getReference().child( getString(R.string.User_nodes) )
                            .child( FirebaseAuth.getInstance().getCurrentUser().getUid() )
                            .setValue( user )
                            .addOnCompleteListener( new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            FirebaseAuth.getInstance().signOut();
                            // redirected to login screen to login
                            GoToSignIn();

                        }
                    } ).addOnFailureListener( new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            FirebaseAuth.getInstance().signOut();
                            // redirected to login screen to login
                            GoToSignIn();
                            Toast.makeText( SignUp.this, "SomeThing Wrong ,Try again", Toast.LENGTH_SHORT ).show();
                        }
                    } );

                    //---------------------------------------------------------------------------------------------------------------


                }
                // check if the email is already registered in database :
                else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    Email.setError( "This Email is already registered" );
                    Email.requestFocus();
                    return;
                } else {
                    Toast.makeText( SignUp.this, "Registeration Failed", Toast.LENGTH_SHORT ).show();
                }
            }
        } );
    }

    //------------------------------------- Email Verification ------------------------------------------------------
    //When using email and password authentication it usually makes sense to take some steps to verify that
    // the email address provided actually belongs to the user. The best way to do this is to send an email to the
    // provided address containing a verification link. Until the user has clicked the link,
    // access to the app can be denied even though an account has been created.
    //To instruct Firebase Authentication to send a verification email, simply call the sendEmailVerification() method of a FirebaseUser instance as follows
        public void EmailVerification()
        {
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if(firebaseUser!=null) {
                firebaseUser.sendEmailVerification().addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        Toast.makeText( SignUp.this, "Verification Sent", Toast.LENGTH_SHORT ).show();
                        else
                            Toast.makeText( SignUp.this, "Verification Not Sent", Toast.LENGTH_SHORT ).show();
                    }
                } );

            }


        }



    public void GoToSignIn()
    {

        Intent intent = new Intent(SignUp.this, SignIn.class);
        startActivity(intent);
        finish();
    }



}
