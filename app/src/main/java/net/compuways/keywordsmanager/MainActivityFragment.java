package net.compuways.keywordsmanager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    private Button btnTools, btnSources, btnAdKW;

    private String message = "", keywordValue = "", localcity = "", country = "", state = "", city = "";
    //localcity is the name of city, city is search string, may contain "%2c" character
    private int PLACE_PICKER_REQUEST = 1, sourceID = 0, textColorInt = -1;
    private boolean appStatus = false, editStatus = false, deleteStatus = false;
    private double latitude = 0.0, longitude = 0.0;
    private int recordcount = 0;
    private boolean deletable = false;
    private LinearLayout linearLayout;


    private final ArrayList<Keyword> list = new ArrayList<Keyword>();
    protected StableArrayAdapter adapter;
    private GoogleApiClient client;
    private Uri uri = null;
    private Intent intent = null;
    private FragmentManager fm;
    private SharedPreferences SP;
    final Context context = getActivity();

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
        linearLayout = (LinearLayout) view.findViewById(R.id.linerLayout);
        btnTools = (Button) view.findViewById(R.id.btnTools);
        btnSources = (Button) view.findViewById(R.id.btnSources);
        btnAdKW = (Button) view.findViewById(R.id.btnAdkw);
        btnTools.setOnClickListener(btnToolsListener);
        btnSources.setOnClickListener(btnSourcesListener);
        btnAdKW.setOnClickListener(btnAdKWListener);

        adapter = new StableArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_1, list);
        lvwSelections.setAdapter(adapter);
        lvwSelections.setOnItemClickListener(lvwSelectionsClickListener);
        lvwSelections.setItemsCanFocus(true);

        SP = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        textColorInt = Color.parseColor(SP.getString("mainListviewftColor", "#000000"));
        String color = SP.getString("mainListviewbgColor", "wood");
        String lstr = SP.getString("language", "EN").trim();

        if (color.contains("wood")) {
            defaultBG(lstr,color);

        } else if (color.contains("marble")) {
            linearLayout.setBackground(ContextCompat.getDrawable(getActivity(), getDrawable(color)));
            lvwSelections.setBackground(ContextCompat.getDrawable(getActivity(), getDrawable(color)));
            if (lstr.equalsIgnoreCase("ZH")) {
                btnTools.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.wm_tools_cn));
                btnSources.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.wm_sources_cn));
                btnAdKW.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.wm_adkw_cn));
            } else {
                btnTools.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.wm_tools));
                btnSources.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.wm_sources));
                btnAdKW.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.wm_adkw));
            }
        } else {
            defaultBG(lstr,color);
        }


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
            ArrayList<Keyword> tem = savedInstanceState.getParcelableArrayList("data");
            if (tem.size() > 0) {
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
            localized();
        }
        messagShow();
        return view;
    }

    private void defaultBG(String lstr,String color) {
        linearLayout.setBackground(ContextCompat.getDrawable(getActivity(), getDrawable(color)));
        lvwSelections.setBackground(ContextCompat.getDrawable(getActivity(),getDrawable(color)));
        if (lstr.equalsIgnoreCase("ZH")) {
            btnTools.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.yw_tools_cn));
            btnSources.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.yw_sources_cn));
            btnAdKW.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.yw_adkw_cn));
        } else {
            btnTools.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ywoodtools));
            btnSources.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ywoodsources));
            btnAdKW.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.ywoodkw));
        }
    }

    public boolean isDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    private String nameformat(String name){
        name=name.replace("Google","").trim();
        if(name.length()<=11){
            return name;
        }else{
            int index=name.lastIndexOf(" ");
            if(index<0){
                name=name.substring(0,10)+"\n"+name.substring(10);
            }else if(index>10){
                name=name.replace(" ","");
                name=name.substring(0,10)+"\n"+name.substring(10);
            }else{
                name=name.substring(0,index)+"\n"+name.substring(index+1);
            }
        }

        return name;
    }
    private View.OnClickListener btnToolsListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            if (deletable) {
                notallowed(builder);
                return;
            }



            final ArrayList<String> toolspkg = tcallback.getPkgs();

            CustomDialog.Builder customBuilder = new
                    CustomDialog.Builder(getActivity()).setLayoutID(R.layout.tools);

            final GridLayout gridLayout = customBuilder.getGridLayout();

            final PackageManager p = getActivity().getPackageManager();
            int pn = toolspkg.size();
            gridLayout.setColumnCount(4);
            gridLayout.setRowCount((int) Math.ceil(pn / 2.0));
            int r = 0, c = 0;

            for (int i = 0; i < pn; i++) {
                try {
                    final ApplicationInfo ap = p.getApplicationInfo(toolspkg.get(i), 0);
                    ImageView icon = new ImageView(getActivity());
                    icon.setImageDrawable(ap.loadIcon(p));

                    LinearLayout ll=new LinearLayout(getActivity());
                    ll.setOrientation(LinearLayout.VERTICAL);
                    ViewGroup.LayoutParams pa=new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                    icon.setLayoutParams(pa);

                    GridLayout.Spec rowSpan = GridLayout.spec(r);
                    GridLayout.Spec colspan = GridLayout.spec(c);
                    GridLayout.LayoutParams gridParam = new GridLayout.LayoutParams(
                            rowSpan, colspan);ll.addView(icon);

                    if(SP.getString("iconname","ON").equalsIgnoreCase("ON")) {
                        TextView txt = new TextView(getActivity());
                        String name = nameformat(p.getApplicationLabel(ap).toString());
                        ;
                        txt.setText(name);
                        txt.setLayoutParams(new ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        ll.addView(txt);
                        gridParam.setMargins(10, 0, 10, 0);
                    }else{
                        gridParam.setMargins(20, 20, 20, 20);
                    }

                    gridLayout.addView(ll, gridParam);

                    final int ii = i;
                    icon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent launchIntent = p.getLaunchIntentForPackage(toolspkg.get(ii));
                            if (launchIntent != null) {
                                startActivity(launchIntent);
                            } else {
                                Toast.makeText(getActivity(), p.getApplicationLabel(ap) + " doesn't work", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    c++;
                    if (c == 4) {
                        c = 0;
                        r++;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    Toast.makeText(getActivity(), "Application Name Not Found", Toast.LENGTH_SHORT).show();
                }

            }
            String color = SP.getString("toolButtonBG", "wood4");
            customBuilder.setBackground(getDrawable(color)).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            CustomDialog dia = customBuilder.create();
            dia.show();

        }
    };
    public int getDrawable(String bg){
        switch(bg){
            case "wood":
                return R.drawable.wood;
            case "wood2":
                return R.drawable.wood2;
            case "wood3":
                return R.drawable.wood3;
            case "wood4":
                return R.drawable.wood4;
            case "marble":
                return R.drawable.marble;
            case "marble2":
                return R.drawable.marble2;
            case "marble3":
                return R.drawable.marble3;
            case "marble4":
                return R.drawable.marble4;
            case "marble5":
                return R.drawable.marble5;
            case "marble6":
                return R.drawable.marble6;
            case "marble7":
                return R.drawable.marble7;
            case "carpet":
                return R.drawable.carpet;
            case "carpet2":
                return R.drawable.carpet2;
            case "carpet3":
                return R.drawable.carpet3;
            case "carpet4":
                return R.drawable.carpet4;
            case "carpet5":
                return R.drawable.carpet5;
            case "carpet6":
                return R.drawable.carpet6;

        }

        return R.drawable.wood;
    }
    private View.OnClickListener btnSourcesListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            if (deletable) {
                notallowed(builder);
                return;
            }

            CustomDialog.Builder customBuilder = new
                    CustomDialog.Builder(getActivity()).setLayoutID(R.layout.tools);

            final GridLayout gridLayout = customBuilder.getGridLayout();


            gridLayout.setColumnCount(2);
            gridLayout.setRowCount(4);

            ImageView icon = new ImageView(getActivity());
            icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.gsearch));
            //icon.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
            GridLayout.Spec rowSpan = GridLayout.spec(0);
            GridLayout.Spec colspan = GridLayout.spec(0);
            GridLayout.LayoutParams gridParam = new GridLayout.LayoutParams(
                    rowSpan, colspan);
            gridParam.setMargins(25, 25, 25, 25);
            gridLayout.addView(icon, gridParam);
            icon.setOnClickListener(gsearchListener);

            icon = new ImageView(getActivity());
            icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.yp));
            //  icon.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
            rowSpan = GridLayout.spec(0);
            colspan = GridLayout.spec(1);
            gridParam = new GridLayout.LayoutParams(
                    rowSpan, colspan);
            gridParam.setMargins(25, 25, 25, 25);
            gridLayout.addView(icon, gridParam);
            icon.setOnClickListener(ypListener);

            icon = new ImageView(getActivity());
            icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.googlemaps));
            // icon.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
            rowSpan = GridLayout.spec(1);
            colspan = GridLayout.spec(0);
            gridParam = new GridLayout.LayoutParams(
                    rowSpan, colspan);
            gridParam.setMargins(25, 25, 25, 25);
            gridLayout.addView(icon, gridParam);
            icon.setOnClickListener(gmapListener);

            icon = new ImageView(getActivity());
            icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.youtube));
            //  icon.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
            rowSpan = GridLayout.spec(1);
            colspan = GridLayout.spec(1);
            gridParam = new GridLayout.LayoutParams(
                    rowSpan, colspan);
            gridParam.setMargins(25, 25, 25, 25);
            gridLayout.addView(icon, gridParam);
            icon.setOnClickListener(youtubeListener);

            icon = new ImageView(getActivity());
            icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ebay));
            //  icon.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
            rowSpan = GridLayout.spec(2);
            colspan = GridLayout.spec(0);
            gridParam = new GridLayout.LayoutParams(
                    rowSpan, colspan);
            gridParam.setMargins(25, 25, 25, 25);
            gridLayout.addView(icon, gridParam);
            icon.setOnClickListener(ebayListener);

            icon = new ImageView(getActivity());
            icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.amazon));
            //  icon.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
            rowSpan = GridLayout.spec(2);
            colspan = GridLayout.spec(1);
            gridParam = new GridLayout.LayoutParams(
                    rowSpan, colspan);
            gridParam.setMargins(25, 25, 25, 25);
            gridLayout.addView(icon, gridParam);
            icon.setOnClickListener(amazonListener);

            icon = new ImageView(getActivity());
            icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.costco));
            // icon.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
            rowSpan = GridLayout.spec(3);
            colspan = GridLayout.spec(0);
            gridParam = new GridLayout.LayoutParams(
                    rowSpan, colspan);
            gridParam.setMargins(25, 25, 25, 25);
            icon.setOnClickListener(costcoListener);
            gridLayout.addView(icon, gridParam);


            if (SP.getString("language", "EN").trim().equalsIgnoreCase("ZH")) {
                icon = new ImageView(getActivity());
                icon.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.baidu));
                // icon.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
                rowSpan = GridLayout.spec(3);
                colspan = GridLayout.spec(1);
                gridParam = new GridLayout.LayoutParams(
                        rowSpan, colspan);
                gridLayout.addView(icon, gridParam);
                gridParam.setMargins(25, 25, 25, 25);
                icon.setOnClickListener(baiduListener);
            }
            String color = SP.getString("sourceButtonBG", "wood2");
            customBuilder.setBackground(getDrawable(color)).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            CustomDialog dia = customBuilder.create();


            dia.show();


        }
    };
    private View.OnClickListener gsearchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setSourceID(0);
        }
    };
    private View.OnClickListener ypListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setSourceID(1);
        }
    };
    private View.OnClickListener gmapListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setSourceID(2);
        }
    };
    private View.OnClickListener youtubeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setSourceID(6);
        }
    };
    private View.OnClickListener ebayListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setSourceID(3);
        }
    };
    private View.OnClickListener amazonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setSourceID(4);
        }
    };
    private View.OnClickListener costcoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setSourceID(5);
        }
    };
    private View.OnClickListener baiduListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setSourceID(7);
        }
    };

    private View.OnClickListener btnAdKWListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
            if (deletable) {
                notallowed(alertDialog);

                return;
            }

            CustomDialog.Builder customBuilder = new
                    CustomDialog.Builder(getActivity()).setLayoutID(R.layout.dialog);
            final TextView input = customBuilder.getEditText();

            String color = SP.getString("addButtonBG", "marble7");
            customBuilder.setBackground(getDrawable(color)).setPositiveButton(getResources().getString(R.string.addlaunch), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String rst = addOP(dialog, input);
                    if (rst != null) {
                        setKeyword(rst);
                        mainOP();
                    } else {
                        Toast.makeText(getActivity(), " Something Wrong!", Toast.LENGTH_LONG).show();
                    }

                    dialog.dismiss();
                }
            }).setNeutralButton(getResources().getString(R.string.justadd), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    addOP(dialog, input);
                    dialog.dismiss();
                }
            }).setNegativeButton(getResources().getString(R.string.cance), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            }).create().show();



        }
    };


    public void increment() {
        recordcount++;
    }

    public void notallowed(AlertDialog.Builder alertDialog) {
        alertDialog.setIcon(R.drawable.kwicon);
        alertDialog.setTitle(getString(R.string.deletetitle)).setMessage(getString(R.string.smdeletemsg))
                .setNeutralButton("OK", null).show();
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
        savedInstanceState.putParcelableArrayList("data", adapter.getData());

    }

    private String addOP(DialogInterface dialog, TextView input) {
        if (input.getText() != null && input.getText().length() > 0) {
            if (recordcount > 100) {// need to be changed
                dialog.cancel();
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Attention!").setMessage("Thank you for trying this application. \nYou have reached the maxium number of free edition" +
                        "\nThank you! ")
                        .setNeutralButton("OK", null).show();
                return null;
            }

            Keyword item = new Keyword(0, input.getText().toString(), 1);
            addItem(item);
            DatabaseHandler db = new DatabaseHandler(getActivity());
            long n = db.addKeyword(item);
            input.setText("");
            if (n < 0) {
                Toast.makeText(getActivity(), " Database addition has failed", Toast.LENGTH_LONG).show();
            } else {
                item.set_id(n);
                increment();
                Toast.makeText(getActivity(), item + " was added", Toast.LENGTH_LONG).show();

            }
            db.close();
            return item.toString();

        } else if (input.getText() != null && input.getText().length() == 0) {
            Toast.makeText(getActivity(), " You didn't enter anything!", Toast.LENGTH_LONG).show();
        }

        return null;
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

        PackageManager p = getActivity().getPackageManager();
        List<ApplicationInfo> packs = p.getInstalledApplications(0);
        int pn = packs.size();

        for (ApplicationInfo ap : packs) {
            String pkname=ap.packageName.toLowerCase();
            String name=p.getApplicationLabel(ap).toString().toLowerCase();
            if(name.contains("facebook") ||
                    name.contains("twitter") ||
                    name.contains("weather") ||
                    name.contains("calcul") ||
                    name.contains("calen") ||
                    name.contains("youtube")||
                    name.contains("message") ||
                    name.contains("clock") ||
                    name.contains("mail") ||
                    name.contains("dialer") ||
                    name.contains("news") ||
                    name.contains("maps") ||
                    name.contains("camera")){
                tcallback.getPkgs().add(pkname);
                db.addToolItem(0,pkname);
            }
        }

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


    public void setSelectionsRedBorder() {
        lvwSelections.setBackgroundColor(Color.parseColor("#ff007f"));
        textColorInt = Color.parseColor("#FFFFFF");
        adapter.notifyDataSetChanged();
    }

    public void setSelectionsNormal() {

        String color = SP.getString("mainListviewbgColor", "wood");
        lvwSelections.setBackground(ContextCompat.getDrawable(getActivity(),getDrawable(color)));
        textColorInt = Color.parseColor(SP.getString("mainListviewftColor", "#000000"));
        adapter.notifyDataSetChanged();

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
        if (SP.getString("syskwreload", "NO").equalsIgnoreCase("YES") || SP.getString("syskwreload", "XXX").equalsIgnoreCase("XXX")) {
            db.deleteKeywordByType("0");
            initiateDB(db);
            SP.edit().putString("syskwreload", "NO").commit(); // set to default no reload
            List<Keyword> allkeywords = db.getAllKeywords();
            adapter.clearData();//clean up before populating
            for (Keyword kw : allkeywords) {
                adapter.addItem(kw);

            }
            tcallback.setPkgs(db.getAllToolItems(0));

        } else if (SP.getString("syskwreload", "XXX").equalsIgnoreCase("NO")) {
            List<Keyword> allkeywords = db.getAllKeywords();
            adapter.clearData();//clean up before populating
            for (Keyword kw : allkeywords) {
                adapter.addItem(kw);

            }
            tcallback.setPkgs(db.getAllToolItems(0));
        }

        db.close();


    }

    @Override
    public void onResume() {
        super.onResume();
        messagShow();
    }

    private AdapterView.OnItemClickListener lvwSelectionsClickListener = new AdapterView.OnItemClickListener() {


        @Override
        public void onItemClick(AdapterView<?> parent, final View view,
                                final int position, long id) {
            if (deletable) {

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
                uri = Uri.parse("https://www.google.ca/search?q=" + keywordValue + "&oq=" + keywordValue + "&aqs=mobile-gws-lite");

                //} else if (country.equalsIgnoreCase("United States") || country.equalsIgnoreCase("USA")|| country.equalsIgnoreCase("美国")) {
                //    uri = Uri.parse("https://www.google.com/search?q=" + keywordValue+"&oq="+keywordValue+"&aqs=mobile-gws-lite");
                // }


                intent = new Intent(Intent.ACTION_VIEW, uri);
                break;
            case 1://yellow page
                if (country.equalsIgnoreCase("Canada") || country.equalsIgnoreCase("CA") || country.equalsIgnoreCase("加拿大")) {
                    uri = Uri.parse("http://www.yellowpages.ca/search/si/1/" + keywordValue + "/" + city + "%2c" + state);
                    intent = new Intent(Intent.ACTION_VIEW, uri);

                } else if (country.equalsIgnoreCase("United States") || country.equalsIgnoreCase("USA") || country.equalsIgnoreCase("美国")) {
                    uri = Uri.parse("http://m.yp.com/search?search_term=" + keywordValue + "&search_type=category" + "&lat=" + latitude + "&lon=" + longitude);
                    intent = new Intent(Intent.ACTION_VIEW, uri);
                }


                break;

            case 4://Amazon

                if (country.equalsIgnoreCase("Canada") || country.equalsIgnoreCase("CA") || country.equalsIgnoreCase("加拿大")) {
                    uri = Uri.parse("https://www.amazon.ca/gp/aw/s/ref=nb_sb_noss?k=" + keywordValue);
                } else if (country.equalsIgnoreCase("United States") || country.equalsIgnoreCase("USA") || country.equalsIgnoreCase("美国")) {
                    uri = Uri.parse("https://www.amazon.com/gp/aw/s/ref=is_s?k=" + keywordValue);
                }
                intent = new Intent(Intent.ACTION_VIEW, uri);

                break;
            case 3://ebay
                if (country.equalsIgnoreCase("Canada") || country.equalsIgnoreCase("CA") || country.equalsIgnoreCase("加拿大")) {
                    uri = Uri.parse("http://www.ebay.ca/sch/i.html?_nkw=" + keywordValue);
                } else if (country.equalsIgnoreCase("United States") || country.equalsIgnoreCase("USA") | country.equalsIgnoreCase("美国")) {
                    uri = Uri.parse("http://www.ebay.com/sch/i.html?_nkw=" + keywordValue);
                }

                intent = new Intent(Intent.ACTION_VIEW, uri);

                break;
            case 5://cosco
                if (country.equalsIgnoreCase("Canada") || country.equalsIgnoreCase("CA") || country.equalsIgnoreCase("加拿大")) {
                    uri = Uri.parse("http://www.costco.ca/CatalogSearch?keyword=" + keywordValue);

                } else if (country.equalsIgnoreCase("United States") || country.equalsIgnoreCase("USA") | country.equalsIgnoreCase("美国")) {
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
                uri = Uri.parse("smsto:" + "");
                intent = new Intent(Intent.ACTION_SENDTO, uri);
                intent.putExtra("compose_mode", true);
                startActivity(intent);
                return;
            case 5://camera

                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                break;

            case 6://calculator
                ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
                PackageManager pm;
                pm = getActivity().getPackageManager();
                List<PackageInfo> packs = pm.getInstalledPackages(0);
                for (PackageInfo pi : packs) {
                    if (pi.packageName.toString().toLowerCase().contains("calcul")) {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put("appName", pi.applicationInfo.loadLabel(pm));
                        map.put("packageName", pi.packageName);
                        items.add(map);
                    }
                }

                if (items.size() >= 1) {
                    String packageName = (String) items.get(0).get("packageName");
                    Intent i = pm.getLaunchIntentForPackage(packageName);
                    if (i != null)
                        startActivity(i);
                } else {
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

        public void setData(ArrayList<Keyword> data) {
            mIdmap = data;
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

        public void clearData() {
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

    public int getSize() {
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
        //  boolean isDeletable();

        //  void setRecordCount(int count);
        ArrayList<String> getPkgs();
        void setPkgs(ArrayList<String> pkgs);
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
        } else if (chars.length > 20) {
            messageView.setTextSize(8);
        } else if (chars.length >= 23) {
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
            recordcount = (db.getKeywordsCount());
            db.close();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

}
