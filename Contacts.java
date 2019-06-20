package com.example.Location_Alert.SOS;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.List;
public class Contacts extends Activity
{
    // The ListView
    private ListView lstNames;FloatingActionButton b;Toolbar toolbar;EditText inputSearch;
    ArrayAdapter adapter;int p;
    ArrayList<String> selected = new ArrayList<>();
    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    SharedPreferences sp;String[] s = new String[5];int c;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        sp = Contacts.this.getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);
        p = sp.getInt("Light",3);
        Intent theme = getIntent();
        if(theme.hasExtra("theme"))
            p = theme.getIntExtra("theme",3);
        if(p == 3)
        {
            setTheme(R.style.Theme_AppCompat_Light_DarkActionBar);
        }
        else if(p == 4)
        {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts);
        // Find the list view
        lstNames = findViewById(R.id.lstNames);
        b =findViewById(R.id.fab);
        inputSearch = findViewById(R.id.inputSearch);
        toolbar = findViewById(R.id.tb);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            toolbar.setTitle("Contacts");
        }
        // Read and show the contacts
        showContacts();
    }
    /**
     * Show the contacts in the ListView.
     */
    private void showContacts()
    {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        }
        else
        {
            // Android version is lesser than 6.0 or the permission is already granted.
            final List<String> contactData = getContactNames();
            if(contactData!=null)
            {

                lstNames.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, contactData);
                lstNames.setAdapter(adapter);
                String sel = sp.getString("selected", null);
                if (sel != null)
                {
                    String[] s1 = sel.split("\n");
                    int i = 0, j = 0;
                    while (i < lstNames.getCount() && j < s1.length && s1[j] != null)
                    {
                        //Log.e("s1"+i,s1[j]+"");
                        if (s1[j].equalsIgnoreCase(lstNames.getItemAtPosition(i).toString().split("\n")[0]) && s1[j] != null)
                        {
                            //Log.e("j "+j,s1[j]);
                            lstNames.setItemChecked(i, true);
                            s1[j] = s1[j] + "\n" + lstNames.getItemAtPosition(i).toString().split("\n")[1];
                            //Log.e("sj inner", s1[j] + "");
                            i = -1;
                            j += 2;
                        }
                        i++;
                    }
                    j = 0;
                    for (int k = 0; k < s1.length; k += 2)
                        s[j++] = s1[k];
                }
            }
            c=lstNames.getCheckedItemCount();
            //Log.e("count",c+"");
            lstNames.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    if (lstNames.isItemChecked(position)&&c < s.length)
                    {
                        s[c++]=lstNames.getItemAtPosition(position).toString();
                    }
                    if(!lstNames.isItemChecked(position))
                    {
                        selected.remove(lstNames.getItemAtPosition(position));
                        //Log.e("outer",s[c-1]+"");
                        //Log.e("remove",lstNames.getItemAtPosition(position)+"");
                        c--;
                    }
                    //Log.e("c ",c+"");
                    //Toast.makeText(Contacts.this,lstNames.getItemAtPosition(position)+" "+c,Toast.LENGTH_SHORT).show();
                }
            });
            inputSearch.addTextChangedListener(new TextWatcher()
            {
               @Override
               public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3)
               {
                   // When user changed the Text
                   Contacts.this.adapter.getFilter().filter(cs);
               }
               @Override
               public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
               {
                   SparseBooleanArray checked = lstNames.getCheckedItemPositions();
                   for (int i = 0; i < lstNames.getCount(); i++)
                   {
                       if (checked.get(i))
                       {
                           Object o = adapter.getItem(i);
                           String name = o.toString();
                           // if the arraylist does not contain the name, add it
                           if (selected.contains(name))
                           {/*Do Nothing*/}
                           else
                           { selected.add(name);}
                       }
                   }
               }
               @Override
               public void afterTextChanged(Editable s)
               {
                   adapter.getFilter().filter(s, new Filter.FilterListener()
                   {
                       public void onFilterComplete(int count)
                       {
                           adapter.notifyDataSetChanged();
                           for (int i = 0; i < adapter.getCount(); i++)
                           {
                               // if the current (filtered)
                               // listview you are viewing has the name included in the list,
                               // check the box
                               Object o = adapter.getItem(i);
                               String name = o.toString();
                               if (selected.contains(name))
                               { lstNames.setItemChecked(i, true);}
                               else
                               { lstNames.setItemChecked(i, false);}
                           }
                       }
                   });
               }
           });
            b.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    adapter = new ArrayAdapter<>(Contacts.this, android.R.layout.simple_list_item_multiple_choice, contactData);
                    lstNames.setAdapter(adapter);
                    /*Log.e("checked ",lstNames.getCheckedItemCount()+"");
                    Log.e("s length",s.length+"");
                    Log.e("c ",c+"");*/
                    String[] o = new String[c];StringBuffer str;
                    str = new StringBuffer();
                    //Log.e("count",c+"");
                    if(lstNames.getCheckedItemCount() > 0 && lstNames.getCheckedItemCount() <= 5)
                    {
                        int i=0,j=0;
                        while (i < lstNames.getCount() && j<c)
                        {
                            if(s[j]!=null)
                            {
                                //Log.e("c"+c,s[c]+"");
                                //Log.e("o"+c,o[c]+"");
                                //Log.e("i+"+i,lstNames.getItemAtPosition(i).toString());
                                if (s[j].equalsIgnoreCase(lstNames.getItemAtPosition(i).toString()))
                                {
                                    o[j] = s[j];
                                    str = str.append(o[j]+"\n");
                                    i=0;
                                    //Log.e("str ",str.toString());
                                    j++;
                                }
                            }
                            i++;
                        }
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("selected",str.toString());
                        editor.apply();
                        //Toast.makeText(Contacts.this,str+"",Toast.LENGTH_LONG).show();
                        Intent contact1 = new Intent(getApplicationContext(), MainActivity.class);
                        contact1.putExtra("contact1", o);
                        startActivity(contact1);
                        finish();
                    }
                    else if(c <= 0)
                    {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(Contacts.this);
                        builder1.setMessage("No Contacts are selected");
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
                    }
                    else if(c>5)
                    {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(Contacts.this);
                        builder1.setMessage("Only 5 contacts are to be selected");
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
                        for(int i=0;i<s.length;i++)
                            s[i]=null;
                        lstNames.clearChoices();
                        c=0;
                    }
                }
            });
        }
    }
    @Override
    public void onBackPressed()
    {
        if(lstNames.getCheckedItemCount() <= 0 || lstNames.getCheckedItemCount() > 5 )
        {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(Contacts.this);
            builder1.setMessage("Contacts not Selected Properly");
            builder1.setCancelable(true);
            builder1.setPositiveButton("Ok", new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int id)
                {
                    Contacts.this.finish();
                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                    dialog.cancel();
                }
            });
            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
        else
        {
            this.finish();
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                // Permission is granted
                showContacts();
            }
            else
            {
                Toast.makeText(this, "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**
     * Read the name of all the contacts.
     *
     * @return a list of names.
     */
    private List<String> getContactNames() {
        List<String> contacts = new ArrayList<>();
       // Get the ContentResolver
        ContentResolver cr = getContentResolver();
        // Get the Cursor of all the contacts
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
        // Move the cursor to first. Also check whether the cursor is empty or not.
        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                // Iterate through the cursor
                do
                {
                    // Get the contacts name and number
                    String ph = null;
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                    {
                        // Query phone here. Covered next
                        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                        if (phones != null)
                        {
                            while (phones.moveToNext())
                            {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                {
                                    ph = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
                                }
                            }
                        }
                    }
                    if (ph != null)
                        contacts.add(name + "\n" + ph);
                } while (cursor.moveToNext());
            }
            // Close the cursor
            cursor.close();
            return contacts;
        }
        return null;
    }
}