package net.compuways.keywordsmanager;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivityFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {

    private ListView lvwSelections;
    private TextView messageView;

    private String message = "", keywordValue = "", localcity = "", country = "", state = "", city = "";
    //localcity is the name of city, city is search string, may contain "%2c" character
    private int PLACE_PICKER_REQUEST = 1, sourceID = 0, textColorInt = -1;
    private boolean appStatus = false, editStatus = false, deleteStatus = false;
    private double latitude = 0.0, longitude = 0.0;

    private final ArrayList<Keyword> list = new ArrayList<Keyword>();
    protected StableArrayAdapter adapter;
    private GoogleApiClient client;
    private Uri uri = null;
    private Intent intent = null;
    private FragmentManager fm;
    private SharedPreferences SP;
    public MainActivityFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        client = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), 0, this)
                .addOnConnectionFailedListener(this)
                .build();
        messageView = (TextView) view.findViewById(R.id.messagView);
        lvwSelections = (ListView) view.findViewById(R.id.lvwSelections);
        adapter = new StableArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_1, list);
        lvwSelections.setAdapter(adapter);
        lvwSelections.setOnItemClickListener(lvwSelectionsClickListener);
        lvwSelections.setItemsCanFocus(true);

        SP = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());

        textColorInt = Color.parseColor(SP.getString("mainListviewftColor", "#000000"));
        lvwSelections.setBackgroundColor(Color.parseColor(SP.getString("mainListviewbgColor", "#DFF7F4")));

        //load saved isntance
        if (savedInstanceState != null) {
            sourceID = savedInstanceState.getInt("sourceIDValue", 0);
            country = savedInstanceState.getString("countryValue", "");
            state = savedInstanceState.getString("stateValue", "");
            city = savedInstanceState.getString("cityValue", "");
            localcity = savedInstanceState.getString("localcityValue", "");
            keywordValue = savedInstanceState.getString("keywordValue", "");
            latitude = savedInstanceState.getDouble("latitude", 0);
            longitude = savedInstanceState.getDouble("longitude", 0);
            ArrayList<Keyword> tem=savedInstanceState.getParcelableArrayList("data");
            if(tem.size()>0) {
                adapter.clearData();
                for (Keyword kw : tem) {
                    adapter.addItem(kw);

                }
            }
            message = getString(R.string.local) + localcity + "," + state + "*" + getString(R.string.source) + getSourceName(sourceID);
            messageView.setText(message);
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                landView();

            }
        } else
        //When app first start up and bundle ==null
        {
            sourceID = 0;
            if(SP.getString("flAddButtonColor","XXXXXX").equalsIgnoreCase("xxxxxx"))
            SP.edit().putString("flAddButtonColor", "#FF00FF").commit();

            if(SP.getString("flAddButtonColor","XXXXXX").equalsIgnoreCase("xxxxxx"))
                SP.edit().putString("flAddButtonColor", "#FF00FF").commit();

            localized();
        }


        messagShow();
        return view;
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("countryValue", country);
        savedInstanceState.putString("stateValue", state);
        savedInstanceState.putString("cityValue", city);
        savedInstanceState.putString("localcityValue", localcity);
        savedInstanceState.putString("keywordValue", keywordValue);
        savedInstanceState.putInt("sourceIDValue", sourceID);
        savedInstanceState.putDouble("longitude", longitude);
        savedInstanceState.putDouble("latitude", latitude);
        savedInstanceState.putParcelableArrayList("data",adapter.getData());

    }

    public void setKeyword(String kw) {
        keywordValue = kw;
    }

    protected void initiateDB(DatabaseHandler db) {
        Keyword kw = new Keyword(0, getString(R.string.library), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.grocery), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.airport), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.supermarket), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.bank), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.cnRestaurantText), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.italianrestaurant), 0);//system type=0;
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.fastfoodText), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.restaurant), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.solitaire), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.unblockedgame), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.games), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.mortgagecalculator), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.gasStationPrice), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.horscope), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.cheapflight), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.movies), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.twitter), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.facebook), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.calendar), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.calculator), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.news), 0);
        db.addKeyword(kw);
        kw = new Keyword(0, getString(R.string.weather), 0);
        db.addKeyword(kw);


    }

    private String getSourceName(int sourceID) {
        switch (sourceID) {
            case 0:
                return getString(R.string.googlesearch);
            case 1:
                return getString(R.string.yellowpage);
            case 2:
                return getString(R.string.googlemap);
            case 3:
                return getString(R.string.ebay);
            case 4:
                return getString(R.string.amazon);
            case 5:
                return getString(R.string.costco);
            case 6:
                return getString(R.string.youtube);
            case 7:  // This doesn't show up in Chinese edition
                return "百度";
        }
        return "";
    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            getHomeOP();
        }
    };

    public void getHomeOP() {

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            Intent intent = builder.build(getActivity());
            startActivityForResult(intent, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            ;
        } catch (GooglePlayServicesNotAvailableException e) {
            ;
        }
    }

    public void setEditStatus(boolean status) {
        editStatus = status;

    }

    public void setSelectionsRedBorder() {
        lvwSelections.setBackgroundColor(Color.parseColor("#ff007f"));
        textColorInt = Color.parseColor("#FFFFFF");
        adapter.notifyDataSetChanged();
    }

    public void setSelectionsNormal() {
        lvwSelections.setBackgroundColor(Color.parseColor(SP.getString("mainListviewbgColor", "#DFF7F4")));
        textColorInt = Color.parseColor(SP.getString("mainListviewftColor", "#000000"));
        adapter.notifyDataSetChanged();

    }

    private View.OnClickListener btnEditListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            editOP();

        }
    };

    public void editOP() {
        if (fm == null) {
            fm = getActivity().getSupportFragmentManager();
        }
        editStatus = true;
        appStatus = true;
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment, new EditFragment());//????
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            Place p = PlacePicker.getPlace(getActivity(), data);
            getLocalLL(p);
        }
    }

    private void getLocalLL(Place p) {
        String[] add = p.getAddress().toString().split(",");
        if (add == null || add.length < 3) {
            return;// need to do something here
        }
        LatLng localLL = p.getLatLng();
        latitude = localLL.latitude;
        longitude = localLL.longitude;
        country = add[add.length - 1];
        country = country.trim();

        state = add[add.length - 2];
        state = state.trim();
        if (state.length() > 2) {
            state = state.substring(0, 2);
        }
        city = add[add.length - 3];
        city = city.trim();

        localcity = city;

        messagShow();

        if (city.indexOf(" ") > 0) {
            city = city.replace(" ", "%20");
        }
    }

    private String changeName(String city0) {
        city0 = city0.trim();
        city0 = city0.replace(" ", "-");
        city0 = city0.replace(".", "-");
        city0 = city0.replace("'", "");
        return city0;
    }

    @Override
    public void onStart() {
        super.onStart();
        //When user requests system keywords reload or new installation
        DatabaseHandler db = new DatabaseHandler(getActivity());
        if (SP.getString("syskwreload", "NO").equalsIgnoreCase("YES")||SP.getString("syskwreload", "XXX").equalsIgnoreCase("XXX") ) {
            db.deleteKeywordByType("0");
            initiateDB(db);
            SP.edit().putString("syskwreload", "NO").commit(); // set to default no reload
            List<Keyword> allkeywords = db.getAllKeywords();
            adapter.clearData();//clean up before populating
            for (Keyword kw : allkeywords) {
                adapter.addItem(kw);

            }

        }else if(SP.getString("syskwreload", "XXX").equalsIgnoreCase("NO")){
            List<Keyword> allkeywords = db.getAllKeywords();
            adapter.clearData();//clean up before populating
            for (Keyword kw : allkeywords) {
                adapter.addItem(kw);

            }
        }

        db.close();



    }
    @Override
    public void onResume(){
        super.onResume();
        messagShow();
    }

    private AdapterView.OnItemClickListener lvwSelectionsClickListener = new AdapterView.OnItemClickListener() {


        @Override
        public void onItemClick(AdapterView<?> parent, final View view,
                                final int position, long id) {
            if (tcallback.isDeletable()) {

                final Keyword deletedItem = adapter.getItem(position);
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                removeItem(adapter.getItem(position));
                                Toast.makeText(getActivity(), deletedItem + getString(R.string.deleteMSG), Toast.LENGTH_LONG).show();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                tcallback.setSelectionsNormal();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.delete));
                builder.setIcon(R.drawable.common_google_signin_btn_icon_dark_focused);

                builder.setMessage(getString(R.string.areyousuredelete) + "\n " + deletedItem.get_keyword() + " ?").setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show();

                return;// app is in Editing situation
            }

            appStatus = true;

            keywordValue = ((Keyword) parent.getItemAtPosition(position)).toString();
            view.animate().setDuration(1000).alpha(0)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {

                            adapter.notifyDataSetChanged();
                            view.setAlpha(1);

                            if (client == null || !client.isConnected()) {
                                Toast.makeText(getActivity(), "Something wrong", Toast.LENGTH_LONG).show();
                                return;
                            }

                            guessCurrentPlace();
                        }
                    });
        }

    };

    protected void mainOP() {
        uri = null;
        intent = null;

        switch (sourceID) {
            case 2://google Map
                uri = Uri.parse("geo:" + latitude + "," + longitude + "?q=" + keywordValue);
                intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setPackage("com.google.android.apps.maps");//>>>>
                break;
            case 0://google search

               // if (country.equalsIgnoreCase("Canada") || country.equalsIgnoreCase("CA")|| country.equalsIgnoreCase("加拿大")) {
                    uri = Uri.parse("https://www.google.ca/search?q=" + keywordValue+"&oq="+keywordValue+"&aqs=mobile-gws-lite");

                //} else if (country.equalsIgnoreCase("United States") || country.equalsIgnoreCase("USA")|| country.equalsIgnoreCase("美国")) {
                //    uri = Uri.parse("https://www.google.com/search?q=" + keywordValue+"&oq="+keywordValue+"&aqs=mobile-gws-lite");
               // }


                intent = new Intent(Intent.ACTION_VIEW, uri);
                break;
            case 1://yellow page
                if (country.equalsIgnoreCase("Canada") || country.equalsIgnoreCase("CA")|| country.equalsIgnoreCase("加拿大")) {
                    uri = Uri.parse("http://www.yellowpages.ca/search/si/1/" + keywordValue + "/" + city + "%2c" + state);
                    intent = new Intent(Intent.ACTION_VIEW, uri);

                } else if (country.equalsIgnoreCase("United States") || country.equalsIgnoreCase("USA")|| country.equalsIgnoreCase("美国")) {
                    uri = Uri.parse("http://m.yp.com/search?search_term=" + keywordValue + "&search_type=category" + "&lat=" + latitude + "&lon=" + longitude);
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                }


                break;

            case 4://Amazon

                if (country.equalsIgnoreCase("Canada") || country.equalsIgnoreCase("CA")|| country.equalsIgnoreCase("加拿大")) {
                    uri = Uri.parse("https://www.amazon.ca/gp/aw/s/ref=nb_sb_noss?k=" + keywordValue);
                } else if (country.equalsIgnoreCase("United States") || country.equalsIgnoreCase("USA")|| country.equalsIgnoreCase("美国")) {
                    uri = Uri.parse("https://www.amazon.com/gp/aw/s/ref=is_s?k=" + keywordValue);
                }
                intent = new Intent(Intent.ACTION_VIEW, uri);

                break;
            case 3://ebay
                if (country.equalsIgnoreCase("Canada") || country.equalsIgnoreCase("CA")|| country.equalsIgnoreCase("加拿大")) {
                    uri = Uri.parse("http://www.ebay.ca/sch/i.html?_nkw=" + keywordValue);
                } else if (country.equalsIgnoreCase("United States") || country.equalsIgnoreCase("USA")| country.equalsIgnoreCase("美国")) {
                    uri = Uri.parse("http://www.ebay.com/sch/i.html?_nkw=" + keywordValue);
                }

                intent = new Intent(Intent.ACTION_VIEW, uri);

                break;
            case 5://cosco
                if (country.equalsIgnoreCase("Canada") || country.equalsIgnoreCase("CA")|| country.equalsIgnoreCase("加拿大")) {
                    uri = Uri.parse("http://www.costco.ca/CatalogSearch?keyword=" + keywordValue);

                } else if (country.equalsIgnoreCase("United States") || country.equalsIgnoreCase("USA")| country.equalsIgnoreCase("美国")) {
                    uri = Uri.parse("http://www.costco.com/CatalogSearch?keyword=" + keywordValue);
                }

                intent = new Intent(Intent.ACTION_VIEW, uri);

                break;
            case 6://youtube search

                uri = Uri.parse("https://www.youtube.com/results?search_query=" + keywordValue);
                intent = new Intent(Intent.ACTION_VIEW, uri);
                break;
            case 7://baidu search

                uri = Uri.parse("http://www.baidu.com/s?wd==" + keywordValue);
                intent = new Intent(Intent.ACTION_VIEW, uri);
                break;

        }

        if (intent == null) {
            Toast.makeText(getActivity(), "App failed to launch", Toast.LENGTH_LONG).show();
            return;

        } else {
            startActivity(intent);
        }
    }

    protected void toolOP(int which) {
        uri = null;
        intent = null;

        switch (which) {
            case 0://facebook
                uri = Uri.parse("https://m.facebook.com");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                break;
            case 1://twitter
                uri = Uri.parse("https://mobile.twitter.com/session/new");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                break;
            case 2://weather
                weatherOP();
                break;
            case 3://send email
                intent = new Intent(Intent.ACTION_SEND);
                intent.setType("plain/text");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
                intent.putExtra(Intent.EXTRA_SUBJECT, "");
                intent.putExtra(Intent.EXTRA_TEXT, "");
                startActivity(Intent.createChooser(intent, ""));
                return;
            case 4://send small message
                uri= Uri.parse("smsto:"+"");
                intent = new Intent(Intent.ACTION_SENDTO, uri);
                intent.putExtra("compose_mode", true);
                startActivity(intent);
                return;
            case 5://camera

                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                break;

            case 6://calculator
                ArrayList<HashMap<String,Object>> items =new ArrayList<HashMap<String,Object>>();
                PackageManager pm;
                pm = getActivity().getPackageManager();
                List<PackageInfo> packs = pm.getInstalledPackages(0);
                for (PackageInfo pi : packs) {
                    if( pi.packageName.toString().toLowerCase().contains("calcul")){
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("appName", pi.applicationInfo.loadLabel(pm));
                        map.put("packageName", pi.packageName);
                        items.add(map);
                    }
                }

                if(items.size()>=1){
                    String packageName = (String) items.get(0).get("packageName");
                    Intent i = pm.getLaunchIntentForPackage(packageName);
                    if (i != null)
                        startActivity(i);
                }
                else{
                    Toast.makeText(getActivity(), "You don't have appropriate calculator app installed", Toast.LENGTH_LONG).show();
                }

                return;
            case 7:
                uri = Uri.parse("http://www.cnn.com");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                break;
            case 8:
                uri = Uri.parse("http://www.cbc.ca/m/touch/");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                break;
            case 9:
                uri = Uri.parse("http://m.creaders.net");
                intent = new Intent(Intent.ACTION_VIEW, uri);
                break;

        }

        if (intent == null) {
            Toast.makeText(getActivity(), "App failed to launch", Toast.LENGTH_LONG).show();
            return;

        } else {
            startActivity(intent);
        }
    }

    public void weatherOP() {
        if (country.equalsIgnoreCase("Canada") || country.equalsIgnoreCase("CA")) {
            uri = Uri.parse("http://www.theweathernetwork.com/ca/weather/" + getPName(state) + "/" + changeName(city));
            intent = new Intent(Intent.ACTION_VIEW, uri);
        } else if (country.equalsIgnoreCase("United States") || country.equalsIgnoreCase("USA")) {
            uri = Uri.parse("http://www.theweathernetwork.com/us/weather/" + getPName(state) + "/" + changeName(city));
            intent = new Intent(Intent.ACTION_VIEW, uri);

        }
    }

    private String getPName(String code) {
        code = code.trim();

        switch (code) {
            case "ON":
                return "Ontario";
            case "QC":
                return "Quebec";
            case "NS":
                return "Nova-Scotia";
            case "NB":
                return "New-Brunswick";
            case "NL":
                return "newfoundland-and-labrador";
            case "MB":
                return "Manitoba";
            case "SK":
                return "saskatchewan";
            case "AB":
                return "Alberta";
            case "BC":
                return "British-Columbia";
            case "YT":
                return "Yukon";
            case "NT":
                return "northwest-territories";
            case "NU":
                return "Nunavut";
            case "NY":
                return "new-york";
            case "MI":
                return "michigan";
            case "CA":
                return "california";
            case "OH":
                return "ohio";
            case "NJ":
                return "new-jersey";
            case "DC":
                return "district-of-columbia";
            case "FL":
                return "florida";
            case "MA":
                return "massachusetts";
            case "ME":
                return "maine";
            case "VT":
                return "vermont";
            case "NH":
                return "new-hampsheire";
            case "CT":
                return "connecticut";
            case "PA":
                return "pennsylvania";
            case "MD":
                return "maryland";
            case "DE":
                return "Delaware";
            case "VA":
                return "virginia";
            case "WV":
                return "west-virginia";
            case "SC":
                return "south-carolina";
            case "NC":
                return "north-carolina";
            case "GA":
                return "georgia";
            case "IN":
                return "indiana";
            case "KY":
                return "kentucky";
            case "TN":
                return "tennessee";
            case "AL":
                return "alabama";
            case "RI":
                return "rhode-island";
            case "IL":
                return "illinois";
            case "MN":
                return "minnesota";
            case "IA":
                return "iowa";
            case "MO":
                return "missouri";
            case "AR":
                return "arkansas";
            case "MS":
                return "mississippi";
            case "LA":
                return "louisiana";
            case "ND":
                return "north-dakota";
            case "SD":
                return "south-dakota";
            case "NE":
                return "nebraska";
            case "OK":
                return "oklahoma";
            case "TX":
                return "texas";
            case "MT":
                return "montana";
            case "WY":
                return "wyoming";
            case "NM":
                return "new-mexico";
            case "ID":
                return "idaho";
            case "UT":
                return "utah";
            case "AZ":
                return "arizona";
            case "WA":
                return "washington";
            case "OR":
                return "oregon";
            case "NV":
                return "nevada";
            case "CO":
                return "colorado";
            case "KS":
                return "kansas";
            case "AK":
                return "alaska";
            case "HI":
                return "hawaii";

        }
        return "Unknown";

    }

    private void localized() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(client, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                        Place p = placeLikelihood.getPlace();
                        getLocalLL(p);

                        //When app first starts, don't run mainOP();
                        if (appStatus)
                            mainOP();

                        break;

                    }
                    likelyPlaces.release();
                }

            });

        }

    }


    private void guessCurrentPlace() {
        if (country.length() != 0) {
            mainOP();
            return;
        }

    }


    private class StableArrayAdapter extends ArrayAdapter<Keyword> {

        ArrayList<Keyword> mIdmap = new ArrayList<>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  ArrayList<Keyword> objects) {
            super(context, textViewResourceId, objects);
            mIdmap = objects;
        }

        public void setData(ArrayList<Keyword> data){
            mIdmap=data;
           // notifyDataSetChanged();
        }
        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            TextView textView = (TextView) view.findViewById(android.R.id.text1);

            /*YOUR CHOICE OF COLOR*/
            textView.setTextColor(textColorInt);

            return view;
        }

        public ArrayList<Keyword> getData() {
            return mIdmap;
        }
        public void clearData(){
            mIdmap.clear();
        }

        public void addItem(Keyword item) {
            mIdmap.add(0, item);
            notifyDataSetChanged();

        }

        public boolean removeItem(Keyword item) {
            deleteStatus = false;
            if (mIdmap.remove(item)) {
                DatabaseHandler db = new DatabaseHandler(getActivity());
                int n = db.deleteKeyword(item);
                db.close();
                notifyDataSetChanged();

                return true;
            } else {
                Toast.makeText(getActivity(), "Deletion Failed", Toast.LENGTH_LONG).show();
                return false;
            }


        }


        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
    public int getSize()
    {
        return adapter.getData().size();
    }
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void addItem(Keyword item) {
        adapter.addItem(item);
    }

    public boolean removeItem(Keyword item) {
        return adapter.removeItem(item);
    }

    public interface main2main {
        boolean isDeletable();

        void setRecordCount(int count);

        void setSelectionsRedBorder();

        void setSelectionsNormal();
    }

    private void landView() {

        String msg = localcity + "," + getSourceName(sourceID);
        char[] chars = msg.toCharArray();
        String text = "";
        for (int i = 0; i < chars.length; i++) {
            text += chars[i] + "\n";
        }

        messageView.setText(text);
        if (chars.length > 17) {
            messageView.setTextSize(10);
        }else
        if (chars.length > 20) {
            messageView.setTextSize(8);
        }else
        if (chars.length >=23) {
            messageView.setTextSize(6);
        }

    }

    private void messagShow() {
        messageView.setTextSize(12);
        message = getString(R.string.local) + localcity + "," + state + "*" + getString(R.string.source) + getSourceName(sourceID);
        messageView.setText(message);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            landView();
        }
    }

    public void setSourceID(int soureid) {
        sourceID = soureid;
        messagShow();
    }

    private main2main tcallback;

    public void onAttach(Context activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            tcallback = (main2main) activity;
            DatabaseHandler db = new DatabaseHandler(getActivity());
            tcallback.setRecordCount(db.getKeywordsCount());
            db.close();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

}
