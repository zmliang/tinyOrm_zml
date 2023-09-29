package com.zml.skin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.media.MediaScannerConnection.OnScanCompletedListener
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater.Factory2
import android.view.View
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.zml.installer.InstallerActivity
import com.zml.skin.chart.ChartView
import com.zml.skin.ui.theme.SkinTheme
import java.util.*


class MainActivity : Activity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


//
//        layoutInflater.factory2 = object :Factory2{
//            @RequiresApi(Build.VERSION_CODES.Q)
//            override fun onCreateView(
//                parent: View?,
//                name: String,
//                context: Context,
//                attrs: AttributeSet
//            ): View? {
//                Log.i("zml","onCreateView-- name=$name")
//                return MySkin.get().with(this@MainActivity).onCreateView(
//                    parent, name, context, attrs
//                )
//            }
//
//            override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
//                Log.i("zml","onCreateView--2")
//                return null
//            }
//
//        }

        setContentView(R.layout.activity_layout)

        val view = findViewById<ChartView>(R.id.handicap_sell)

        val handler = Handler()

        val task = object :Runnable {
            override fun run() {
                //val volume = 10f
                val list = ArrayList<ChartView.ChartModel>()
                for (i in 0..5) {
                    val model = ChartView.ChartModel()
                    model.volume = (Random().nextInt(9) + 1).toFloat()
                    model.leftTxt = (Random().nextInt(900).toString())
                    model.rightTxt = (Random().nextInt(9000).toString())
                    list.add(model)
                }
                view.setChartModel(10f,list)
                handler.postDelayed(this,2000)
            }

        }

       handler.postDelayed(task,0)

        findViewById<Button>(R.id.btn).setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)

            intent.type = "*/*"

            intent.addCategory(Intent.CATEGORY_OPENABLE)

            startActivityForResult(Intent.createChooser(intent,"需要选择文件"),1)
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) { //请求权限
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )
        }

        checkStorageManagerPermission(this)


    }

    fun checkStorageManagerPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            context.startActivity(intent)
            return false
        }
        return true
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            val uri = data?.data

            Log.i("zml","uri=${getPath(this,uri)}")
            //val pathString = UriUtil.getPath(this,uri);

            //filePathtextView.setText(pathString);

            //val sourceFile: File? = uri?.path?.let { File(it) }

            startActivity(Intent(this, InstallerActivity::class.java).apply {
                this.data = uri
            })
//
//
//            val parsed: PackageParser.Package = PackageUtil.getPackageInfo(this, sourceFile)
//
//            val `as`: PackageUtil.AppSnippet = PackageUtil.getAppSnippet(this, appInfo, sourceFile)

        }
    }


    //@RequiresApi(api = Build.VERSION_CODES.KITKAT)
    fun getPath(context:Context, uri:Uri?):String {
        //val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

//        // DocumentProvider
//        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
//            // ExternalStorageProvider
//            if (isExternalStorageDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                if ("primary".equalsIgnoreCase(type)) {
//                    return Environment.getExternalStorageDirectory() + "/" + split[1];
//                }
//            }
//            // DownloadsProvider
//            else if (isDownloadsDocument(uri)) {
//
//                final String id = DocumentsContract.getDocumentId(uri);
//                final Uri contentUri = ContentUris.withAppendedId(
//                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
//
//                return getDataColumn(context, contentUri, null, null);
//            }
//            // MediaProvider
//            else if (isMediaDocument(uri)) {
//                final String docId = DocumentsContract.getDocumentId(uri);
//                final String[] split = docId.split(":");
//                final String type = split[0];
//
//                Uri contentUri = null;
//                if ("image".equals(type)) {
//                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//                } else if ("video".equals(type)) {
//                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//                } else if ("audio".equals(type)) {
//                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
//                }
//
//                final String selection = "_id=?";
//                final String[] selectionArgs = new String[]{
//                    split[1]
//                };
//
//                return getDataColumn(context, contentUri, selection, selectionArgs);
//            }
//        }
//        // MediaStore (and general)
//        else if ("content".equalsIgnoreCase(uri.getScheme())) {
//            return getDataColumn(context, uri, null, null);
//        }
//        // File
//        else

            if ( uri?.scheme.equals("file",true)) {
            return uri?.path ?:""
        }

        return ""
    }


}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SkinTheme {
        Greeting("Android")
    }
}