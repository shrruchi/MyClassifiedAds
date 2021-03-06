package com.example.home_pc.myclassifiedads.myads;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.home_pc.myclassifiedads.R;
import com.example.home_pc.myclassifiedads.classified_api.JSONParser;
import com.example.home_pc.myclassifiedads.classified_api.RestAPI;
import com.example.home_pc.myclassifiedads.common_contactsnwanted.ContactnWantedAdsAdapter;
import com.example.home_pc.myclassifiedads.common_contactsnwanted.ContactsnWantedAdObject;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Ruchi on 2015-09-05.
 */
public class MyContactsFragment extends Fragment {
    RecyclerView contactsList;
    ProgressDialog progressDialog;
    ArrayList<ContactsnWantedAdObject> cObject;
    Context context;
    ContactnWantedAdsAdapter contactAdsAdapter;
    String tableCategory,userID,userCategory;
    SwipeRefreshLayout mswipeRefreshLayout;
    public MyContactsFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.ads_recycler_view, container,
                false);
        context = getActivity();
        userID=getArguments().getString("userID");
        tableCategory=getArguments().getString("tableCategory");
        userCategory=getArguments().getString("userCategory");
        contactsList=(RecyclerView) view.findViewById(R.id.cardList);
        mswipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swipeRefreshLayout);
        contactsList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        contactsList.setLayoutManager(llm);
        loadContactAds();
        mswipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadContactAds();
                onItemLoadComplete();
            }
        });

        return view;
    }
    public void onItemLoadComplete(){
        mswipeRefreshLayout.setRefreshing(false);
    }

    public void loadContactAds(){
        new AsyncLoadContactAds().execute();
    }


    protected class AsyncLoadContactAds extends
            AsyncTask<Void, Void, ArrayList<ContactsnWantedAdObject>> {


        @Override
        protected ArrayList<ContactsnWantedAdObject> doInBackground(Void... params) {

            RestAPI api = new RestAPI();
            try {
                cObject=new ArrayList<ContactsnWantedAdObject>();
                JSONObject jsonObj = api.GetMyContactsList(userID,tableCategory);
                JSONParser parser = new JSONParser();
                cObject = parser.parseContactsList(jsonObj);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                Log.d("AsyncLoadContactList", "" + e);
            }
            return cObject;
        }

        @Override
        protected void onPreExecute(){
            progressDialog=new ProgressDialog(context);
            progressDialog.setMessage("Loading...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.show();

        }

        @Override
        protected void onPostExecute(ArrayList<ContactsnWantedAdObject> result) {
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            if(result!=null){
                contactAdsAdapter =new ContactnWantedAdsAdapter(context,result,tableCategory,userID,1);
                contactsList.setAdapter(contactAdsAdapter);
            } else{
                Toast.makeText(context, "NO ADS FOUND :(", Toast.LENGTH_LONG).show();
            }
        }
    }
}

