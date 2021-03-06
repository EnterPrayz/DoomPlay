package com.perm.DoomPlay;

import android.content.Context;
import android.util.Log;

/*
 My device doesn't log without that;
 */

public class ExceptionLog implements Thread.UncaughtExceptionHandler
{
    private Thread.UncaughtExceptionHandler defaultHandler;
    private Context context;
    private static ExceptionLog exceptionLog;

    private ExceptionLog(Context context)
    {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.context = context;
    }



    public static void init(Context context)
    {
        if(exceptionLog == null)
            exceptionLog = new ExceptionLog(context);

        Thread.setDefaultUncaughtExceptionHandler(exceptionLog);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex)
    {
        Log.i("EXCEPTION",ex.toString());
        ex.printStackTrace();
        defaultHandler.uncaughtException(thread,ex);
    }
}
