package com.example.image_picker

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.lifecycle.HiddenLifecycleReference
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** ImagePickerPlugin */
class ImagePickerPlugin: FlutterPlugin, MethodCallHandler,ActivityAware {
  inner class LifeCycleObserver internal constructor(private val thisActivity: Activity) :
    Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    override fun onCreate(owner: LifecycleOwner) {}
    override fun onStart(owner: LifecycleOwner) {}
    override fun onResume(owner: LifecycleOwner) {}
    override fun onPause(owner: LifecycleOwner) {}
    override fun onStop(owner: LifecycleOwner) {
      onActivityStopped(thisActivity)
    }

    override fun onDestroy(owner: LifecycleOwner) {
      onActivityDestroyed(thisActivity)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
      if (thisActivity === activity && activity.applicationContext != null) {
        (activity.applicationContext as Application)
          .unregisterActivityLifecycleCallbacks(
            this
          ) // Use getApplicationContext() to avoid casting failures
      }
    }

    override fun onActivityStopped(activity: Activity) {
      if (thisActivity === activity) {

      }
    }
  }

  inner class ActivityState(
    application: Application,
    activity: Activity,
    binaryMessenger: BinaryMessenger,
    handler: MethodCallHandler,
    binding: ActivityPluginBinding
  ){
    private var channel:MethodChannel?
    private var delegate:ImagePickerDelegate?
    private var activityBinding:ActivityPluginBinding?
    private var application:Application? = null
    private var activity: Activity?
    private var observer:LifeCycleObserver? = null
    private var lifecycle: Lifecycle? = null
    init {
      activityBinding = binding
      this.application = application
      this.activity = activity
      channel = MethodChannel(binaryMessenger, CHANNEL)
      observer = LifeCycleObserver(activity)
      channel!!.setMethodCallHandler(handler)
      delegate = ImagePickerDelegate(activity)
      activityBinding!!.addActivityResultListener(delegate!!)
      activityBinding!!.addRequestPermissionsResultListener(delegate!!)
      lifecycle = (activityBinding!!.lifecycle as HiddenLifecycleReference).lifecycle
      lifecycle!!.addObserver(observer!!)
    }

    fun getDelegate(): ImagePickerDelegate? {
      return delegate
    }

    fun getActivity():Activity?{
      return activity
    }

    fun release(){
      if (activityBinding != null) {
        activityBinding!!.removeActivityResultListener(delegate!!)
        activityBinding!!.removeRequestPermissionsResultListener(delegate!!)
        activityBinding = null
      }

      if (channel != null) {
        channel!!.setMethodCallHandler(null)
        channel = null
      }

      activity = null
      delegate = null
    }
  }

  private fun setup(
    messenger: BinaryMessenger,
    application: Application,
    activity: Activity,
    activityBinding: ActivityPluginBinding
  ) {
    activityState =
      ActivityState(application, activity, messenger, this, activityBinding)
  }

  private fun tearDown() {
    if (activityState != null) {
      activityState!!.release()
      activityState = null
    }
  }

  private lateinit var channel : MethodChannel
  companion object{
    const val CHANNEL = "image_picker";
  }
  var activityState:ActivityState? = null
  lateinit var flutterPluginBinding:FlutterPlugin.FlutterPluginBinding
  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    this.flutterPluginBinding = flutterPluginBinding
    print("onAttachedToEngine")
  }


  override fun onMethodCall(call: MethodCall, result: Result) {
   if(activityState != null && activityState!!.getActivity() != null) {
     when (call.method) {
       "pickImage" -> {
         activityState!!.getDelegate()?.selectImage(result, call)
       }
       "takePhoto" -> {
         activityState!!.getDelegate()?.takePhoto(result, call)
       }
       else -> {
         result.error("Error", "Error", "Error")
       }
     }
   }else {
     result.error("Error", "Error", "Error")
   }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    print("onDetachedFromEngine")
    channel.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    print("onAttachedToActivity")
    setup(flutterPluginBinding.binaryMessenger,
      flutterPluginBinding.applicationContext as Application,
      binding.activity,
      binding)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    print("onDetachedFromActivityForConfigChanges")
   onDetachedFromActivity()
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    print("onReattachedToActivityForConfigChanges")
    onAttachedToActivity(binding)
  }

  override fun onDetachedFromActivity() {
     tearDown()
  }
}
