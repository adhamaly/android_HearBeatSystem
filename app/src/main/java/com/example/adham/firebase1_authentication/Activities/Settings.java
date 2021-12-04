package com.example.adham.firebase1_authentication.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adham.firebase1_authentication.Dialogs.ChangeProfileImgDialog;
import com.example.adham.firebase1_authentication.R;
import com.example.adham.firebase1_authentication.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class Settings extends AppCompatActivity implements ChangeProfileImgDialog.OnPhotoReceivedListener {

    public CircleImageView profileImage;
    public TextView ChangePassword;
    private TextView DeleteAccount;
    private TextView changeEmail;
    public EditText Email;
    public  EditText Password;
    public  EditText UserName;
    public EditText PhoneNumber;
    public Button Save;
    public ProgressBar mprogressBar;
    public FirebaseAuth.AuthStateListener authStateListener;
    public FirebaseStorage firebaseStorageInstance = FirebaseStorage.getInstance();
    public StorageReference ProfileImagestorageReference;
    String ProfileImageFile;
    private static final int REQUEST_CODE = 1234;
    private static final double MB_THRESHHOLD = 5.0;
    private static final double MB = 1000000.0;
    private boolean mStoragePermissions;
    private Uri mSelectedImageUri;
    private Bitmap mSelectedImageBitmap;
    private byte[] mBytes;
    private double progress;

    // Handle profile Image for User
    @Override
    public void getImagePath(Uri imagePath) {
        if( !imagePath.toString().equals("")){
            mSelectedImageBitmap = null;
            mSelectedImageUri = imagePath;
            Log.d("", "getImagePath: got the image uri: " + mSelectedImageUri);
            ImageLoader.getInstance().displayImage(mSelectedImageUri.toString(), profileImage);
        }

    }

    @Override
    public void getImageBitmap(Bitmap bitmap) {
        if(bitmap != null){
            mSelectedImageUri = null;
            mSelectedImageBitmap = bitmap;
            Log.d("", "getImageBitmap: got the image bitmap: " + mSelectedImageBitmap);
            profileImage.setImageBitmap(mSelectedImageBitmap);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_settings );

        profileImage = (CircleImageView) findViewById( R.id.Profile_Image );
        UserName = (EditText)findViewById( R.id.setting_name );
        PhoneNumber = (EditText)findViewById( R.id.setting_phone );
        Email =  (EditText)findViewById( R.id.setting_email );
        Password = (EditText)findViewById( R.id.setting_password );
        Save = (Button)findViewById( R.id.save_setting_button );
        ChangePassword = (TextView)findViewById( R.id.change_password );
        DeleteAccount = (TextView)findViewById( R.id.deleteAccount );
        changeEmail =(TextView)findViewById( R.id.change_Email );
        // change email
        //changeEmail(changeEmail);
        // Delete the Account using delete() method..
      //  deletAccount( DeleteAccount );

        // Reference for the App itself
        StorageReference storageReference = firebaseStorageInstance.getReference();

        // Specific file for profile image
        ProfileImageFile = "profileimage/"+ UUID.randomUUID()+".PNG";

        // Reference for The file that contain profile images
        ProfileImagestorageReference  = firebaseStorageInstance.getReference(ProfileImageFile);

        verifyStoragePermissions();
        setupFirebaseAuth();
        setCurrentEmail();
        init();
        hideSoftKeyboard();





    }
    private void changeEmail(TextView changeEmail){
        String email = Email.getText().toString();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.updateEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Settings.this,
                                    "Email address updated",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener( new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText( Settings.this, "Update Failed,Try again ...", Toast.LENGTH_SHORT ).show();
            }
        } );

    }
    private void deletAccount(TextView deleteAccount){
        deleteAccount.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Settings.this,
                                            "Account deleted",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener( new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText( Settings.this, "Try Again..", Toast.LENGTH_SHORT ).show();
                    }
                } );

            }
        } );
    }

    private void init(){
        getUserAccountsData();

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("", "onClick: attempting to save settings.");

                //see if they changed the email
                if(!Email.getText().toString().trim().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())){
                    //make sure email and current password fields are filled
                    if(!isEmpty(Email.getText().toString().trim())
                            && !isEmpty(Password.getText().toString().trim())){

                        //verify that user is changing to a company email address
                        if(!Patterns.EMAIL_ADDRESS.matcher( Email.getText().toString() ).matches()){
                            editUserEmail();
                        }else{
                            Toast.makeText(Settings.this, "Invalid Domain", Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        Toast.makeText(Settings.this, "Email and Current Password Fields Must be Filled to Save", Toast.LENGTH_SHORT).show();
                    }
                }


                /*
                ------ METHOD 1 for changing database data (proper way in this scenario) -----
                 */
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                /*
                ------ Change Name -----
                 */
                if(!UserName.getText().toString().equals("")){
                    reference.child(getString(R.string.User_nodes))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(getString(R.string.name))
                            .setValue(UserName.getText().toString());
                }


                /*
                ------ Change Phone Number -----
                 */
                if(!PhoneNumber.getText().toString().equals("")){
                    reference.child(getString(R.string.User_nodes))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(getString(R.string.phone))
                            .setValue(PhoneNumber.getText().toString());
                }

                // Upload Image into Firebase Storage
                if(mSelectedImageUri != null){
                    uploadNewPhoto(mSelectedImageUri);
                }else if(mSelectedImageBitmap  != null){
                    uploadNewPhoto(mSelectedImageBitmap);
                }

                Toast.makeText(Settings.this, "saved", Toast.LENGTH_SHORT).show();
            }
        });

        ChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("======", "onClick: sending password reset link");

                /*
                ------ Reset Password Link -----
                */
                sendResetPasswordLink();
            }
        });

        // open the dialog to choose from camera or gallery
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mStoragePermissions){
                    ChangeProfileImgDialog dialog = new ChangeProfileImgDialog();
                    dialog.show(getSupportFragmentManager(), "profile_img_dialog");
                }else{
                    verifyStoragePermissions();
                }

            }
        });

    }

    /**
     * Uploads a new profile photo to Firebase Storage using a @param ***imageUri***
     * @param imageUri
     */
    public void uploadNewPhoto(Uri imageUri){
        /*
            upload a new profile photo to firebase storage
         */
        Log.v("==== from Gallery ", "uploadNewPhoto: uploading new profile photo to firebase storage.");

        //Only accept image sizes that are compressed to under 5MB. If thats not possible
        //then do not allow image to be uploaded
        BackgroundImageResize resize = new BackgroundImageResize(null);
        resize.execute(imageUri);
    }

    /**
     * Uploads a new profile photo to Firebase Storage using a @param ***imageBitmap***
     * @param imageBitmap
     */
    public void uploadNewPhoto(Bitmap imageBitmap){
        /*
            upload a new profile photo to firebase storage
         */
        Log.v("==== from Camera  ", "uploadNewPhoto: uploading new profile photo to firebase storage.");

        //Only accept image sizes that are compressed to under 5MB. If thats not possible
        //then do not allow image to be uploaded
        BackgroundImageResize resize = new BackgroundImageResize(imageBitmap);
        Uri uri = null;
        resize.execute(uri);
    }

    /**
     * 1) doinBackground takes an imageUri and returns the byte array after compression
     * 2) onPostExecute will print the % compression to the log once finished
     */
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {

        Bitmap mBitmap;
        public BackgroundImageResize(Bitmap bm) {
            if(bm != null){
                mBitmap = bm;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
         //   showDialog();
            Toast.makeText(Settings.this, "compressing image", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected byte[] doInBackground(Uri... params ) {
            Log.v("", "doInBackground: started.");

            if(mBitmap == null){

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(Settings.this.getContentResolver(), params[0]);
                    Log.v("", "doInBackground: bitmap size: megabytes: " + mBitmap.getByteCount()/MB + " MB");
                } catch (IOException e) {
                    Log.v("", "doInBackground: IOException: ", e.getCause());
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++){
                if(i == 10){
                    Toast.makeText(Settings.this, "That image is too large.", Toast.LENGTH_SHORT).show();
                    break;
                }
                bytes = getBytesFromBitmap(mBitmap,100/i);
                Log.v("", "doInBackground: megabytes: (" + (11-i) + "0%) "  + bytes.length/MB + " MB");
                if(bytes.length/MB  < MB_THRESHHOLD){
                    return bytes;
                }
            }
            return bytes;
        }


        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);

            mBytes = bytes;
            //execute the upload
            executeUploadTask();
        }
    }

    // convert from bitmap to byte array
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }

    private void executeUploadTask(){

    //    progressBar.setVisibility( View.VISIBLE );
        //specify where the photo will be stored
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("images/users" + "/" + FirebaseAuth.getInstance().getCurrentUser().getUid()
                        + "/profile_image"); //just replace the old image with the new one

        if(mBytes.length/MB < MB_THRESHHOLD) {
            //if the image size is valid then we can submit to database
            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(mBytes);
            //uploadTask = storageReference.putBytes(mBytes); //without metadata


            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Now insert the download url into the firebase database
                    Uri firebaseURL = taskSnapshot.getDownloadUrl();
                    Toast.makeText(Settings.this, "Upload Success", Toast.LENGTH_SHORT).show();
                    Log.v("", "onSuccess: firebase download url : " + firebaseURL.toString());
                    FirebaseDatabase.getInstance().getReference()
                            .child(getString(R.string.User_nodes))
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(getString(R.string.profile_image))
                            .setValue(firebaseURL.toString());

                   //progressBar.setVisibility( View.GONE );

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(Settings.this, "could not upload photo", Toast.LENGTH_SHORT).show();

                //    progressBar.setVisibility( View.GONE );

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double currentProgress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    if(currentProgress > (progress + 15)){
                        progress = (100 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        Log.v("", "onProgress: Upload is " + progress + "% done");
                        Toast.makeText(Settings.this, progress + "%", Toast.LENGTH_SHORT).show();
                    }

                }
            })
            ;
        }else{
            Toast.makeText(this, "Image is too Large", Toast.LENGTH_SHORT).show();
        }

    }






    private void getUserAccountsData(){
        Log.v("", "getUserAccountsData: getting the users account information");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        /*
            --------- Query method 1 ----------
         */
        Query query1 = reference.child(getString(R.string.User_nodes))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    User user = singleSnapshot.getValue(User.class);
                    Log.v("==============", "onDataChange: (QUERY METHOD 1) found user: " + user.toString());

                    UserName.setText(user.getName());
                    PhoneNumber.setText(user.getPhone());
                    ImageLoader.getInstance().displayImage( user.getProfile_image(),profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*
            --------- Query method 2 ----------
         */
      /*  Query query2 = reference.child(getString(R.string.User_nodes))
                .orderByKey()
                .equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                    User user = singleSnapshot.getValue(User.class);
                    Log.d(TAG, "onDataChange: (QUERY METHOD 2) found user: " + user.toString());

                    mName.setText(user.getName());
                    mPhone.setText(user.getPhone());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
*/
        Email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
    }

    /**
     * Generalized method for asking permission. Can pass any array of permissions
     */
    public void verifyStoragePermissions(){
        Log.v("", "verifyPermissions: asking user for permissions.");
        String[] permissions = {
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1] ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[2] ) == PackageManager.PERMISSION_GRANTED) {
            mStoragePermissions = true;
        } else {
            ActivityCompat.requestPermissions( Settings.this, permissions, REQUEST_CODE );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.v("", "onRequestPermissionsResult: requestCode: " + requestCode);
        switch(requestCode){
            case REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Log.v("", "onRequestPermissionsResult: User has allowed permission to access: " + permissions[0]);

                }
                break;
        }
    }
