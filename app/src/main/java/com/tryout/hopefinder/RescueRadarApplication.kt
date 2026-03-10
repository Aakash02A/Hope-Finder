package com.tryout.hopefinder

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for the Rescue Radar System.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class RescueRadarApplication : Application()
