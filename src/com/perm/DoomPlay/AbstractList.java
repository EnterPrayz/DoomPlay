package com.perm.DoomPlay;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import com.api.Account;
import com.api.KException;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*
 Base class for all classes which use ListView with Audio
 */
abstract class AbstractList extends AbstractControls
{
    ListView listView;
    ListsAdapter adapter;
    protected ArrayList<Audio> audios;
    static boolean isLoading = false;
    LinearLayout linearLoading;
    PlaylistDB playlistDB;

    static void startLiryctDialog(FragmentManager fragmentManager,String artist,String title)
    {
        LyricsDialog dialog = new LyricsDialog();
        Bundle bundle = new Bundle();
        bundle.putString(LyricsDialog.keyLyricsTitle,artist+" "+title);
        dialog.setArguments(bundle);
        dialog.show(fragmentManager,"tag");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        playlistDB = PlaylistDB.getInstance(this);
    }
    void markItem(int position, boolean withScroll)
    {
        if(adapter != null)
        {
            if(playingService != null && equalsCollections(playingService.audios, audios))
            {

                adapter.setMarkedItem(position);
                if(withScroll && SettingActivity.getPreferences(SettingActivity.keyScroll))
                    listView.smoothScrollToPosition(position);
            }
            else
                adapter.setMarkedItem(PlayingService.valueIncredible);
        }

    }
     static boolean equalsCollections(List<Audio> first, List<Audio> second)
     {
         if(first == null || second == null || first.size() != second.size())
             return false;

         for (int i = 0 ; i < first.size(); i++)
             if (!first.get(i).equals(second.get(i)))
                return false;

         return true;
     }

    @Override
    protected void trackChanged()
    {
        if(playingService != null)
            markItem(playingService.indexCurrentTrack,true);
    }

    @Override
    protected void onServiceBound() {
        super.onServiceBound();
        markItem(playingService.indexCurrentTrack,false);
    }

