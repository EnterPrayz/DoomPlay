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
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;

public class BigWidget extends AppWidgetProvider
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context,intent);

        if(intent.getAction().equals(SmallWidget.actionUpdateWidget))
            updateWidget(context,(Audio)intent.getParcelableExtra(SmallWidget.EXTRA_AUDIO),
                    intent.getIntExtra(SmallWidget.EXTRA_SIZE,0));

    }
    private static void updateWidget(Context context,Audio audio,int size)
    {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_big);

        views.setTextViewText(R.id.widgetlTitle, audio.getTitle());
        views.setTextViewText(R.id.widgetArtist, audio.getArtist());
        views.setTextViewText(R.id.widgetCount,String.valueOf(PlayingService.indexCurrentTrack + 1)+ "/" +String.valueOf(size));

        Bitmap cover = AlbumArtGetter.getBitmapFromStore(audio.getAid(),context);
        if (cover != null)
        {
            //TODO: java.lang.IllegalArgumentException: RemoteViews for widget update exceeds
            // maximum bitmap memory usage (used: 3240000, max: 2304000)
            // The total memory cannot exceed that required to fill the device's screen once
            try
            {
                views.setImageViewBitmap(R.id.widgetAlbum, cover);
            }
            catch(IllegalArgumentException e)
            {
                Bitmap tempBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fallback_cover);
                views.setImageViewBitmap(R.id.widgetAlbum,tempBitmap);
                tempBitmap.recycle();
            }
            finally {
                cover.recycle();
            }

        }
        else
        {
            Bitmap tempBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.fallback_cover);
            views.setImageViewBitmap(R.id.widgetAlbum,tempBitmap);
            tempBitmap.recycle();
        }

        int playButton = PlayingService.isPlaying ? R.drawable.pause : R.drawable.play;
        int shuffleBtn = PlayingService.isShuffle ? R.drawable.shuffle_enable :  R.drawable.shuffle_disable;
        int loopBtn = PlayingService.isLoop ? R.drawable.repeat_enable : R.drawable.repeat_disable;

        views.setImageViewResource(R.id.imagePlay, playButton);
        views.setImageViewResource(R.id.imageShuffle, shuffleBtn);
        views.setImageViewResource(R.id.imageRepeat, loopBtn);

        ComponentName componentService = new ComponentName(context,PlayingService.class);

        Intent intentPlay = new Intent(PlayingService.actionPlay);
        intentPlay.setComponent(componentService);
        views.setOnClickPendingIntent(R.id.imagePlay, PendingIntent.getService(context, 0, intentPlay, 0));

        Intent intentNext = new Intent(PlayingService.actionNext);
        intentNext.setComponent(componentService);
        views.setOnClickPendingIntent(R.id.imageNext, PendingIntent.getService(context, 0, intentNext, 0));

        Intent intentPrevious = new Intent(PlayingService.actionPrevious);
        intentPrevious.setComponent(componentService);
        views.setOnClickPendingIntent(R.id.imagePrevious, PendingIntent.getService(context, 0, intentPrevious, 0));

        Intent intentShuffle = new Intent(PlayingService.actionShuffle);
        intentShuffle.setComponent(componentService);
        views.setOnClickPendingIntent(R.id.imageShuffle, PendingIntent.getService(context, 0, intentShuffle, 0));

        Intent intentLoop = new Intent(PlayingService.actionLoop);
        intentLoop.setComponent(componentService);
        views.setOnClickPendingIntent(R.id.imageRepeat, PendingIntent.getService(context, 0, intentLoop, 0));


        AppWidgetManager manager = AppWidgetManager.getInstance(context);

        final int ids[] = manager.getAppWidgetIds(new ComponentName(context,BigWidget.class));

        for (int widgetID : ids)
            manager.updateAppWidget(widgetID, views);

    }
}
