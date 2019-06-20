package com.example.Location_Alert.SOS;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toolbar;
public class _Settings extends ListActivity
{
    Toolbar toolbar;SharedPreferences sp;
    Switch switch1,switch2;
    protected void onCreate(Bundle savedInstanceState)
    {
        sp = _Settings.this.getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        int p = sp.getInt("Light",3);
        if(p == 3)
        {
            setTheme(R.style.Theme_AppCompat_Light_DarkActionBar);
        }
        else if(p == 4)
        {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        toolbar = findViewById(R.id.tb);
        int tog = sp.getInt("pos",0);
        if(tog!=0)
        {
            if(tog==1)
                switch1.setChecked(true);
            if(tog==2)
                switch2.setChecked(true);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            toolbar.setTitle(R.string.settings);
        }
        String[] values = getResources().getStringArray(R.array.settings_values);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, values);
        //Assign adapter to list
        setListAdapter(adapter);
        final SharedPreferences.Editor editor = sp.edit();
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    Intent p = new Intent(getApplicationContext(),MainActivity.class);
                    p.putExtra("pos",1);
                    editor.putInt("pos",1);
                    editor.apply();
                    switch2.setChecked(false);
                    //startActivity(p);
                    //finish();
                    // The toggle is enabled
                }
                else if(switch2.isChecked())
                {
                    Intent p = new Intent(getApplicationContext(),MainActivity.class);
                    p.putExtra("pos",2);
                    editor.putInt("pos",2);
                    editor.apply();
                    //startActivity(p);
                    //finish();
                    // The toggle is disabled
                }
                else
                {
                    Intent p = new Intent(getApplicationContext(),MainActivity.class);
                    p.putExtra("pos",0);
                    editor.putInt("pos",0);
                    editor.apply();
                    //startActivity(p);
                    //finish();
                    // The toggle is disabled
                }
            }
        });
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (isChecked)
                {
                    Intent p = new Intent(getApplicationContext(),MainActivity.class);
                    p.putExtra("pos",2);
                    editor.putInt("pos",2);
                    editor.apply();
                    //startActivity(p);
                    //finish();
                    // The toggle is enabled
                }
                else if(switch1.isChecked())
                {
                    Intent p = new Intent(getApplicationContext(),MainActivity.class);
                    p.putExtra("pos",1);
                    editor.putInt("pos",1);
                    editor.apply();
                    //startActivity(p);
                    //finish();
                    // The toggle is disabled
                }
                else
                {
                    Intent p = new Intent(getApplicationContext(),MainActivity.class);
                    p.putExtra("pos",0);
                    editor.putInt("pos",0);
                    editor.apply();
                    //startActivity(p);
                    //finish();
                }
            }
        });
    }
    @Override
    public void onListItemClick(ListView l, View v, final int pos, final long id)
    {
        //super.onListItemClick(l,v,pos,id);
        //ListView Clicked item index
        final Intent position = new Intent(getApplicationContext(),MainActivity.class);
        switch (pos)
        {
            case 0:
                position.putExtra("pos",pos);
                switch1.setChecked(false);
                switch2.setChecked(false);
                synchronized (this)
                {
                    try
                    {
                        this.wait(500);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                //Toast.makeText(this,"Default Selected",Toast.LENGTH_LONG).show();
                startActivity(position);
                this.finish();
                break;
                //Toast.makeText(this,pos+"",Toast.LENGTH_LONG).show();break;
            case 1:
                if(switch1.isChecked())
                    switch1.setChecked(false);
                else
                    switch1.setChecked(true);
                break;
                //Toast.makeText(this,pos+"",Toast.LENGTH_LONG).show();break;
            case 2:
                if(switch2.isChecked())
                    switch2.setChecked(false);
                else
                    switch2.setChecked(true);
                break;
                //Toast.makeText(this,pos+"", Toast.LENGTH_LONG).show();break;
            case 3:
                final SharedPreferences.Editor editor = sp.edit();
                //Toast.makeText(this,pos+"", Toast.LENGTH_LONG).show();
                String[] values = {"Light","Dark"};
                final AlertDialog.Builder theme = new AlertDialog.Builder(_Settings.this);
                theme.setTitle("Select Theme:\n");
                theme.setCancelable(true);
                theme.setSingleChoiceItems(values, -1, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int item)
                    {
                        switch(item)
                        {
                            case 0:
                                editor.putInt("Light",item+3);
                                //Toast.makeText(_Settings.this, "First Item Clicked", Toast.LENGTH_LONG).show();
                                break;
                            case 1:
                                editor.putInt("Light",item+3);
                                //Toast.makeText(_Settings.this, "Second Item Clicked", Toast.LENGTH_LONG).show();
                                break;
                        }
                        editor.apply();
                        position.putExtra("theme",item+3);
                        startActivity(position);
                        _Settings.this.finish();
                        //this.finish();
                    }
                });
                theme.create();
                theme.show();
                break;
            case 4:
                try
                {
                    PackageManager manager = this.getPackageManager();
                    PackageInfo info = manager.getPackageInfo(_Settings.this.getPackageName(), PackageManager.GET_ACTIVITIES);
                    AlertDialog.Builder a = new AlertDialog.Builder(_Settings.this);
                    ApplicationInfo app = this.getPackageManager().getApplicationInfo("com.example.Location_Alert.SOS",0);
                    a.setMessage(""+app+"\nVersionCode= "+info.versionCode+"\nVersionName= "+info.versionName +"\nFirstTimeInstallation="
                                    +info.firstInstallTime+"\nLastUpdateTime= "+info.lastUpdateTime);
                    a.create();a.show();
                }
                catch (PackageManager.NameNotFoundException e)
                {
                    e.printStackTrace();
                }
                break;
            default:
                position.putExtra("pos",pos);
                startActivity(position);
                super.onListItemClick(l,v,pos,id);break;
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            Intent i = new Intent(_Settings.this,MainActivity.class);
            startActivity(i);
            this.finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}