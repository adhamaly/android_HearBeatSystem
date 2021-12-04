package com.example.adham.firebase1_authentication.Dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adham.firebase1_authentication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordDialog extends DialogFragment {


    private static final String TAG = "PasswordResetDialog";

    //widgets
    private EditText mEmail;
    TextView confirmDialog;

    //vars
    private Context mContext;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate( R.layout.forget_password_dialog, container, false);

        mEmail = (EditText) view.findViewById(R.id.email_password_reset);
        mContext = getActivity();
         confirmDialog = (TextView) view.findViewById(R.id.dialogConfirm);

         ConfirmClickable();


        return view;
    }

    public void ConfirmClickable()
    {
        confirmDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mEmail.getText().toString().trim().equals( "" )){
                    Log.v(TAG, "onClick: attempting to send reset link to: " + mEmail.getText().toString());
                    sendPasswordResetEmail(mEmail.getText().toString().trim());
                    getDialog().dismiss();
                }

            }
        });

    }
    /**
     * Send a password reset link to the email provided
     * @param email
     */
    //-------------------------------------- Implementing the Password Reset Option -----------------------
    //When using FirebaseUI Auth, a password reset option was provided automatically as part of the authentication user interface flow.
// When using Firebase SDK authentication this feature has to be added manually.
// The user interface layout already contains a button titled Reset Password with the onClick property set to call a method named resetPassword().
// The last task in this phase of the project is to implement this method.
// The method will need to extract the email address entered by the user before passing that address as an argument to the sendPasswordResetEmail()
// method of the FirebaseAuth instance. A completion handler may also be specified to check that the email has been sent.
// Remaining in the PasswordAuthActivity.java file, add the following method:
    public void sendPasswordResetEmail(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail( email )
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.v( TAG, "onComplete: Password Reset Email sent." );
                            Toast.makeText( mContext, "Password Reset Link Sent to Email",
                                    Toast.LENGTH_SHORT ).show();
                        } else {
                            Log.d( TAG, "onComplete: No user associated with that email." );
                            Toast.makeText( mContext, "No User is Associated with that Email",
                                    Toast.LENGTH_SHORT ).show();

                        }
                    }
                } );
    }
}
