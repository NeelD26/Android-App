package com.example.Location_Alert.SOS;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;
public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener,GestureDetector.OnDoubleTapListener
{
    Button btnShowLocation;private static final int REQUEST_CODE_PERMISSION = 2;
    int position = 0;String mPermission = Manifest.permission.ACCESS_FINE_LOCATION,msg;
    GPSTracker gps;// GPSTracker class
    String[] phoneNo,ph = {"ph1","ph2","ph3","ph4","ph5"};
    SharedPreferences sp;int flag = 0;int p;
    EditText et;GestureDetector gestureDetector;
    @SuppressLint("ResourceType") @Override
    public void onCreate(Bundle savedInstanceState)
    {
        sp = MainActivity.this.getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        p = sp.getInt("Light",3);
        Intent theme = getIntent();
        if(theme.hasExtra("theme"))
            p = theme.getIntExtra("theme",3);
        if(p == 3)
        {
            editor.putInt("Light",3);
            setTheme(R.style.Theme_AppCompat_Light_DarkActionBar);
            editor.apply();
        }
        else if(p == 4)
        {
            editor.putInt("Light",4);
            setTheme(R.style.AppTheme);
            editor.apply();
        }
        super.onCreate(savedInstanceState);setContentView(R.layout.activity_main);
        gestureDetector = new GestureDetector(MainActivity.this, MainActivity.this);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>()
        {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse)
            {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });
        task.addOnFailureListener(this, new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                if (e instanceof ResolvableApiException)
                {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try
                    {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,100);
                    }
                    catch (IntentSender.SendIntentException sendEx)
                    {
                        // Ignore the error.
                    }
                }
            }
        });
        et = findViewById(R.id.et);
        //getting the previous selected contacts
        phoneNo = new String[sp.getInt("length",0)];
        int i = 0;
        while (i < phoneNo.length)
        {
            phoneNo[i] = sp.getString(ph[i], null);
            //Toast.makeText(MainActivity.this, "" + phoneNo[i], Toast.LENGTH_LONG).show();
            i++;
        }
        position = sp.getInt("pos",0);
        Intent pos = getIntent();
        if(pos.hasExtra("contact1"))
        {
            phoneNo = pos.getStringArrayExtra("contact1");
            //editor.clear();
            if(phoneNo[0]!=null)
                editor.putString(ph[0], PhoneNumberUtils.stripSeparators(phoneNo[0].split("\n")[1]));
            //Toast.makeText(MainActivity.this, "" + ph[0], Toast.LENGTH_LONG).show();
            int j = 1;
            while(j < phoneNo.length && phoneNo[j]!=null)
            {
                editor.putString(ph[j], PhoneNumberUtils.stripSeparators(phoneNo[j].split("\n")[1]));
                //Toast.makeText(MainActivity.this, "" + ph[j], Toast.LENGTH_LONG).show();
                j++;
            }
            editor.putInt("length",j);
            editor.apply();
        }
        if(pos.hasExtra("pos"))
        {
            position = pos.getIntExtra("pos",0);
            //editor.clear();
            editor.putInt("pos",position);
            editor.apply();
        }
        try
        {
            if (ActivityCompat.checkSelfPermission(this, mPermission) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{mPermission}, REQUEST_CODE_PERMISSION);
                // If any permission above not allowed by user, this condition will execute every time, else your else part will work
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        btnShowLocation = findViewById(R.id.b);
        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                gps = new GPSTracker(MainActivity.this);// create class object
                if(gps.canGetLocation())// check if GPS enabled
                {
                    double latitude = gps.getLatitude();
                    double longitude = gps.getLongitude();
                    msg = et.getText()+"\nHelp me...I'm at\n"+"https://www.google.com/maps?q="+latitude+","+longitude;
                    Toast.makeText(getApplicationContext(), msg+"", Toast.LENGTH_LONG).show();
                    send();
                }
                else
                {
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps.showSettingsAlert();
                }
            }
        });
    }
    public void send()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED)
        {
            if(phoneNo.length == 0 && flag == 0)
            {
                AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
                builder1.setMessage("No Contacts is Selected");
                builder1.setCancelable(true);
                builder1.setPositiveButton("Ok", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
                //Toast.makeText(MainActivity.this,"Contact Not Selected",Toast.LENGTH_LONG).show();
            }
            else if(flag != 1)
            {
                try
                {
                    flag=0;
                    //Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    //position = pos.getIntExtra("pos",0);
                    if(position == 0)
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
                        {
                            int i = 0;
                            while (i < phoneNo.length && phoneNo[i]!=null)
                            {
                                phoneNo[i]=PhoneNumberUtils.formatNumberToE164(phoneNo[i],"IN");
                                SmsManager.getSmsManagerForSubscriptionId(SmsManager.getDefaultSmsSubscriptionId()).sendTextMessage(phoneNo[i] + "", null, msg + "", null, null);
                                i++;
                            }
                        }
                        Toast.makeText(getApplicationContext(),"Message sent", Toast.LENGTH_LONG).show();
                    }
                    else if(position == 1)
                    {
                        int i = 0;boolean isWhatsappInstalled = whatsappInstalledOrNot();
                        if(isWhatsappInstalled)
                        {
                            Intent whatsapp = new Intent(Intent.ACTION_VIEW);
                            while (i < phoneNo.length && phoneNo[i] != null)
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    phoneNo[i] = PhoneNumberUtils.formatNumberToE164(phoneNo[i], "IN");
                                }
                                //Toast.makeText(this,phoneNo[i]+"",Toast.LENGTH_LONG).show();
                                whatsapp.setData(Uri.parse("https://api.whatsapp.com/send?phone=" + phoneNo[i] + "&text=" + URLEncoder.encode(msg, "UTF-8")));
                                startActivity(whatsapp);
                                onWindowFocusChanged(true);
                                i++;
                            }
                            Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Uri uri = Uri.parse("market://details?id=com.whatsapp");
                            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                            Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT).show();
                            startActivity(goToMarket);
                        }
                    }
                    else if(position == 2)
                    {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                        {
                            // here, Permission is not granted
                            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.CAMERA}, 50);
                        }
                        Intent p = new Intent(getApplicationContext(),Pic.class);
                        p.putExtra("text",msg);
                        p.putExtra("contact1",phoneNo);
                        startActivity(p);
                    }
                }
                catch (Exception ex)
                {
                    //Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    ex.printStackTrace();
                }
            }
        }
        else
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS},10);
            }
        }
    }
    private boolean whatsappInstalledOrNot()
    {
        PackageManager pm = getPackageManager();
        boolean app_installed = false;
        try
        {
            pm.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return app_installed;
    }
    protected void onResume()
    {
        super.onResume();
    }
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu1,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.Contacts:
                this.finish();
                Intent c = new Intent(getApplicationContext(),Contacts.class);
                startActivity(c);
                return true;
            case R.id.Settings:
                this.finish();
                Intent s = new Intent(getApplicationContext(),_Settings.class);
                startActivity(s);
                //Toast.makeText(MainActivity.this, "Settings is Selected", Toast.LENGTH_LONG).show();
                return true;
            default:return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        Intent i =getIntent();
        boolean open = i.getBooleanExtra("open", false);
        if (open)
        {
            //Log.e("start", "Activity started");
        }
    }
    /**
     * Showing google speech input dialog
     * */
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private void promptSpeechInput()
    {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,getString(R.string.speech_prompt));
        try
        {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        }
        catch (ActivityNotFoundException a)
        {
            Toast.makeText(getApplicationContext(),"speech_not_supported",Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQ_CODE_SPEECH_INPUT:
                {
                if (resultCode == RESULT_OK && data!=null)
                {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //txtSpeechInput.setText(result.get(0));
                    if(result!=null)
                    {
                        String s = result.get(0);
                        if(s.contains("help"))
                        {
                            send();
                            //Toast.makeText(this,result.get(0)+"",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            }

        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        this.gestureDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }
    @Override
    public boolean onDown(MotionEvent event)
    {
        return true;
    }
    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,float velocityX, float velocityY)
    {
        return false;
    }
    @Override
    public void onLongPress(MotionEvent event)
    {

    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,float distanceY)
    {
        return false;
    }
    @Override
    public void onShowPress(MotionEvent event)
    {

    }
    @Override
    public boolean onSingleTapUp(MotionEvent event)
    {
        return false;
    }
    int dc = 0;static long t;
    @Override
    public boolean onDoubleTap(MotionEvent event)
    {
        dc++;
        t = System.currentTimeMillis();
        if(dc>=3 && (System.currentTimeMillis()-t)==0)
        {
            dc = 0;
            promptSpeechInput();
            Toast.makeText(MainActivity.this, "Say 'Help'...", Toast.LENGTH_LONG).show();
        }
        //Toast.makeText(this,System.currentTimeMillis()-t+"",Toast.LENGTH_SHORT).show();
        return true;
    }
    @Override
    public boolean onDoubleTapEvent(MotionEvent event)
    {
        return true;
    }
    @Override
    public boolean onSingleTapConfirmed(MotionEvent event)
    {
        return false;
    }
}