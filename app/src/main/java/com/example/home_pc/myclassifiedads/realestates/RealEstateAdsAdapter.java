package com.example.home_pc.myclassifiedads.realestates;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.home_pc.myclassifiedads.R;
import com.example.home_pc.myclassifiedads.classified_api.ImageLoaderAPI;
import com.example.home_pc.myclassifiedads.classified_api.JSONParser;
import com.example.home_pc.myclassifiedads.classified_api.RestAPI;
import com.example.home_pc.myclassifiedads.mainactivity.MainActivity;
import com.example.home_pc.myclassifiedads.myads.MyRealestateEditActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Home-PC on 7/31/2015.
 */
public class RealEstateAdsAdapter extends RecyclerView.Adapter<RealEstateAdsAdapter.ViewHolder> {
    String userID;
    Context context;
    private ArrayList<RealEstatesAdObject> realEstatesAdObjects;
    View view;
    Intent intent;
    int flag;

    public RealEstateAdsAdapter(Context context,ArrayList<RealEstatesAdObject> realEstatesAdObjects,String userID,int flag) {
        this.context=context;
        this.realEstatesAdObjects=realEstatesAdObjects;
        this.userID=userID;
        this.flag=flag;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        protected ImageView realEstateImage;
        protected TextView realEstateTitle,realEstateAddress,realEstateContact,username,realEstatePrice,saleType;
        public ImageView realEstatepopupMenu;

        public ViewHolder(View itemView) {
            super(itemView);
            realEstateImage=(ImageView)itemView.findViewById(R.id.realEstateImage);
            realEstateTitle=(TextView)itemView.findViewById(R.id.realEstateTitle);
            realEstateAddress=(TextView)itemView.findViewById(R.id.realEstateAddress);
            realEstateContact=(TextView)itemView.findViewById(R.id.realEstateContact);
            username=(TextView)itemView.findViewById(R.id.username);
            realEstatePrice=(TextView)itemView.findViewById(R.id.realEstatePrice);
            saleType=(TextView)itemView.findViewById(R.id.saleType);
            realEstatepopupMenu=(ImageView)itemView.findViewById(R.id.realEstatePopupMenu);
        }
    }
    public void remove(RealEstatesAdObject reo) {
        int position=realEstatesAdObjects.indexOf(reo);
        realEstatesAdObjects.remove(position);
        notifyItemRemoved(position);
    }


    @Override
    public RealEstateAdsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_realestate, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final RealEstatesAdObject reo=realEstatesAdObjects.get(position);

