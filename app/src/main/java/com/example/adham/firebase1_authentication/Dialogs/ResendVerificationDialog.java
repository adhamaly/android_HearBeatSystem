package com.example.adham.firebase1_authentication.Dialogs;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ResendVerificationDialog extends DialogFragment {

    public View view;
    public TextView Resend;
    public TextView Cancel;
    public TextView Confirm;
    public EditText Email;
    public EditText Password;

    public AuthCredential authCredential;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate( R.layout.resend_verification_dialog,container,false );

        Resend = (TextView) view.findViewById(R.id.Resend_Verification_Email_text_dialog  );
        Email = (EditText)view.findViewById( R.id.confirm_email );
        Password = (EditText)view.findViewById( R.id.confirm_password );
        Cancel = (TextView)view.findViewById( R.id.dialogCancel );
        Confirm = (TextView)view.findViewById( R.id.dialogConfirm );

        Confirm.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Email.getText().toString().trim()!=""&&Password.getText().toString().trim()!="") {

                    Log.d( "Verification Sent","Ok!" );
                    ResendVerification( Email.getText().toString().trim(),Password.getText().toString().trim() );



                }else {
                    Toast.makeText( getContext(), "all fields must be filled out", Toast.LENGTH_SHORT ).show();

                }


            }
        } );

        Cancel.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        } );




        return view;
    }



    /**
     * sends an email verification link to the user
     */
    public void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                             //   Toast.makeText(getContext(), "Sent Verification Email", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(getContext(), "couldn't send email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }


    public void ResendVerification(String email,String password)
    {
        authCredential = EmailAuthProvider.getCredential(email,password);
        FirebaseAuth.getInstance().signInWithCredential( authCredential ).addOnCompleteListener( new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful())
                {
                    sendVerificationEmail();
                    FirebaseAuth.getInstance().signOut();
                    getDialog().dismiss();
                }

            }
        } ).addOnFailureListener( new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        } );

    }

}