//-------------------------------------- Implementing the Password Reset Option -----------------------
    //When using FirebaseUI Auth, a password reset option was provided automatically as part of the authentication user interface flow.
// When using Firebase SDK authentication this feature has to be added manually.
// The user interface layout already contains a button titled Reset Password with the onClick property set to call a method named resetPassword().
// The last task in this phase of the project is to implement this method.
// The method will need to extract the email address entered by the user before passing that address as an argument to the sendPasswordResetEmail()
// method of the FirebaseAuth instance. A completion handler may also be specified to check that the email has been sent.
// Remaining in the PasswordAuthActivity.java file, add the following method:
    private void sendResetPasswordLink(){
        FirebaseAuth.getInstance().sendPasswordResetEmail(FirebaseAuth.getInstance().getCurrentUser().getEmail())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.v("", "onComplete: Password Reset Email sent.");
                            Toast.makeText(Settings.this, "Sent Password Reset Link to Email",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Log.d("", "onComplete: No user associated with that email.");

                            Toast.makeText(Settings.this, "No User Associated with that Email.",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                    // Clearly, given the fact that this is categorized as a secure action, some code needs to be added to identify
                    // when the recent login required exception is thrown.
                    //This, once again, involves the use of a failure listener to check for the type of exception:

                }).addOnFailureListener( new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                    //Toast.makeText( Settings.this, "Re-authentication needed", Toast.LENGTH_SHORT ).show();

                    /*
                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(firebaseUser.getEmail(),Password.getText().toString() );

                    firebaseUser.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // Re-authentication was successful
                                    // Re-attempt secure password update action
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText( Settings.this, "", Toast.LENGTH_SHORT ).show();
                        }
                    });

                    */
                }
                }
            }
         );

    }

    private void editUserEmail(){
        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.

     //   progressBar.setVisibility( View.VISIBLE );

        AuthCredential credential = EmailAuthProvider
                .getCredential(FirebaseAuth.getInstance().getCurrentUser().getEmail(), Password.getText().toString());
        Log.v("", "editUserEmail: reauthenticating with:  \n email " + FirebaseAuth.getInstance().getCurrentUser().getEmail()
                + " \n passowrd: " + Password.getText().toString());


        FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.v("", "onComplete: reauthenticate success.");

                            //make sure the domain is valid
                            if(!Patterns.EMAIL_ADDRESS.matcher( Email.getText().toString()).matches()){

                                ///////////////////now check to see if the email is not already present in the database
                                FirebaseAuth.getInstance().fetchProvidersForEmail(Email.getText().toString()).addOnCompleteListener(
                                        new OnCompleteListener<ProviderQueryResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<ProviderQueryResult> task) {

                                                if(task.isSuccessful()){
                                                    ///////// getProviders().size() will return size 1 if email ID is in use.

                                                    Log.v("", "onComplete: RESULT: " + task.getResult().getProviders().size());
                                                    if(task.getResult().getProviders().size() == 1){
                                                        Log.v("", "onComplete: That email is already in use.");
                                                        //progressBar.setVisibility( View.GONE );
                                                        Toast.makeText(Settings.this, "That email is already in use", Toast.LENGTH_SHORT).show();

                                                    }else{
                                                        Log.v("", "onComplete: That email is available.");

                                                        /////////////////////add new email
                                                        FirebaseAuth.getInstance().getCurrentUser().updateEmail(Email.getText().toString())
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            Log.v("", "onComplete: User email address updated.");
                                                                            Toast.makeText(Settings.this, "Updated email", Toast.LENGTH_SHORT).show();
                                                                            sendVerificationEmail();
                                                                            FirebaseAuth.getInstance().signOut();
                                                                        }else{
                                                                            Log.v("", "onComplete: Could not update email.");
                                                                            Toast.makeText(Settings.this, "unable to update email", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                     //   progressBar.setVisibility( View.GONE );
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                    //    progressBar.setVisibility( View.GONE );

                                                                        Toast.makeText(Settings.this, "unable to update email", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });


                                                    }

                                                }
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                               // progressBar.setVisibility( View.GONE );
                                                Toast.makeText(Settings.this, "unable to update email", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }else{
                                Toast.makeText(Settings.this, "you must use a company email", Toast.LENGTH_SHORT).show();
                            }

                        }else{
                            Log.v("", "onComplete: Incorrect Password");
                            Toast.makeText(Settings.this, "Incorrect Password", Toast.LENGTH_SHORT).show();
                         //   progressBar.setVisibility( View.GONE );
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                     //   progressBar.setVisibility( View.GONE );
                        Toast.makeText(Settings.this, "“unable to update email”", Toast.LENGTH_SHORT).show();
                    }
                });
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
                                Toast.makeText(Settings.this, "Sent Verification Email", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(Settings.this, "Couldn't Verification Send Email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

    }

    private void setCurrentEmail(){
        Log.v("", "setCurrentEmail: setting current email to EditText field");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null){
            Log.v("", "setCurrentEmail: user is NOT null.");

            String email = user.getEmail();

            Log.v("", "setCurrentEmail: got the email: " + email);

            Email.setText(email);
        }
    }






    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * Return true if the @param is null
     * @param string
     * @return
     */
    private boolean isEmpty(String string){
        return string.equals("");
    }



            // Check Authentication State --------------------------------------------------------------------

    private void checkAuthenticationState(){
        Log.d("", "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            Log.d("", "checkAuthenticationState: user is null, navigating back to login screen.");

            Intent intent = new Intent(Settings.this, SignIn.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }else{
            Log.d("", "checkAuthenticationState: user is authenticated.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationState();
    }


    //-----------------------------------------------------------------------------------------------------------------------

    /*
            ----------------------------- Firebase setup ---------------------------------
         */
    private void setupFirebaseAuth(){
        Log.d("", "setupFirebaseAuth: started.");

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("", "onAuthStateChanged:signed_in:" + user.getUid());
                    //toastMessage("Successfully signed in with: " + user.getEmail());


                } else {
                    // User is signed out
                    Log.d("", "onAuthStateChanged:signed_out");
                    Toast.makeText(Settings.this, "Signed out", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Settings.this, SignIn.class);
                    startActivity(intent);
                    finish();
                }
                // ...
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(authStateListener);
        }
    }

    //--------------------------------------------------------------------------------------------------------------



    public void ResendPasswordLink()
    {
        FirebaseAuth.getInstance().sendPasswordResetEmail( FirebaseAuth.getInstance().getCurrentUser().getEmail() )
                .addOnCompleteListener( new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText( Settings.this, "Sent Password Reset Link to Email", Toast.LENGTH_SHORT ).show();
                        }
                        else {
                            Toast.makeText( Settings.this, "No user associated with this Email", Toast.LENGTH_SHORT ).show();
                        }
                    }

                } );

    }

}