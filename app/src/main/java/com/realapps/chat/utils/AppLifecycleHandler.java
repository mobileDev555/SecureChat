package com.realapps.chat.utils;

/**
 * Created by Prashant Sharma on 6/8/2018.
 * Core techies
 * prashant@coretechies.org
 */


import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import kotlin.jvm.internal.Intrinsics;


public final class AppLifecycleHandler implements ActivityLifecycleCallbacks, ComponentCallbacks2 {
    private boolean appInForeground;
    private final LifeCycleDelegate lifeCycleDelegate;

    public void onActivityPaused(@Nullable Activity p0) {
    }

    public void onActivityResumed(@Nullable Activity p0) {
        if (!this.appInForeground) {
            this.appInForeground = true;
            this.lifeCycleDelegate.onAppForegrounded();
        }

    }

    public void onActivityStarted(@Nullable Activity p0) {
    }

    public void onActivityDestroyed(@Nullable Activity p0) {
    }

    public void onActivitySaveInstanceState(@Nullable Activity p0, @Nullable Bundle p1) {
    }

    public void onActivityStopped(@Nullable Activity p0) {
    }

    public void onActivityCreated(@Nullable Activity p0, @Nullable Bundle p1) {
    }

    public void onLowMemory() {
    }

    public void onConfigurationChanged(@Nullable Configuration p0) {
    }

    public void onTrimMemory(int level) {
        if (level == 20 || level == 40) {
            this.appInForeground = false;
            this.lifeCycleDelegate.onAppBackgrounded();
        }

    }

    public AppLifecycleHandler(@NotNull LifeCycleDelegate lifeCycleDelegate) {
        super();
        Intrinsics.checkParameterIsNotNull(lifeCycleDelegate, "lifeCycleDelegate");
        this.lifeCycleDelegate = lifeCycleDelegate;
    }
}
