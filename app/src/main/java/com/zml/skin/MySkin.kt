package com.zml.skin

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi

class MySkin private constructor() {
    private lateinit var layoutInflater:LayoutInflater
    private lateinit var mSkinResources: Resources
    private val pluginPath = ""

    companion object{
        private val INSTANCE = MySkin()

        fun get():MySkin{
            return INSTANCE
        }

    }
    fun with(context:Context):MySkin{
        layoutInflater = LayoutInflater.from(context)
        if (!load(pluginPath,context)){
            throw RuntimeException("can't load skin plugin resources")
        }

        return this
    }



    @RequiresApi(Build.VERSION_CODES.Q)


    fun onCreateView(parent: View?,
                     name: String,
                     context: Context,
                     attrs: AttributeSet
    ):View?{
        if (name.indexOf(".") == -1){
            return layoutInflater.onCreateView(context,parent, name, attrs)
        }
        return layoutInflater.createView(context,name,null,attrs)
    }




    private fun load(path:String,context: Context):Boolean{
        try {
            val assetManager = AssetManager::class.java.newInstance()
            val addAssetPath = assetManager.javaClass.getMethod(
                "addAssetPath",
                String::class.java
            )
            addAssetPath.invoke(assetManager, path)

            val appResources = context.resources
            mSkinResources = Resources(
                assetManager,
                appResources.displayMetrics,
                appResources.configuration
            )
            return true
            //val mPm: PackageManager = context.packageManager
            //val info = mPm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)
        } catch (e: Exception) {
        }
        return false
    }

}