        holder.realEstateTitle.setText(reo.title);
        holder.realEstatePrice.setText("NPR."+reo.price);
        holder.saleType.setText(reo.saleType);
        holder.realEstateAddress.setText(reo.aDdress);
        holder.realEstateContact.setText(reo.contactNo);
        holder.username.setText(reo.userName);
        new AsyncLoadImage(position,holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, reo.realestateID);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(v.getContext(), RealEstateViewDetail.class);
                Bundle bundle = new Bundle();
                bundle.putInt("realestateID", reo.realestateID);
                bundle.putString("userID", userID);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
        switch (flag) {
            case 0:
                holder.realEstatepopupMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final PopupMenu popup = new PopupMenu(v.getContext(), v);
                        popup.inflate(R.menu.overflow_popup_menu);
                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()) {
                                    case R.id.item_watchlist:
                                        if (userID.equals("Guest")) {
                                            navigatetohome();
                                        } else {
                                            RealEstatesAdObject ro=new RealEstatesAdObject(reo.realestateID,reo.title);
                                            new AsyncSavetoWatchlist().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,ro);
                                        }
                                }
                                return true;
                            }
                        });
                    }
                });
                break;
            case 1:
                holder.realEstatepopupMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final PopupMenu popup = new PopupMenu(v.getContext(), v);
                        popup.inflate(R.menu.myoverflow_popup_menu);
                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                switch (menuItem.getItemId()) {
                                    case R.id.item_edit:
                                        int ad_id=reo.realestateID;
                                        navigatetoEditActivity(ad_id);
                                        break;
                                    case R.id.item_delete:
                                        final AlertDialog alertDialog = new AlertDialog.Builder(
                                                context).create();
                                        alertDialog.setMessage("Are you sure?");
                                        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                deleteFromDb(reo.realestateID);
                                                remove(reo);
                                            }
                                        });
                                        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                alertDialog.dismiss();
                                            }
                                        });
                                        alertDialog.show();
                                        break;
                                }
                                return true;
                            }
                        });
                    }
                });
                break;
            default:
                break;
        }
    }

    public void navigatetoEditActivity(int ad_id){
        intent =new Intent(context, MyRealestateEditActivity.class);
        intent.putExtra("userID", userID);
        intent.putExtra("realestateID", ad_id);
        context.startActivity(intent);

    }

    public void navigatetohome(){
        final AlertDialog alertDialog = new AlertDialog.Builder(
                context).create();
        alertDialog.setMessage("Please create your account first or Login");
        alertDialog.setIcon(R.drawable.backward);
        alertDialog.setTitle("The Classified Ads App");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(context, MainActivity.class);
                context.startActivity(intent);
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }


    protected class AsyncSavetoWatchlist extends
            AsyncTask<RealEstatesAdObject, Void, Boolean> {

        Boolean flag=false;
        @Override
        protected Boolean doInBackground(RealEstatesAdObject... params) {
            RestAPI api = new RestAPI();
            try {
                JSONObject jsonObject = api.PushtoWatchlist(params[0].realestateID,"RealEstate",userID,params[0].title);
                JSONParser parser = new JSONParser();
                flag= parser.parseReturnedValue(jsonObject);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("AsyncSavetoWatchlist", e.getMessage());
            }
            return flag;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            final AlertDialog alertDialog = new AlertDialog.Builder(
                    context).create();
            if(result){
                alertDialog.setMessage("Added to watchlist");
                // Toast.makeText(context,"Added to watchlist",Toast.LENGTH_LONG).show();
            }
            else{
                alertDialog.setMessage("Already added");
            }
            alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }
    }

    protected class AsyncLoadImage extends
            AsyncTask<Integer, Void, Bitmap> {
        int pos;
        ViewHolder holder;
        Bitmap realestatePic;
        String picURL;

        public AsyncLoadImage(int pos,ViewHolder holder) {
            this.pos = pos;
            this.holder = holder;
        }

        @Override
        protected Bitmap doInBackground(Integer... params) {
            RestAPI api = new RestAPI();
            try {
                JSONObject jsonObject = api.GetRealestatePictureURL(params[0]);
                JSONParser parser = new JSONParser();
                picURL= parser.parseReturnedURL(jsonObject);
                if(picURL!=null){
                    String sub1 = picURL.substring(0,61);
                    String sub2 = "temp_"+picURL.substring(61);
                    System.out.println("here=>"+sub1+sub2);
                    realestatePic= ImageLoaderAPI.AzureImageDownloader(sub1+sub2);
                }
                else{
                    realestatePic=null;
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("AsyncLoadURl", e.getMessage());
            }
            return realestatePic;
        }

        @Override
        protected void onPostExecute(Bitmap result){
            if(result!=null){
                realestatePic= Bitmap.createScaledBitmap(result, dptopx(100), dptopx(100), true);
                holder.realEstateImage.setImageBitmap(result);
            }

        }

    }
    public void deleteFromDb(int adid) {
        Toast.makeText(context, "" + adid, Toast.LENGTH_LONG).show();
        new AsyncDeleteRealestateAd().execute(adid);
    }

    protected class AsyncDeleteRealestateAd extends
            AsyncTask<Integer, Void, Void> {
        int adid;

        @Override
        protected Void doInBackground(Integer ...params) {
            // TODO Auto-generated method stub

            RestAPI api = new RestAPI();
            try {
                api.DeleteRealestateAd(adid,"RealEstate");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("AsyncLoadResult", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub

            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();

        }
    }


    @Override
    public int getItemCount() {
        return realEstatesAdObjects.size();
    }
    public int dptopx(float dp){
        // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return ((int) (dp * scale + 0.5f));
    }

}