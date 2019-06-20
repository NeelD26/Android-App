package com.example.Location_Alert.SOS;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.telephony.PhoneNumberUtils;
import android.widget.ImageView;
import android.widget.Toast;
public class Pic extends Activity
{
    private static final int CAMERA_REQUEST = 1888;
    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
        this.imageView = this.findViewById(R.id.imageView1);
        Camera camera = Camera.open(1);
        Camera.Parameters p = camera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(p);
        picture();
    }
    public void picture()
    {
        this.imageView = this.findViewById(R.id.imageView1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            }
            else
            {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(photo);
            Toast.makeText(this,imageView.getDrawable()+"",Toast.LENGTH_SHORT).show();
            Intent pos = getIntent();
            String[] phoneNo;
            phoneNo = pos.getStringArrayExtra("contact1");
            Bitmap b =((BitmapDrawable)imageView.getDrawable()).getBitmap();
            String img = MediaStore.Images.Media.insertImage(getContentResolver(),b,System.currentTimeMillis()+"",null);
            Uri uri = Uri.parse(img);
            if(whatsappInstalledOrNot())
            {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                int i = 0;
                while (i < phoneNo.length && phoneNo[i] != null)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
                        phoneNo[i] = PhoneNumberUtils.formatNumberToE164(phoneNo[i], "IN");
                    }
                    sendIntent.setPackage("com.whatsapp");
                    sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    sendIntent.setType("image/jpeg");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, pos.getStringExtra("text"));
                    sendIntent.putExtra("jid", phoneNo[i].substring(1) + "@s.whatsapp.net");//phone number without "+" prefix
                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    this.finish();
                    startActivity(sendIntent);
                    onWindowFocusChanged(true);
                    i++;
                }
                Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_LONG).show();
            }
            else
            {
                Uri ur = Uri.parse("market://details?id=com.whatsapp");
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, ur);
                Toast.makeText(this, "WhatsApp not Installed", Toast.LENGTH_SHORT).show();
                startActivity(goToMarket);
            }
        }
        else if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_CANCELED)
        {
            this.finish();
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
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
}