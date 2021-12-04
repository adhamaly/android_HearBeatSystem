package com.example.adham.firebase1_authentication.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adham.firebase1_authentication.R;
import com.example.adham.firebase1_authentication.UniversalImageLoader;
import com.example.adham.firebase1_authentication.models.User;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.lang.Boolean.TRUE;


public class Profile extends AppCompatActivity implements SensorEventListener {


    private static final int CHOOSE_IMAGE =101 ;
    TextView textView;
    public ProgressBar progressBar;
    FirebaseAuth.AuthStateListener mAuthListener;
    ImageView imageView;
    BluetoothSocket mSocket;
    SmsManager sms;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mDevice;
    DataTransferThread dataTransferThread;
    //static final UUID mUUID = UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" );
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "myprefs";
    public static final String value = "key";
    String longKey = "longKey",latKey = "latKey";
    SharedPreferences.Editor editor;
    Intent intent;
    PendingIntent pi;
    SmsTransfer smsTransfer;
    Button button;
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;
    double latitude,longitude;
    TextView stepCounts;
    SensorManager sensorManager;
    boolean running= false;
    CircleImageView profileImage;
    boolean StartRead = false;



    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_profile );

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText( this, "Device does not support Bluetooth", Toast.LENGTH_SHORT ).show();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }


        setupFirebaseAuth();

        initImageLoader();
        getUserAccountsData();


        sharedpreferences = getSharedPreferences( MyPREFERENCES, Context.MODE_PRIVATE );
        textView = findViewById( R.id.BPM );
        stepCounts = findViewById( R.id.counts );
        profileImage = findViewById( R.id.Profile_Image );


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE );





        Button button = findViewById( R.id.maps );
        button.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( Profile.this, ItemList.class);
                startActivity( intent );

            }
        } );
        imageView = findViewById( R.id.current_location_icon );
        imageView.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("geo:"+sharedpreferences.getString( latKey,null )+sharedpreferences.getString( longKey,null ));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);

            }
        } );

        Button checkButton = findViewById( R.id.check );
        checkButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent( Profile.this, CheckActivity.class);
                startActivity( intent );

            }
        } );



        ConnectThread mConnectThread = new ConnectThread();
        mConnectThread.start();

        if(sharedpreferences.getBoolean( "status",false ) == TRUE) {
            // data transfer thread ..
            dataTransferThread = new DataTransferThread( mSocket );
            new Thread( dataTransferThread ).start();
            // sms transfer thread ...
            smsTransfer = new SmsTransfer();
            new Thread( smsTransfer ).start();

        }
        // handling location ...
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient( this );
        getLastLocation();


    }
    private void initImageLoader(){
        UniversalImageLoader imageLoader = new UniversalImageLoader(Profile.this);
        ImageLoader.getInstance().init(imageLoader.getConfig());
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


                    ImageLoader.getInstance().displayImage( user.getProfile_image(),profileImage);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }




    // check if there is a user currently
    private void checkAuthenticationState(){
        Log.d("", "checkAuthenticationState: checking authentication state.");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){

            Log.d("", "checkAuthenticationState: user is null, navigating back to login screen.");

            // لو مفيش مستخدم اخرج من الصفحة ديه و احذفها من stack
            Intent intent = new Intent(Profile.this, SignIn.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
         //   intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
            startActivity(intent);
            finish();
        }else{
            Log.d("", "checkAuthenticationState: user is authenticated.");
        }
    }
    /*
           -----------------------------(1) Firebase setup ---------------------------------
        */
    // First Step is handle the AuthenticationState:
    // second step is add and remove this listener into FirebaseAuth reference
    // Without this vital step, the method will never be called in response to changes in the authentication state:

    // first step:
    //--------------------------------
    // process of setup the authentication of user by AuthStateListener reference that store the current state of user Authentication
    // this prcoess is called each time user open this activity and if change occur will handled in onAuthStateChanged() method
    //1_
    // check if user is signed in then dispaly id
    // else .. return back to the mainActivity to sign in or sign Up
    // meaning of else that user 1_signout , 2_email is removed
    private void setupFirebaseAuth(){
        Log.d("", "setupFirebaseAuth: started.");

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {

                    Log.d("", "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    Log.d("", "onAuthStateChanged:signed_out");
                    Intent intent = new Intent(Profile.this, SignIn.class);
                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //    intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
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
                   // Add the StateListener into FirebaseAuth
        //For the purposes of this example, the standard onStart()
        // Android lifecycle method will be overridden and used to add the listener.
        // The onStart() method is called immediately after the call to the onCreate()
        // method and before the activity is to become visible to the user.
        // Within the FirebaseAuthActivity.java file, add this method so that it reads as follows:
        FirebaseAuth.getInstance().addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
                                // remove the StateListener From FirebaseAuth
        //In addition to adding the listener, it is equally important to remove the listener when it is no longer needed.
        // To achieve this, the onStop() method will be used:
        if (mAuthListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthListener);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       getMenuInflater().inflate( R.menu.profile_setting,menu );
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            //------------------------------------------------ Sign Out ----------------------------------
            case R.id.signOut_item:
                FirebaseAuth.getInstance().signOut();
                return true;
            case R.id.account_setting:
                Intent intent = new Intent(Profile.this, Settings.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);


        }
    }



     class ConnectThread extends Thread {

         private final BluetoothSocket mmSocket;
         private final BluetoothDevice mmDevice;
         final UUID mUUID = UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" );
         BluetoothDevice hc05 = mBluetoothAdapter.getRemoteDevice( "98:D3:71:FD:7E:19" );

         @SuppressLint("MissingPermission")
         public ConnectThread( ) {
             BluetoothSocket tmp = null;
             mmDevice = hc05;
             try {
                 tmp = hc05.createRfcommSocketToServiceRecord( mUUID );
             } catch (IOException e) {

             }
             mmSocket = tmp;
         }

         @SuppressLint("MissingPermission")
         public void run() {
             mBluetoothAdapter.cancelDiscovery();
             try {
                 mmSocket.connect();
                 editor.putBoolean( "status",true );
             } catch (IOException connectException) {


                 }
             }
         }



    public class DataTransferThread implements Runnable {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        int bpm;


        public DataTransferThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
            }
            mmInStream = tmpIn;
        }

        public void run() {
            Handler handler = new Handler( Looper.getMainLooper() );
            editor = sharedpreferences.edit();
            while (true) {

                try {
                    mmInStream.skip( mmInStream.available() );
                    bpm = mmInStream.read();
                    editor.putInt( value, bpm );
                    editor.commit();
                    handler.post( new Runnable() {
                        @Override
                        public void run() {
                            textView.setText( String.valueOf( bpm ) );
                            System.out.println( (int) bpm );

                        }
                    } );


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
    }


    public class SmsTransfer implements Runnable {

        @Override
        public void run() {
            while (true) {
                intent = new Intent( getApplicationContext(), Profile.class );
                pi = PendingIntent.getActivity( getApplicationContext(), 0, intent, 0 );

                //Get the SmsManager instance and call the sendTextMessage method to send message
                sms = SmsManager.getDefault();
                if (sharedpreferences.getInt( value, -1 ) > 185 && sharedpreferences.getInt( value, -1 ) < 195) {
                    sms.sendTextMessage( "01093338669", null, "help me dad iam in "+sharedpreferences.getString( latKey,null )+sharedpreferences.getString( longKey,null ), null, null );
                    System.out.println( "message sent ....." );
                }

                try {
                    Thread.sleep( 10000 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation () {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();
                                } else {
                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    editor.putString( longKey,String.valueOf( longitude ) );
                                    editor.putString( latKey,String.valueOf( latitude ));
                                    editor.commit();
                                }
                            }
                        }
                );
            } else {
                Toast.makeText( this, "Turn on location", Toast.LENGTH_LONG ).show();
                Intent intent = new Intent( android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS );
                startActivity( intent );
            }
        } else {
            requestPermissions();
        }
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData () {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY );
        mLocationRequest.setInterval( 0 );
        mLocationRequest.setFastestInterval( 0 );
        mLocationRequest.setNumUpdates( 1 );

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient( this );
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );

    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
        }
    };



    private boolean checkPermissions () {
        if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED )

        {
            return true;

        }
        return false;
    }


    private void requestPermissions () {
        ActivityCompat.requestPermissions( this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_ID );
        ActivityCompat.requestPermissions( Profile.this, new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS}, PackageManager.PERMISSION_GRANTED );

    }

    private boolean isLocationEnabled () {
        LocationManager locationManager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        return locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @Override
    public void onRequestPermissionsResult ( int requestCode, String[] permissions,
                                             int[] grantResults){
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {



            stepCounts.setText( String.valueOf( event.values[0]));

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume () {
        super.onResume();
        checkAuthenticationState();

        if (checkPermissions()) {
            getLastLocation();
        }
        running  = true;
        Sensor countsensor = sensorManager.getDefaultSensor( Sensor.TYPE_STEP_COUNTER );
        if(countsensor !=null){

            sensorManager.registerListener( this,countsensor,SensorManager.SENSOR_DELAY_UI );

        }
        else{
            Toast.makeText( this, "sensor not found..", Toast.LENGTH_SHORT ).show();

        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        // running = false;

    }
}


