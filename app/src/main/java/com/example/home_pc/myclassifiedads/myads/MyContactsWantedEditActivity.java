package com.example.home_pc.myclassifiedads.myads;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home_pc.myclassifiedads.R;
import com.example.home_pc.myclassifiedads.classified_api.ImageLoaderAPI;
import com.example.home_pc.myclassifiedads.classified_api.JSONParser;
import com.example.home_pc.myclassifiedads.classified_api.RestAPI;
import com.example.home_pc.myclassifiedads.common_contactsnwanted.ContactsnWantedAdObject;
import com.example.home_pc.myclassifiedads.mainactivity.LocateOnMapActivity;

import org.json.JSONObject;

import java.util.ArrayList;

public class MyContactsWantedEditActivity extends ActionBarActivity {
    int adid;
    String userID,tableCategory;
    Context context = this;
    private final int RESULT_LOAD_IMAGE = 0;
    private final int RESULT_CAMERA_PIC = 1;
    private final int REQUEST_LATLONG = 2;
    private ImageView uploadedPic;
    private TextView uploadPic,locateOnMap,dialogOption1,dialogOption2;
    private EditText title,description,aDdress,contactNo,mobileNo,emailId;
    String userName,adTitle,adDescription,adAddress,adContactNo,adMobileNo,adEmailId,adCategory,adtype,adImageURL;
    private Spinner category;
    ArrayAdapter<String> categoryAdapter;
    Double _latitude=0.0,_longitude=0.0;
    private Button saveButton;
    Bitmap picture,temp;
    int photoCount;
    Boolean toggle;
    Dialog dialog;
    ContactsnWantedAdObject contactsnWantedAdObject;
    ArrayList<String> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contactsnwanted);

        adid=getIntent().getExtras().getInt("adid");
        adtype=getIntent().getExtras().getString("tableCategory");
        userID=getIntent().getExtras().getString("userID");

        uploadedPic=(ImageView) findViewById(R.id.adImage);
        uploadPic=(TextView) findViewById(R.id.uploadPics);
        title=(EditText) findViewById(R.id.title);
        description=(EditText) findViewById(R.id.description);
        category=(Spinner) findViewById(R.id.adCategory);
        switch(adtype){
            case "contacts":new AsyncLoadContactsList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);break;
            case "wanted":new AsyncLoadWantedList().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);break;
        }
        new AsyncLoadDetails().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        aDdress=(EditText) findViewById(R.id.aDdress);
        contactNo=(EditText) findViewById(R.id.contactNo);
        mobileNo=(EditText) findViewById(R.id.mobileNo);
        emailId=(EditText) findViewById(R.id.emailId);
        locateOnMap=(TextView) findViewById(R.id.locateOnMap);
        saveButton= (Button) findViewById(R.id.saveButton);

        dialog=new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog);
        dialogOption1=(TextView)dialog.findViewById(R.id.dialogOption1);
        dialogOption2=(TextView)dialog.findViewById(R.id.dialogOption2);

        uploadedPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPictureClick(v);
            }
        });

        uploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPictureClick(v);
            }
        });

        locateOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent locateOnMap= new Intent(getApplicationContext(), LocateOnMapActivity.class);
                locateOnMap.putExtra("Latitude",_latitude);
                locateOnMap.putExtra("Longitude",_longitude);
                startActivityForResult(locateOnMap, REQUEST_LATLONG);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButtonClick();
            }
        });
    }

    public void saveButtonClick(){
        adTitle = title.getText().toString();
        adDescription = description.getText().toString();
        adAddress = aDdress.getText().toString();
        adContactNo = contactNo.getText().toString();
        adMobileNo = mobileNo.getText().toString();
        adEmailId = emailId.getText().toString();
        adCategory = category.getSelectedItem().toString();
        adImageURL = "-";
        contactsnWantedAdObject = new ContactsnWantedAdObject(userID,adTitle,adDescription,adAddress,adContactNo,adMobileNo,adEmailId,adCategory,_latitude,_longitude,adImageURL);
        saveButton.setEnabled(false);
        switch(adtype){
            case "contacts":new AsyncUpdateContactsAds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,contactsnWantedAdObject);break;
            case "wanted":new AsyncUpdateWantedAds().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,contactsnWantedAdObject);break;
        }

    }

    public void uploadPictureClick(View view){

        if(!toggle) {
            if (photoCount == 0) {

                dialog.setTitle("Choose option to upload photo:");
                dialogOption1.setText("From Device");
                dialogOption2.setText("From Camera");
                dialog.show();

                dialogOption1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent gallery = new Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(gallery, RESULT_LOAD_IMAGE);
                    }
                });

                dialogOption2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent camera = new Intent("android.media.action.IMAGE_CAPTURE");
                        startActivityForResult(camera, RESULT_CAMERA_PIC);
                    }
                });
            }
            toggle=true;
        }

        else {

            if (photoCount > 0) {

                dialog.setTitle("Do you want to remove this photo?");
                dialogOption1.setText("Yes");
                dialogOption2.setText("No");
                dialog.show();

                dialogOption1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        uploadedPic.setImageResource(R.drawable.camerapic);
                        picture = null;
                        photoCount--;
                        dialog.dismiss();
                    }
                });

                dialogOption2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
            toggle=false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==REQUEST_LATLONG){
            if(resultCode== LocateOnMapActivity.RESULT_LATLONG){
                _latitude=data.getDoubleExtra("Latitude",0.0);
                _longitude=data.getDoubleExtra("Longitude",0.0);
                if(_latitude==0.0 && _longitude==0.0){
                    _latitude=null;
                    _longitude=null;
                }
                Toast.makeText(this, "" + _latitude + " " + _longitude, Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            picture = BitmapFactory.decodeFile(picturePath);
            temp = Bitmap.createScaledBitmap(picture, dptopx(100), dptopx(100), true);
            uploadedPic.setImageBitmap(temp);
            photoCount++;
        } else if (requestCode == RESULT_CAMERA_PIC && resultCode == RESULT_OK && data!=null) {
            picture = (Bitmap) data.getExtras().get("data");
            temp = Bitmap.createScaledBitmap(picture, dptopx(100), dptopx(100), true);
            uploadedPic.setImageBitmap(temp);
            photoCount++;
        }

        dialog.dismiss();
    }


    protected class AsyncLoadContactsList extends AsyncTask<Void,Void,ArrayList<String>>{

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            System.out.println("LOADLIST");
            ArrayList<String> categoryLst = new ArrayList<String>();
            RestAPI api = new RestAPI();
            try{
                JSONObject object = api.GetContactsCategory();
                JSONParser parser = new JSONParser();
                categoryLst = parser.getList(object);
            }
            catch(Exception e){}

            return categoryLst;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            categoryList = new ArrayList<String>();
            if(result.size() != 0 && result !=null) {
                for (int i = 0; i < result.size(); i++) {
                    categoryList.add(result.get(i));
                }
            }
            else{
                categoryList.add("Not Available");
            }
            categoryAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,categoryList);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            category.setAdapter(categoryAdapter);
        }
    }

    protected class AsyncLoadWantedList extends AsyncTask<Void,Void,ArrayList<String>>{

        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            System.out.println("LOADLIST");
            ArrayList<String> categoryLst = new ArrayList<String>();
            RestAPI api = new RestAPI();
            try{
                JSONObject object = api.GetWantedCategory();
                JSONParser parser = new JSONParser();
                categoryLst = parser.getList(object);
            }
            catch(Exception e){}

            return categoryLst;
        }

        @Override
        protected void onPostExecute(ArrayList<String> result) {
            categoryList = new ArrayList<String>();
            if(result.size() != 0 && result !=null) {
                for (int i = 0; i < result.size(); i++) {
                    categoryList.add(result.get(i));
                }
            }
            else{
                categoryList.add("Not Available");
            }
            categoryAdapter = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item,categoryList);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            category.setAdapter(categoryAdapter);
        }
    }


    protected class AsyncLoadDetails extends
            AsyncTask<Void, Void,ContactsnWantedAdObject> {
        ProgressDialog progressDialog;
        @Override
        protected ContactsnWantedAdObject doInBackground(Void...params) {
            // TODO Auto-generated method stub
            RestAPI api = new RestAPI();
            try {
                JSONObject jsonObj = api.GetContactDetails(adid, adtype);
                JSONParser parser = new JSONParser();
                contactsnWantedAdObject = parser.parseContactDetails(jsonObj);

            } catch (Exception e) {
                // TODO Auto-generated catch block
              //  Log.d("AsyncLoadContactDetails", e.getMessage());
                System.out.println("Error=>"+e);
            }

            return contactsnWantedAdObject;
        }

        @Override
        protected void onPreExecute(){
            progressDialog=new ProgressDialog(MyContactsWantedEditActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.show();

        }



        @Override
        protected void onPostExecute(ContactsnWantedAdObject result) {
            // TODO Auto-generated method stub
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            title.setText(result.gettitle());
            description.setText(result.getDescription());
            aDdress.setText(result.getaDdress());
            contactNo.setText(result.getContactNo());
            emailId.setText(result.getemail());
            mobileNo.setText(result.getMobileNo());
            _latitude=result.getLatitude();
           _longitude=result.getLongitute();
            if(!result.getAdImage().equals("-")){
                new AsyncLoadImage().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,result.getAdImage());
                photoCount=1;
                toggle=true;
            }else
            {
                photoCount=0;
                toggle=false;
            }
        }
    }

    protected class AsyncLoadImage extends
            AsyncTask<String, Void, Bitmap> {
            Bitmap contactswanted_image;
        @Override
        protected Bitmap doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                contactswanted_image = ImageLoaderAPI.AzureImageDownloader(params[0]);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("AsyncLoadImage", ""+e);
            }

            return contactswanted_image;
        }

        @Override
        protected void onPostExecute(Bitmap result){
            picture=result;
            temp=Bitmap.createScaledBitmap(result,dptopx(100),dptopx(100),true);
            contactswanted_image=Bitmap.createScaledBitmap(result,dptopx(110),dptopx(110),true);
            uploadedPic.setImageBitmap(contactswanted_image);
        }
    }

    protected class AsyncUpdateContactsAds extends AsyncTask<ContactsnWantedAdObject,Void,String>
    {
        String pictureURL;
        String result;

        @Override
        protected String doInBackground(ContactsnWantedAdObject... params) {


            RestAPI api = new RestAPI();

            try{
                api.UpdateContactsAds(adid,params[0].gettitle(), params[0].getDescription(), params[0].getCategory(), params[0].getaDdress(), params[0].getContactNo(), params[0].getMobileNo(), params[0].getemail(), params[0].getLatitude(), params[0].getLongitute(), params[0].getAdImage());
                JSONParser parser = new JSONParser();
                if(picture!=null) {
                    pictureURL = ImageLoaderAPI.AzureImageUploader(picture,temp,"Contacts" + adid);
                    JSONObject object = api.UpdateContactsAd("" + adid, pictureURL);
                    result = parser.getResult(object);
                }
                else{result = "Success";}
            }
            catch (Exception e){

            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            System.out.println("Contacts: " + result);
            saveButton.setEnabled(true);
            onBackPressed();
        }
    }


    protected class AsyncUpdateWantedAds extends AsyncTask <ContactsnWantedAdObject,Void,String>
    {

        String adID;
        String pictureURL;
        String result;

        @Override
        protected String doInBackground(ContactsnWantedAdObject... params) {


            RestAPI api = new RestAPI();

            try{
                api.UpdateWantedAds(adid,params[0].gettitle(), params[0].getDescription(), params[0].getCategory(), params[0].getaDdress(), params[0].getContactNo(), params[0].getMobileNo(), params[0].getemail(), params[0].getLatitude(), params[0].getLongitute(), params[0].getAdImage());
                JSONParser parser = new JSONParser();
                pictureURL = ImageLoaderAPI.AzureImageUploader(picture,temp,"Wanted" + adid);
                System.out.println(pictureURL);

                JSONObject object = api.UpdateWantedAd(""+adid, pictureURL);
                result = parser.getResult(object);
            }
            catch (Exception e){
                result = ""+e;
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            System.out.println("Wanted: "+result);
            saveButton.setEnabled(true);
            onBackPressed();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public int dptopx(float dp){
        // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return ((int) (dp * scale + 0.5f));
    }
}
