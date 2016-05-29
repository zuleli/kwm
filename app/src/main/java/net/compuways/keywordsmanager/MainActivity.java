package net.compuways.keywordsmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.main2main {
    MainActivityFragment mf;
    private SharedPreferences SP;
    private ArrayList<String> toolspkg=new ArrayList();

    @Override
    public void setSelectionsNormal() {
        mf.setSelectionsNormal();
        mf.setDeletable(false);
    }

    @Override
    public void setSelectionsRedBorder() {
        mf.setSelectionsRedBorder();
        mf.setDeletable(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if (mf == null && savedInstanceState == null) {  // to make sure preferences settings don't interfere with savedstate
            mf = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
            mf.setSourceID(Integer.parseInt(SP.getString("source", "0")));// to be modified
        }

        final String lstr = SP.getString("language", "EN").trim();
        Locale locale = null;

        if (lstr.equalsIgnoreCase("zh")) {
            locale = new Locale("zh");
        } else {
            locale = new Locale("en");
        }
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());


    }


    @Override
    protected void onStart() {
        super.onStart();
        mf = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);


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

        switch (id) {
            case R.id.action_settings:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                if (mf.isDeletable()) {
                    mf.notallowed(builder);
                    return false;
                }
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_edit:
                builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle(getString(R.string.helphead)).setIcon(R.drawable.kwicon).setMessage(getString(R.string.helplines))
                        .setNeutralButton(getString(R.string.ok), null).setPositiveButton(getString(R.string.sendcomment), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("plain/text");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"keywordsmanager@gmail.com"});
                        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.commentsugestion));
                        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.yourcommenthere));
                        startActivity(Intent.createChooser(intent, getString(R.string.emailtitle)));
                    }
                }).show();
                return true;
            case R.id.action_source:
                if (mf.isDeletable()) {
                    mf.setDeletable(false);
                    setSelectionsNormal();
                } else {
                    mf.setDeletable(true);
                    setSelectionsRedBorder();
                }


                return true;
            case R.id.action_local:
                if (mf.isDeletable()) {
                    builder = new AlertDialog.Builder(MainActivity.this);
                    mf.notallowed(builder);
                    return false;
                }
                mf.getHomeOP();//locate the local
                return true;

            case R.id.action_apps:
                if (mf.isDeletable()) {
                    builder = new AlertDialog.Builder(MainActivity.this);
                    mf.notallowed(builder);
                    return false;
                }

                editapps();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }


    }

    private void editapps() {
        //GridLayout gridLayout=null;
        CustomDialog.Builder customBuilder = new
                CustomDialog.Builder(this).setLayoutID(R.layout.tools);

        final GridLayout gridLayout = customBuilder.getGridLayout();

        PackageManager p = getPackageManager();
        List<ApplicationInfo> packs = p.getInstalledApplications(0);
        int pn = packs.size();
        gridLayout.setColumnCount(4);
        gridLayout.setRowCount((int) Math.ceil(pn));
        int r = 0, c = 0;

        for (ApplicationInfo ap : packs) {

            if (ap.loadIcon(p).getIntrinsicWidth()>180) { // remove too big icon
                continue;
            }

            CheckBox cb = new CheckBox(this);
            if(checkPkgName(ap.packageName)){
                cb.setChecked(true);
            }
            GridLayout.Spec rowSpan = GridLayout.spec(r);
            GridLayout.Spec colspan = GridLayout.spec(c);
            GridLayout.LayoutParams gridParam = new GridLayout.LayoutParams(
                    rowSpan, colspan);
            gridLayout.addView(cb, gridParam);

            c++;
            ImageView icon = new ImageView(this);
            icon.setImageDrawable(ap.loadIcon(p));
           // icon.setLayoutParams(new ViewGroup.LayoutParams(100, 100));
            rowSpan = GridLayout.spec(r);
            colspan = GridLayout.spec(c);
            gridParam = new GridLayout.LayoutParams(
                    rowSpan, colspan);
            gridLayout.addView(icon, gridParam);

            c++;
            TextView pkname = new TextView((this));
           // name.setText(p.getApplicationLabel(ap));
            pkname.setText(ap.packageName);
            rowSpan = GridLayout.spec(r);
            colspan = GridLayout.spec(c);
            gridParam = new GridLayout.LayoutParams(
                    rowSpan, colspan);
            pkname.setVisibility(View.GONE);
            gridLayout.addView(pkname, gridParam);

            c++;
            TextView name = new TextView((this));
            // name.setText(p.getApplicationLabel(ap));
            name.setText(p.getApplicationLabel(ap));
            rowSpan = GridLayout.spec(r);
            colspan = GridLayout.spec(c);
            gridParam = new GridLayout.LayoutParams(
                    rowSpan, colspan);
            gridLayout.addView(name, gridParam);

            c++;
            if (c == 4) {
                c = 0;
                r++;
            }

        }


        String color = SP.getString("appssourcesBG", "marble6");
        customBuilder.setBackground(mf.getDrawable(color)).setPositiveButton("Update", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                process(gridLayout);
                dialog.dismiss();
            }
        });

        CustomDialog dia = customBuilder.create();


        dia.show();

    }
    private boolean checkPkgName(String pkgname){

        for(String name:toolspkg){
            if(name.equalsIgnoreCase(pkgname)){
                return true;
            }
        }

        return false;
    }
    @Override
    public ArrayList<String> getPkgs() {
        return toolspkg;
    }

    @Override
    public void setPkgs(ArrayList<String> pkgs) {
        toolspkg=pkgs;
    }

    private void process(GridLayout gridLayout) {
        toolspkg.clear();
        DatabaseHandler db = new DatabaseHandler(this);
        db.deleteToolItemByType("0");
        int size = gridLayout.getChildCount();
        for (int i = 0; i < size; i++) {
            View gridChild = gridLayout.getChildAt(i);

            if (gridChild instanceof CheckBox) {
                if(((CheckBox) gridChild).isChecked()){
                    i++;
                    i++;
                    String r=((TextView)gridLayout.getChildAt(i)).getText().toString();
                    toolspkg.add(r);

                    long n = db.addToolItem(0,r);
                    if(n<=0){
                        Toast.makeText(this, " Database Addition Problem", Toast.LENGTH_SHORT).show();
                    }

                }
            }

        }
        db.close();
    }

}
