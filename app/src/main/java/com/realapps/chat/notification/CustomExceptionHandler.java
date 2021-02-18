package com.realapps.chat.notification;


import com.google.firebase.crash.FirebaseCrash;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by prashant.
 */

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {
    String stacktrace = "";

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        ex.printStackTrace(printWriter);
        stacktrace = result.toString();
        ex.printStackTrace();
        FirebaseCrash.report(new Exception(stacktrace));
    }
}
