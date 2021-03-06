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
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.api.Account;
import com.api.KException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

public class ListVkActivity extends AbstractList
{
    static String currentAction ;
    public static final String actionMyMusic ="actionMyMusic";
    public static final String actionMyAlbums = "actionMyAlbums";
    public static final String actionJust = "actionJust";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_vk_with_controlls);

        audios  = getIntent().getParcelableArrayListExtra(MainScreenActivity.keyOpenInListTrack);
        currentAction = getIntent().getAction();




        initializeUi();
        initializeAbstract();
        checkIsShown(savedInstanceState);

        if(currentAction.equals(actionMyMusic) && TracksHolder.audiosVk == null)
        {
            refreshAudios();
        }
        else if(currentAction.equals(actionMyMusic))
        {
            audios = TracksHolder.audiosVk;
            if(adapter == null)
            {
                adapter = new ListsAdapter(audios,getBaseContext());
                listView.setAdapter(adapter);
            }
            else
            {
                adapter.changeData(audios);
            }
        }
        else
        {
            adapter = new ListsAdapter(audios,getBaseContext());
            listView.setAdapter(adapter);
        }


    }

    @Override
    protected void onClickTrack(int position)
    {
        super.onClickTrack(position);
        PlayingService.isOnline = true;
    }

    private AsyncTask<Void,Void,Void> asyncTask;

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemRefresh:
                if(isLoading)
                    AbstractList.waitMessage(getBaseContext());
                else if(!MainScreenActivity.isRegister)
                    Toast.makeText(getBaseContext(),getResources().getString(R.string.please_sign_in),Toast.LENGTH_SHORT).show();
                else  if(!Utils.isOnline(getBaseContext()))
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
                else
                {
                    refreshAudios();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        if(asyncTask != null)
            asyncTask.cancel(false);
        super.onBackPressed();
    }


    @Override
    protected void onServiceAbstractConnected()
    {
        playingService.setOnLoadingTrackListener(new PlayingService.OnLoadingTrackListener()
        {
            //TODO: android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.
            //but i call it only in ui thread , is it a bug?

            @Override
            public void onLoadingTrackStarted()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        linearLoading.setVisibility(View.VISIBLE);
                    }
                });

            }

            @Override
            public void onLoadingTrackEnded()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        linearLoading.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    private void refreshAudios()
    {
        asyncTask = new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
                isLoading = true;
                linearLoading.setVisibility(View.VISIBLE);
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                try
                {
                    if(currentAction.equals(actionMyMusic))
                    {
                        audios = TracksHolder.audiosVk = MainScreenActivity.api.getAudio(Account.account.user_id,
                            null,null,SettingActivity.getPreference("countvkall"));

                    }

                    else if(currentAction.equals(actionMyAlbums))
                        audios =  MainScreenActivity.api.getAudio(null,
                                null,VkAlbumsActivity.albums.get(VkAlbumsActivity.currentAlbum).album_id,
                                SettingActivity.getPreference("countvkall"));

                } catch (IOException e) {
                    showException(e);
                    cancel(false);
                } catch (JSONException e)
                {
                    showException(e);
                    cancel(false);
                } catch (KException e) {

                    handleKException(e);
                    cancel(false);

                }
                return null;
            }

            @Override
            protected void onCancelled()
            {
                super.onCancelled();
                isLoading = false;
                linearLoading.setVisibility(View.GONE);
            }

            @Override
            protected void onPostExecute(Void aVoid)
            {
                super.onPostExecute(aVoid);
                isLoading = false;
                linearLoading.setVisibility(View.GONE);

                if(adapter == null)
                {
                    adapter = new ListsAdapter(audios,getBaseContext());
                    listView.setAdapter(adapter);
                }
                else
                {
                    adapter.changeData(audios);
                }

                markItem(PlayingService.indexCurrentTrack,false);
            }
        };
        asyncTask.execute();
    }

    private final ActionMode.Callback callback = new ActionMode.Callback()
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {

            if(currentAction.equals(actionMyMusic))
                getMenuInflater().inflate(R.menu.action_vk_my,menu);
            else
                getMenuInflater().inflate(R.menu.action_vk_list_album,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {return false; }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {

            handleActionMode(item.getItemId(), (Integer) mode.getTag());
            mode.finish();
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {}
    };

    private void initializeUi()
    {
        listView = (ListView)findViewById(R.id.listAllSongs);
        listView.setOnItemClickListener(onItemTrackClick);
        if(currentAction.equals(actionMyMusic) || currentAction.equals(actionMyAlbums))
        {
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
            {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
                {
                    startSupportActionMode(callback).setTag(position);
                    return true;
                }
            });
        }
        else
            listView.setOnItemLongClickListener(onItemLongVkListener);
        linearControls = (RelativeLayout)findViewById(R.id.linearControls);
        intentService = new Intent(this,PlayingService.class);
        intentService.setAction(PlayingService.actionOnline);
        imgPlay = (ImageView)findViewById(R.id.imagePlay) ;
        imgShuffle = (ImageView)findViewById(R.id.imageShuffle);
        imgRepeat = (ImageView)findViewById(R.id.imageRepeat);
        imgNext = (ImageView)findViewById(R.id.imageNext);
        imgPrevious = (ImageView)findViewById(R.id.imagePrevious);
        seekBar = (SeekBar)findViewById(R.id.seek_bar);
        textCurrentTime = (TextView)findViewById(R.id.textElapsed);
        textTotalTime = (TextView)findViewById(R.id.textDuration);
        linearLoading = (LinearLayout)findViewById(R.id.linearLoading);
    }
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
        ArrayList<Audio> tempTracks = intent.getParcelableArrayListExtra(MainScreenActivity.keyOpenInListTrack);

        if(tempTracks != null)
        {
            audios = tempTracks;
            adapter.changeData(audios);
        }
        currentAction  = intent.getAction();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        if(currentAction.equals(actionJust))
            getMenuInflater().inflate(R.menu.bar_list,menu);
        else
            getMenuInflater().inflate(R.menu.bar_vk_mymusic,menu);
        return true;
    }
}
