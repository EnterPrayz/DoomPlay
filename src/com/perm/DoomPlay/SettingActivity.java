package com.perm.DoomPlay;

/*
 *    Copyright 2013 Vladislav Krot
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 *    You can contact me <DoomPlaye@gmail.com>
 */

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.util.Locale;

public class SettingActivity extends PreferenceActivity
{
    public static final String keyOnCall = "oncall";
    public static final String keyAfterCall = "aftercall";
    public static final String keyShortFocus = "shortfocus";
    public static final String keyLongFocus = "longfocus";
    public static final String keyScroll = "scroll";
    public static final String keyOnGain = "gainfocus";
    public static final String keyOnClickNotif = "notifreturn";
    public static final String keyShowControls = "hideoncreate";
    private static final int REQUEST_DOWNLOAD_FOLDER = 23516;
    private static final int REQUEST_ALBUMART_FOLDER = 83542;
    private static final int REQUEST_BEGINNING_FOLDER = 97482;


    public static boolean getPreferences(String key)
    {
         return PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance()).getBoolean(key,false);
    }
    public static int getPreference(String key)
    {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance()).getString(key,"666"));
    }


    Preference downloadFolderPref;
    Preference albumartsFolderPref;
    Preference beginningFolderPref;

    private void prepareLang()
    {
        String lang = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance()).getString("languages",
                Locale.getDefault().getLanguage());

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, null);
    }



    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        prepareLang();

        addPreferencesFromResource(R.xml.pref);

        findPreference("contact").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"doomplaye@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT, "subject of email");
                i.putExtra(Intent.EXTRA_TEXT   , "body of email");
                try
                {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                }
                catch (android.content.ActivityNotFoundException ex)
                {
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.doesnt_available), Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

        findPreference("github").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/quxey/doomPlaye"));
                startActivity(browserIntent);
                return true;
            }
        });
        findPreference("licenses").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
        {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                View view = inflater.inflate(R.layout.license,null);
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
                builder.setView(view).setTitle("licenses").create().show();
                return true;
            }
        });

        findPreference("languages").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                String languageToLoad ;
                if(newValue.equals("ru"))
                {
                     languageToLoad = "ru";
                }
                else
                {
                    languageToLoad = "en";
                }

                Locale locale = new Locale(languageToLoad);
                Locale.setDefault(locale);
                Configuration config = new Configuration();
                config.locale = locale;
                getBaseContext().getResources().updateConfiguration(config, null);

                Intent intent = new Intent(getBaseContext(),MainScreenActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
        });

        downloadFolderPref = findPreference("foldertracks");
        downloadFolderPref.setSummary(DownloadingService.getDownloadDir());
        downloadFolderPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Intent chooserIntent = new Intent(getBaseContext(), DirectoryChooserActivity.class);
                chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_NEW_DIR_NAME, "download");
                chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_INITIAL_DIRECTORY, FileSystemActivity.defaultRootFilePath);
                startActivityForResult(chooserIntent, REQUEST_DOWNLOAD_FOLDER);
                return  true;
            }
        });

        albumartsFolderPref = findPreference("folderalbumart");
        albumartsFolderPref.setSummary(AlbumArtGetter.getAlbumArtsDir());
        albumartsFolderPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Intent chooserIntent = new Intent(getBaseContext(), DirectoryChooserActivity.class);
                chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_NEW_DIR_NAME, "coverArts");
                chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_INITIAL_DIRECTORY,FileSystemActivity.defaultRootFilePath);

                startActivityForResult(chooserIntent, REQUEST_ALBUMART_FOLDER);
                return  true;
            }
        });

        beginningFolderPref = findPreference("beginningfolder");
        beginningFolderPref.setSummary(FileSystemActivity.getFileSystemDir());
        beginningFolderPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference)
            {
                Intent chooserIntent = new Intent(getBaseContext(), DirectoryChooserActivity.class);
                chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_NEW_DIR_NAME, "music");
                chooserIntent.putExtra(DirectoryChooserActivity.EXTRA_INITIAL_DIRECTORY, FileSystemActivity.defaultRootFilePath);

                startActivityForResult(chooserIntent, REQUEST_BEGINNING_FOLDER);
                return  true;
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == DirectoryChooserActivity.RESULT_CODE_DIR_SELECTED)
        {
            String path = data.getStringExtra(DirectoryChooserActivity.RESULT_SELECTED_DIR);

            if (requestCode == REQUEST_DOWNLOAD_FOLDER)
            {
                SharedPreferences.Editor editpr = PreferenceManager.getDefaultSharedPreferences(
                        getBaseContext()).edit().putString("foldertracks",path);
                editpr.commit();
                editpr.apply();

                downloadFolderPref.setSummary(path);
            }
            else if(requestCode == REQUEST_ALBUMART_FOLDER)
            {
                SharedPreferences.Editor editpr = PreferenceManager.getDefaultSharedPreferences(
                        getBaseContext()).edit().putString("folderalbumart",path);
                editpr.commit();
                editpr.apply();

                albumartsFolderPref.setSummary(path);
            }
            else if(requestCode == REQUEST_BEGINNING_FOLDER)
            {
                SharedPreferences.Editor editpr = PreferenceManager.getDefaultSharedPreferences(
                        getBaseContext()).edit().putString("beginningfolder",path);
                editpr.commit();
                editpr.apply();

                beginningFolderPref.setSummary(path);
            }
        }
    }
}

