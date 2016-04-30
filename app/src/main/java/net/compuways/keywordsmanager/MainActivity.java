package net.compuways.keywordsmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements EditFragment.listUpdate,MainActivityFragment.main2main {
    MainActivityFragment mf;
    private int recordcount=0;
    private boolean deletable=false;
    private  SharedPreferences SP;
    @Override
    public void setSelectionsNormal() {
        mf.setSelectionsNormal();
        deletable=false;
    }

    @Override
    public void setSelectionsRedBorder() {
        mf.setSelectionsRedBorder();
        deletable=true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if(mf==null && savedInstanceState==null) {  // to make sure preferences settings don't interfere with savedstate
            mf = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
            mf.setSourceID(Integer.parseInt(SP.getString("source", "0")));// to be modified
        }

        final String lstr=SP.getString("language","EN").trim();
        Locale locale =null;

        if(lstr.equalsIgnoreCase("zh")){
            locale=new Locale("zh");
        }else{
            locale=new Locale("en");
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                if (deletable) {
                    notallowed(alertDialog);

                    return;
                }


                alertDialog.setTitle(getString(R.string.newkeyword));
                alertDialog.setMessage(getString(R.string.enternewkeyword));

                final EditText input = new EditText(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setIcon(R.drawable.common_google_signin_btn_icon_dark_focused);

                alertDialog.setPositiveButton(getString(R.string.addlaunch),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                String rst = addOP(dialog, input);
                                if (rst != null) {
                                    mf.setKeyword(rst);
                                    mf.mainOP();
                                } else {
                                    Toast.makeText(MainActivity.this, " Something Wrong!", Toast.LENGTH_LONG).show();
                                }

                            }
                        });

                alertDialog.setNegativeButton(getString(R.string.cance),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                alertDialog.setNeutralButton(getString(R.string.justadd),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                addOP(dialog, input);
                            }
                        });

                alertDialog.show();
            }
        });

        FloatingActionButton sources = (FloatingActionButton) findViewById(R.id.sources);
        sources.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                if(deletable){
                    notallowed(builder);
                    return;
                }

                CharSequence items[]=null;

                if(lstr.equalsIgnoreCase("ZH")){
                    items=new CharSequence[] {getString(R.string.googlesearch),
                            getString(R.string.yellowpage),
                            getString(R.string.googlemap),
                            getString(R.string.ebay),
                            getString(R.string.amazon),
                            getString(R.string.costco),
                            getString(R.string.youtube),"百度"};
                }else{
                    items= new CharSequence[] { getString(R.string.googlesearch),
                            getString(R.string.yellowpage),
                            getString(R.string.googlemap),
                            getString(R.string.ebay),
                            getString(R.string.amazon),
                            getString(R.string.costco),
                            getString(R.string.youtube)};
                }


                builder.setTitle(getString(R.string.selectsource));
                builder.setIcon(R.drawable.common_google_signin_btn_icon_dark_focused);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mf.setSourceID(which);

                        if (which == 2) {
                            mf.mainOP();
                            return;
                        }
                    }
                });
                builder.setNegativeButton(getString(R.string.cance),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.show();

            }
        });

        FloatingActionButton tool = (FloatingActionButton) findViewById(R.id.tool);
        tool.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                if (deletable) {
                    notallowed(builder);

                    return;
                }


                CharSequence items[] = null;

                if (lstr.equalsIgnoreCase("ZH")) {
                    items = new CharSequence[]{getString(R.string.facebook),
                            getString(R.string.twitter),
                            getString(R.string.weather),
                            getString(R.string.email),
                            getString(R.string.sms),
                            getString(R.string.camera),
                            getString(R.string.calculator),"CNN News","CBC News","万维网"};
                } else {
                    items = new CharSequence[]{getString(R.string.facebook),
                            getString(R.string.twitter),
                            getString(R.string.weather),
                            getString(R.string.email),
                            getString(R.string.sms),
                            getString(R.string.camera),
                            getString(R.string.calculator),
                    "CNN News","CBC News"};
                }


                builder.setTitle(getString(R.string.selecttool));
                builder.setIcon(R.drawable.common_google_signin_btn_icon_dark_focused);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mf.toolOP(which);

                    }
                });
                builder.setNegativeButton(getString(R.string.cance),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                builder.show();

            }
        });

        int[][] states = new int[][] {
                new int[] {-android.R.attr.state_hovered}, // disabled
        };

        int[] colors = new int[] {
                Color.parseColor(SP.getString("flAddButtonColor","#FF00FF")),
        };

        ColorStateList myList = new ColorStateList(states, colors);
        fab.setBackgroundTintList(myList);

        colors = new int[] {
                Color.parseColor(SP.getString("flSourcesButtonColor","#008800")),
        };

        myList = new ColorStateList(states, colors);
        sources.setBackgroundTintList(myList);

        colors = new int[] {
                Color.parseColor(SP.getString("fltoolButtonColor","#0000FF")),
        };

        myList = new ColorStateList(states, colors);
        tool.setBackgroundTintList(myList);


    }
    private void notallowed(AlertDialog.Builder alertDialog){
        alertDialog.setIcon(R.drawable.common_google_signin_btn_icon_dark_focused);
        alertDialog.setTitle(getString(R.string.deletetitle)).setMessage(getString(R.string.smdeletemsg))
                .setNeutralButton("OK",null).show();
    }
    private String addOP(DialogInterface dialog,TextView input){
        if(input.getText()!=null && input.getText().length()>0){
            if(getRecordCount()>100){// need to be changed
                dialog.cancel();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Attention!").setMessage("Thank you for trying this application. \nYou have reached the maxium number of free edition" +
                        "\nThank you! ")
                        .setNeutralButton("OK",null).show();
                return null;
            }
            Keyword item=new Keyword(0,input.getText().toString(),1);
            addItem(item);
            DatabaseHandler db=new DatabaseHandler(MainActivity.this);
            long n=db.addKeyword(item);
            input.setText("");
            if(n<0){
                Toast.makeText(MainActivity.this, " Database addition has failed", Toast.LENGTH_LONG).show();
            }else{
                item.set_id(n);
                increment();
                Toast.makeText(this, item + " was added", Toast.LENGTH_LONG).show();

            }
            db.close();
            return item.toString();

        }else if(input.getText()!=null && input.getText().length()==0){
            Toast.makeText(MainActivity.this, " You didn't enter anything!", Toast.LENGTH_LONG).show();
        }

        return null;
    }
    @Override
    protected  void onStart(){
        super.onStart();
        mf = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

       //mf.adapter.notifyDataSetChanged();


    }


    @Override
    public void increment() {
        recordcount++;
    }

    @Override
    public void addItem(Keyword item) {
        mf.addItem(item);
    }

    @Override
    public void setRecordCount(int count) {
        recordcount=count;
    }


    @Override
    public void editStatus(boolean status) {

        mf.setEditStatus(status);
    }

    @Override
    public int getRecordCount() {
        return recordcount;
    }


    @Override
    public boolean isDeletable() {
        return deletable;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case R.id.action_settings:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                if(deletable){
                    notallowed(builder);
                    return false;
                }
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_edit:
                builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle(getString(R.string.helphead)).setIcon(R.drawable.common_google_signin_btn_icon_dark_focused).setMessage(getString(R.string.helplines))
                        .setNeutralButton(getString(R.string.ok),null).setPositiveButton(getString(R.string.sendcomment),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("plain/text");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"keywordsmanager@gmail.com"});
                        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.commentsugestion));
                        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.yourcommenthere));
                        startActivity(Intent.createChooser(intent,getString(R.string.emailtitle)));
                    }
                }).show();
                return true;
            case R.id.action_source:
                if(deletable){
                    deletable=false;
                    setSelectionsNormal();
                }else{
                    deletable=true;
                    setSelectionsRedBorder();
                }


                return true;
            case R.id.action_local:
                if(deletable){
                    builder = new AlertDialog.Builder(MainActivity.this);
                    notallowed(builder);
                    return false;
                }
                mf.getHomeOP();//locate the local
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }


    }

}