    void goFullScreen()
    {
        if(playingService.audios != null)
        {
            Intent intent = new Intent(getBaseContext(),FullPlaybackActivity.class);
            intent.putExtra(FileSystemActivity.keyMusic,playingService.audios);
            intent.setAction(FullPlaybackActivity.actionReturnFull);
            startActivity(intent);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.itemShowHide:
                showHide();
                return true;
            case R.id.itemEqualizer:
                startActivity(new Intent(this,EqualizerActivity.class));
                return true;
            case R.id.itemSleep:
                SleepDialog sleepDialog = new SleepDialog();
                sleepDialog.show(getSupportFragmentManager(),FullPlaybackActivity.tagSleepDialog);
                return true;
            case R.id.itemFullScreen:
                goFullScreen();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    final AdapterView.OnItemClickListener onItemTrackClick = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            onClickTrack(position);
        }
    };
    private final ActionMode.Callback callback = new ActionMode.Callback()
    {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {

            getMenuInflater().inflate(R.menu.action_vk,menu);
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

    void handleActionMode(int itemId,int position)
    {
        if(Utils.isOnline(getBaseContext()))
        {
            switch (itemId)
            {
                case  R.id.itemGetLiricks:
                    startLyricsDialog(getSupportFragmentManager(), audios.get(position).getLyrics_id());
                    break;
                case R.id.itemLike:
                    if(!MainScreenActivity.isRegister)
                        Toast.makeText(getBaseContext(),getResources().getString(R.string.please_sign_in),Toast.LENGTH_SHORT).show();
                    else
                        likeTrack(position);
                    break;
                case R.id.itemDislike:
                    if(!MainScreenActivity.isRegister)
                        Toast.makeText(getBaseContext(),getResources().getString(R.string.please_sign_in),Toast.LENGTH_SHORT).show();
                    else
                        dislikeTrack(position);
                    break;
                case R.id.itemDownload:
                    Intent downloadIntent = new Intent(this,DownloadingService.class);
                    downloadIntent.putExtra(DownloadingService.keyDownload,(Parcelable)audios.get(position))
                            .setAction(PlayingService.actionPlay);
                    startService(downloadIntent);
                    break;
                case R.id.itemMoveToAlbum:
                    if(!MainScreenActivity.isRegister)
                        Toast.makeText(getBaseContext(),getResources().getString(R.string.please_sign_in),Toast.LENGTH_SHORT).show();
                    else
                        moveToAlbum(getSupportFragmentManager(), audios.get(position).getAid());

                    break;
            }
        }
        else
        {
            Toast.makeText(getBaseContext(),getResources().getString(R.string.check_internet),Toast.LENGTH_SHORT).show();
        }
    }
    public static void waitMessage(Context context)
    {
        Toast.makeText(context,context.getResources().getString(R.string.please_wait),Toast.LENGTH_SHORT).show();
    }

    private static void moveToAlbum(FragmentManager manager, long aid)
    {
        AddTrackToAlbumDialog dialog = new AddTrackToAlbumDialog();
        Bundle bundle = new Bundle();
        bundle.putLong(AddTrackToAlbumDialog.keyDialogAlbum,aid);
        dialog.setArguments(bundle);
        dialog.show(manager,"tag");

    }


    void dislikeTrack(int position)
    {
        new AsyncTask<Integer, Void, Integer>()
        {
            @Override
            protected Integer doInBackground(Integer... params)
            {
                try
                {
                    MainScreenActivity.api.deleteAudio(audios.get(params[0]).getAid(), Account.account.user_id);

                } catch (IOException e)
                {
                    showException(e);
                    cancel(false);
                }
                catch (JSONException e)
                {
                    showException(e);
                    cancel(false);
                }
                catch (KException e)
                {
                    handleKException(e);
                    cancel(false);
                }
                return params[0];
            }

            @Override
            protected void onPostExecute(Integer position)
            {
                super.onPostExecute(position);
                audios.remove((int) position);
                TracksHolder.audiosVk = audios;
                adapter.changeData(audios);

                if(equalsCollections(playingService.audios,audios) && position == playingService.indexCurrentTrack)
                    playingService.playTrackFromList(playingService.indexCurrentTrack);
            }
        }.execute(position);

    }
    public static void startLyricsDialog(FragmentManager manager, long lid)
    {
        Bundle bundle = new Bundle();
        bundle.putLong(LyricsDialog.keyLyricsId, lid);
        LyricsDialog dialog = new LyricsDialog();
        dialog.setArguments(bundle);
        dialog.show(manager,"tag");
    }


    void likeTrack(int position)
    {
        new AsyncTask<Integer, Void, Void>()
        {
            @Override
            protected Void doInBackground(Integer... params)
            {
                try
                {
                    MainScreenActivity.api.addAudio(audios.get(params[0]).getAid(), audios.get(params[0]).getOwner_id());
                    if(TracksHolder.audiosVk != null)
                        TracksHolder.audiosVk.add(0,audios.get(params[0]));

                } catch (IOException e)
                {
                    showException(e);
                    cancel(false);
                }
                catch (JSONException e)
                {
                    showException(e);
                    cancel(false);
                } catch (KException e)
                {

                    handleKException(e);
                    cancel(false);
                }
                return null;

            }

        }.execute(position);

    }
    final AdapterView.OnItemLongClickListener onItemLongVkListener = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
        {
            startSupportActionMode(callback).setTag(position);
            return true;
        }
    };
    void onClickTrack(int position)
    {
        if(playingService.isLoadingTrack())
        {
            waitMessage(this);
            return;
        }

        intentService.putExtra(FullPlaybackActivity.keyIndex,position);
        intentService.putExtra(FullPlaybackActivity.keyService,audios);

        connectService();
        PlayingService.isPlaying = true;
        startService(intentService);

    }
    @Override
    protected void onClickActionBar()
    {
        if(equalsCollections(audios, playingService.audios) && Build.VERSION.SDK_INT >= 8)
            listView.smoothScrollToPosition(playingService.indexCurrentTrack);
    }
}
