package com.epam.app.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.epam.app.R;
import com.epam.app.application.MyApplication;
import com.epam.app.config.SngineConfig;
import com.epam.app.utils.DetectConnection;
import com.epam.app.utils.GPSTrack;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //Permission variables
    static boolean SngineApp_JSCRIPT    = SngineConfig.SngineApp_JSCRIPT;
    static boolean SngineApp_FUPLOAD    = SngineConfig.SngineApp_FUPLOAD;
    static boolean SngineApp_CAMUPLOAD  = SngineConfig.SngineApp_CAMUPLOAD;
    static boolean SngineApp_ONLYCAM		= SngineConfig.SngineApp_ONLYCAM;
    static boolean SngineApp_MULFILE    = SngineConfig.SngineApp_MULFILE;
    static boolean SngineApp_LOCATION   = SngineConfig.SngineApp_LOCATION;
    static boolean SngineApp_RATINGS    = SngineConfig.SngineApp_RATINGS;
    static boolean SngineApp_PULLFRESH	= SngineConfig.SngineApp_PULLFRESH;
    static boolean SngineApp_PBAR       = SngineConfig.SngineApp_PBAR;
    static boolean SngineApp_ZOOM       = SngineConfig.SngineApp_ZOOM;
    static boolean SngineApp_SFORM      = SngineConfig.SngineApp_SFORM;
    static boolean SngineApp_OFFLINE		= SngineConfig.SngineApp_OFFLINE;
    static boolean SngineApp_EXTURL		= SngineConfig.SngineApp_EXTURL;

    static boolean Sngine_ClearHistory = false;

    //Security variables
    static boolean SngineApp_CERT_VERIFICATION = SngineConfig.SngineApp_CERT_VERIFICATION;

    //Configuration variables
    private static String Sngine_URL      = SngineConfig.Sngine_URL;
    private String CURR_URL				 = Sngine_URL;
    private static String Sngine_F_TYPE   = SngineConfig.Sngine_F_TYPE;

    public static String ASWV_HOST		= aswm_host(Sngine_URL);

    //Careful with these variable names if altering
    WebView swvp_view;
    ProgressBar swvp_progress;
    TextView swvp_loading_text;
    NotificationManager swvp_notification;
    Notification swvp_notification_new;

    private String swvp_cam_message;
    private ValueCallback<Uri> swvp_file_message;
    private ValueCallback<Uri[]> swvp_file_path;
    private final static int swvp_file_req = 127;

    private final static int loc_perm = 1;
    private final static int file_perm = 2;

    private SecureRandom random = new SecureRandom();

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int SELECTTEXT_MENU_ID = Menu.FIRST;
    private JSBridge jsBridge;

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);

        menu.add(0, SELECTTEXT_MENU_ID, 0, "Select Text");

        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case SELECTTEXT_MENU_ID:
                SelectText();
                return true;
        }
        return true;
    }

    public void SelectText(){
        try{
            KeyEvent shiftPressEvent =
                    new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
            shiftPressEvent.dispatch(swvp_view);
        }catch(Exception e){
            throw new AssertionError(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        Uri[] results = null;
        Log.d("ajayfile","photoFile OA0");
        if (Build.VERSION.SDK_INT>=21) {
            Log.d("ajayfile","photoFile OA0 if code "+resultCode+" != "+Activity.RESULT_OK);
            if (resultCode == Activity.RESULT_OK) {
                Log.d("ajayfile", "photoFile OA01");
                if (requestCode == swvp_file_req) {
                    Log.d("ajayfile", "photoFile OA02");
                    if (null == swvp_file_path) {
                        return;
                    }
                    if (intent == null || intent.getData() == null) {
                        Log.d("ajayfile", "photoFile OA1");
                        if (swvp_cam_message != null) {
                            Log.d("ajayfile", "photoFile OA2");
                            results = new Uri[]{Uri.parse(swvp_cam_message)};
                        }
                    } else {
                        Log.d("ajayfile", "photoFile OA3");
                        String dataString = intent.getDataString();
                        if (dataString != null) {
                            Log.d("ajayfile", "photoFile OA4");
                            results = new Uri[]{Uri.parse(dataString)};
                        } else {
                            if (SngineApp_MULFILE) {
                                if (intent.getClipData() != null) {
                                    final int numSelectedFiles = intent.getClipData().getItemCount();
                                    results = new Uri[numSelectedFiles];
                                    for (int i = 0; i < numSelectedFiles; i++) {
                                        results[i] = intent.getClipData().getItemAt(i).getUri();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Log.d("ajayfile","else OA");
            if (requestCode == swvp_file_req) {
                Log.d("ajayfile","else OA1");
                if (null == swvp_file_message) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                swvp_file_message.onReceiveValue(result);
                swvp_file_message = null;
            }
        }
        if (swvp_file_path!=null)
            swvp_file_path.onReceiveValue(results);
        swvp_file_path = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("READ_PERM = ",Manifest.permission.READ_EXTERNAL_STORAGE);
        Log.w("WRITE_PERM = ",Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //Prevent the app from being started again when it is still alive in the background
        Log.d("testUrl", "Direct here");
        if (!isTaskRoot()) {
            Log.d("testUrl", "Cancelling out here");
            finish();
            return;
        }
        Intent intent = getIntent();
        Log.d("testIntent", "intent : "+intent);
        Bundle b = getIntent().getExtras();
        Log.d("testIntent", "bundle : "+intent.getExtras());
        Log.d("testIntent", "dataString : "+intent.getDataString());
        String someData;
        if (b!=null) {
            someData = b.getString("key1");
            Log.d("testIntentUrl", "url : "+someData);
//            onNewIntent(getIntent());
            Sngine_URL = someData;
        }
//        onNewIntent(getIntent());
        setContentView(R.layout.activity_main);

        swvp_view = findViewById(R.id.msw_view);
        jsBridge = new JSBridge();

//		swvp_view.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_PASTE));

//		swvp_view.setOnLongClickListener(new View.OnLongClickListener() {
//			@Override
//			public boolean onLongClick(View v) {
//				return false;
//			}
//		});

        final SwipeRefreshLayout pullfresh = findViewById(R.id.pullfresh);
        if (SngineApp_PULLFRESH) {
            pullfresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    pull_fresh();
                    pullfresh.setRefreshing(false);
                }
            });
            swvp_view.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    if (swvp_view.getScrollY() == 0) {
                        pullfresh.setEnabled(true);
                    } else {
                        pullfresh.setEnabled(false);
                    }
                }
            });
        }else{
            pullfresh.setRefreshing(false);
            pullfresh.setEnabled(false);
        }

        if (SngineApp_PBAR) {
            swvp_progress = findViewById(R.id.msw_progress);
        } else {
            findViewById(R.id.msw_progress).setVisibility(View.GONE);
        }
        swvp_loading_text = findViewById(R.id.msw_loading_text);
        Handler handler = new Handler();

        //Launching app rating request
        if (SngineApp_RATINGS) {
            handler.postDelayed(new Runnable() { public void run() { get_rating(); }}, 1000 * 60); //running request after few moments
        }

        //Getting basic device information
        get_info();

        //Getting GPS location of device if given permission
        if(SngineApp_LOCATION && !check_permission(1)){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, loc_perm);
        }
        get_location();

        //Webview settings; defaults are customized for best performance
        WebSettings webSettings = swvp_view.getSettings();
        swvp_view.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19");

        if(!SngineApp_OFFLINE){
            webSettings.setJavaScriptEnabled(SngineApp_JSCRIPT);
        }
        webSettings.setSaveFormData(SngineApp_SFORM);
        webSettings.setSupportZoom(SngineApp_ZOOM);
        webSettings.setGeolocationEnabled(SngineApp_LOCATION);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(SngineApp_JSCRIPT);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        swvp_view.addJavascriptInterface(jsBridge,"JSBridge");


        swvp_view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        swvp_view.setHapticFeedbackEnabled(false);

        swvp_view.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {

                if(!check_permission(2)){
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, file_perm);
                }else {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                    request.setMimeType(mimeType);
                    String cookies = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("cookie", cookies);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setDescription(getString(R.string.dl_downloading));
                    request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    assert dm != null;
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), getString(R.string.dl_downloading2), Toast.LENGTH_LONG).show();
                }
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        swvp_view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        swvp_view.setVerticalScrollBarEnabled(false);
        swvp_view.setWebViewClient(new Callback());

        //Rendering the default URL
        aswm_view(Sngine_URL, false);


        swvp_view.setWebChromeClient(new WebChromeClient() {
            //Handling input[type="file"]
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams){
                Log.d("ajayfile","FileChooserParams: "+fileChooserParams.isCaptureEnabled()+", "+fileChooserParams.getMode()+", "+ Arrays.toString(fileChooserParams.getAcceptTypes()));
                if(check_permission(2) && check_permission(3)) {
                    if (SngineApp_FUPLOAD) {
                        if (swvp_file_path != null) {
                            swvp_file_path.onReceiveValue(null);
                        }
                        swvp_file_path = filePathCallback;
                        Intent takePictureIntent = null;
                        if (SngineApp_CAMUPLOAD && fileChooserParams.isCaptureEnabled()) {
                            Log.d("ajayfile","photoFile");
                            takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            Log.d("ajayfile","photoFile before if : "+takePictureIntent);
                            if (takePictureIntent.resolveActivity   (MainActivity.this.getPackageManager()) != null) {
                                Log.d("ajayfile","photoFile in if");
                                File photoFile = null;
                                try {
                                    Log.d("ajayfile","photoFile1");
                                    photoFile = create_image();
                                    Log.d("ajayfile","photoFile2");
                                    takePictureIntent.putExtra("PhotoPath", swvp_cam_message);
                                } catch (IOException ex) {
                                    Log.d("ajayfile","photoFile Exception");
                                    Log.e(TAG, "Image file creation failed", ex);
                                }
                                if (photoFile != null) {
                                    Log.d("ajayfile","photoFile3");
                                    swvp_cam_message = "file:" + photoFile.getAbsolutePath();
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                                        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                    } else {
                                        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
                                        for (ResolveInfo resolveInfo : resInfoList) {
                                            String packageName = resolveInfo.activityInfo.packageName;
                                            grantUriPermission(packageName, Uri.fromFile(photoFile), Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        }
                                    }
                                } else {
                                    Log.d("ajayfile","photoFile4");
                                    takePictureIntent = null;
                                }
                            }
                        }
                        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        if (!SngineApp_ONLYCAM) {
                            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                            contentSelectionIntent.setType(Sngine_F_TYPE);
                            if (SngineApp_MULFILE) {
                                contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            }
                        }
                        Intent[] intentArray;
                        if (takePictureIntent != null) {
                            intentArray = new Intent[]{takePictureIntent};
                        } else {
                            intentArray = new Intent[0];
                        }

                        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                        if (!fileChooserParams.isCaptureEnabled())
                            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                        else
                            chooserIntent.putExtra(Intent.EXTRA_INTENT, new Intent());
                        chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.fl_chooser));
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                        startActivityForResult(chooserIntent, swvp_file_req);
                    }
                    return true;
                }else{
                    get_file();
                    return false;
                }
            }

            //Getting webview rendering progress
            @Override
            public void onProgressChanged(WebView view, int p) {
                if (SngineApp_PBAR) {
                    swvp_progress.setProgress(p);
                    if (p <= 10) {
//                        Toast.makeText(MainActivity.this,"Setting Visible : " +p, Toast.LENGTH_SHORT).show();
//                        swvp_progress.setVisibility(View.VISIBLE);

                    } else if (p == 100) {
                        swvp_progress.setProgress(0);
                        swvp_progress.setVisibility(View.INVISIBLE);
                    }
                }
            }



            // overload the geoLocations permissions prompt to always allow instantly as app permission was granted previously
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if(Build.VERSION.SDK_INT < 23 || check_permission(1)){
                    // location permissions were granted previously so auto-approve
                    callback.invoke(origin, true, false);
                } else {
                    // location permissions not granted so request them
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, loc_perm);
                }
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                android.util.Log.d("testWebLogin", consoleMessage.message());
                return true;
            }
        });
        Log.d("testUrll","path : before null check");
        if (getIntent().getData() != null) {
            String path     = getIntent().getDataString();
            Log.d("testUrll","path : "+path);
            /*
            If you want to check or use specific directories or schemes or hosts

            Uri data        = getIntent().getData();
            String scheme   = data.getScheme();
            String host     = data.getHost();
            List<String> pr = data.getPathSegments();
            String param1   = pr.get(0);
            */
            aswm_view(path, false);
        }
        Log.d("testUrll","path : after null check");
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        swvp_view.onResume();
        //Coloring the "recent apps" tab header; doing it onResume, as an insurance
        if (Build.VERSION.SDK_INT >= 23) {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            ActivityManager.TaskDescription taskDesc;
            taskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), bm, getColor(R.color.colorPrimary));
            MainActivity.this.setTaskDescription(taskDesc);
        }
        get_location();
    }

    //Setting activity layout visibility
    private class Callback extends WebViewClient {
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            swvp_progress.setVisibility(View.VISIBLE);
            swvp_progress.setProgress(0);
            get_location();
        }

        public void onPageFinished(WebView view, String url) {
            findViewById(R.id.msw_welcome).setVisibility(View.GONE);
            findViewById(R.id.msw_view).setVisibility(View.VISIBLE);

            swvp_progress = findViewById(R.id.msw_progress_outer);

            if (Sngine_ClearHistory) {
                swvp_view.clearHistory();
                Sngine_ClearHistory = false;
            }
//
//            CookieManager.getInstance().setAcceptCookie(true);
//            CookieManager.getInstance().flush();
//            Toast.makeText(MainActivity.this,"visibility : "+swvp_progress.getVisibility(),Toast.LENGTH_SHORT).show();
        }
        //For android below API 23
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(getApplicationContext(), getString(R.string.went_wrong), Toast.LENGTH_SHORT).show();
            aswm_view("file:///android_asset/error.html", false);
        }

        //Overriding webview URLs
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("testLogin","curr url : "+CURR_URL+"\t URL : "+url);
            if (CURR_URL.contains("login") && url.equalsIgnoreCase(Sngine_URL)) {
                Log.d("testLogin","url : "+CURR_URL+"\t equal? : "+CURR_URL.equalsIgnoreCase(url));
            }
            CURR_URL = url;
            return url_actions(view, url);
        }

        //Overriding webview URLs for API 23+ [suggested by github.com/JakePou]
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.d("testLogin","curr url : "+CURR_URL+"\t URL : "+request.getUrl().toString());
            if (CURR_URL.contains("login") && request.getUrl().toString().equalsIgnoreCase(Sngine_URL)) {
                Log.d("testLogin","url : "+CURR_URL+"\t equal? : "+CURR_URL.equalsIgnoreCase(request.getUrl().toString()));
                jsBridge.sendDataToWebView();
            }
            CURR_URL = request.getUrl().toString();
            return url_actions(view, request.getUrl().toString());
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if(SngineApp_CERT_VERIFICATION) {
                super.onReceivedSslError(view, handler, error);
            } else {
                handler.proceed(); // Ignore SSL certificate errors
            }
        }
    }

    private class JSBridge {

        @JavascriptInterface
        public void showMessageInNative(String message){
            //Received message from webview in native, process data
            Log.d("bigthing", "message  : "+message);
        }
        
        public void sendDataToWebView(){
            swvp_view.evaluateJavascript(
                    "javascript: " +"updateFromNative(" + MyApplication.nKey +
                            ")",null);
        }
    }

    //Random ID creation function to help get fresh cache every-time webview reloaded
    public String random_id() {
        return new BigInteger(130, random).toString(32);
    }

    //Opening URLs inside webview with request
    void aswm_view(String url, Boolean tab) {
        if (tab) {
//            Toast.makeText(this,"Flag : view: "+tab,Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } else {
//            Toast.makeText(this,"Flag : "+url+" view : "+tab,Toast.LENGTH_SHORT).show();
//            if(url.contains("?")){ // check to see whether the url already has query parameters and handle appropriately.
//                url += "&";
//            } else {
//                url += "?";
//            }
//            url += "rid="+random_id();
            swvp_view.loadUrl(url);
        }
    }

    //Actions based on shouldOverrideUrlLoading
    public boolean url_actions(WebView view, String url){
        boolean a = true;
        //Show toast error if not connected to the network
        if (!SngineApp_OFFLINE && !DetectConnection.isInternetAvailable(MainActivity.this)) {
            Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();

            //Use this in a hyperlink to redirect back to default URL :: href="refresh:android"
        } else if (url.startsWith("refresh:")) {
            String ref_sch = (Uri.parse(url).toString()).replace("refresh:","");
            if(ref_sch.matches("URL")){
                CURR_URL = Sngine_URL;
            }
            pull_fresh();

            //Use this in a hyperlink to launch default phone dialer for specific number :: href="tel:+919876543210"
        } else if (url.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            startActivity(intent);

            //Use this to open your apps page on google play store app :: href="rate:android"
        } else if (url.startsWith("rate:")) {
            final String app_package = getPackageName(); //requesting app package name from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + app_package)));
            } catch (ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + app_package)));
            }

            //Sharing content from your webview to external apps :: href="share:URL" and remember to place the URL you want to share after share:___
        } else if (url.startsWith("share:")) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, view.getTitle());
            intent.putExtra(Intent.EXTRA_TEXT, view.getTitle()+"\nVisit: "+(Uri.parse(url).toString()).replace("share:",""));
            startActivity(Intent.createChooser(intent, getString(R.string.share_w_friends)));

            //Use this in a hyperlink to exit your app :: href="exit:android"
        } else if (url.startsWith("exit:")) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            //Getting location for offline files
        } else if (url.startsWith("offloc:")) {
            String offloc = Sngine_URL + "?loc=" + get_location();
            aswm_view(offloc, false);
            Log.d("OFFLINE LOC REQ", offloc);

        } else if (url.contains(Sngine_URL)) {

            a = false;
            //Opening external URLs in android default web browser
        } else if (SngineApp_EXTURL && !aswm_host(url).equals(ASWV_HOST)) {
//            Toast.makeText(this,"Flag actions: "+a,Toast.LENGTH_SHORT).show();
            aswm_view(url,true);
        } else {
            a = false;
//			Toast.makeText(MainActivity.this,"false",Toast.LENGTH_SHORT).show();
        }

        return a;
    }

    //Getting host name
    public static String aswm_host(String url){
        if (url == null || url.length() == 0) {
            return "";
        }
        int dslash = url.indexOf("//");
        if (dslash == -1) {
            dslash = 0;
        } else {
            dslash += 2;
        }
        int end = url.indexOf('/', dslash);
        end = end >= 0 ? end : url.length();
        int port = url.indexOf(':', dslash);
        end = (port > 0 && port < end) ? port : end;
        Log.w("URL Host: ",url.substring(dslash, end));
        return url.substring(dslash, end);
    }

    //Reloading current page
    public void pull_fresh(){
        aswm_view((!CURR_URL.equals("")?CURR_URL:Sngine_URL),false);
    }

    //Getting device basic information
    public void get_info(){
        CookieManager cookieManager = CookieManager.getInstance();
//        if (android.os.Build.VERSION.SDK_INT >= 21) {
//            cookieManager.setAcceptThirdPartyCookies(swvp_view,true);
//        }else {
//            cookieManager.setAcceptCookie(true);
//        }
//        CookieManager.setAcceptFileSchemeCookies(true);
//        cookieManager.setCookie(Sngine_URL, "DEVICE=android");
//        cookieManager.setCookie(Sngine_URL, "DEV_API=" + Build.VERSION.SDK_INT);
    }

    //Checking permission for storage and camera for writing and uploading images
    public void get_file(){
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

        //Checking for storage permission to write images for upload
        if (SngineApp_FUPLOAD && SngineApp_CAMUPLOAD && !check_permission(2) && !check_permission(3)) {
            ActivityCompat.requestPermissions(MainActivity.this, perms, file_perm);

            //Checking for WRITE_EXTERNAL_STORAGE permission
        } else if (SngineApp_FUPLOAD && !check_permission(2)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, file_perm);

            //Checking for CAMERA permissions
        } else if (SngineApp_CAMUPLOAD && !check_permission(3)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, file_perm);
        }
    }

    //Using cookies to update user locations
    public String get_location(){
        String newloc = "0,0";
        //Checking for location permissions
        if (SngineApp_LOCATION && (Build.VERSION.SDK_INT < 23 || check_permission(1))) {
            GPSTrack gps;
            gps = new GPSTrack(MainActivity.this);
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            if (gps.canGetLocation()) {
                if (latitude != 0 || longitude != 0) {
                    if(!SngineApp_OFFLINE) {
                        CookieManager cookieManager = CookieManager.getInstance();
                        cookieManager.setAcceptCookie(true);
                        cookieManager.setCookie(Sngine_URL, "lat=" + latitude);
                        cookieManager.setCookie(Sngine_URL, "long=" + longitude);
                    }
                    //Log.w("New Updated Location:", latitude + "," + longitude);  //enable to test dummy latitude and longitude
                    newloc = latitude+","+longitude;
                } else {
                    Log.w("New Updated Location:", "NULL");
                }
            } else {
                show_notification(1, 1);
                Log.w("New Updated Location:", "FAIL");
            }
        }
        return newloc;
    }

    //Checking if particular permission is given or not
    public boolean check_permission(int permission){
        switch(permission){
            case 1:
                return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            case 2:
                return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            case 3:
                return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        }
        return false;
    }

    //Creating image file for upload
    private File create_image() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String file_name    = new SimpleDateFormat("yyyy_mm_ss").format(new Date());
        String new_name     = "file_"+file_name+"_";
        File sd_directory   = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        Log.d("ajayfile","file : "+sd_directory);
//        return File.createTempFile(new_name, ".jpg", sd_directory);
        return new File(sd_directory+"/"+new_name+".jpg");
    }

    //Launching app rating dialoge [developed by github.com/hotchemi]
    public void get_rating() {
        if (DetectConnection.isInternetAvailable(MainActivity.this)) {
//            AppRate.with(this)
//                    .setStoreType(StoreType.GOOGLEPLAY)     //default is Google Play, other option is Amazon App Store
//                    .setInstallDays(SngineConfig.ASWR_DAYS)
//                    .setLaunchTimes(SngineConfig.ASWR_TIMES)
//                    .setRemindInterval(SngineConfig.ASWR_INTERVAL)
//                    .setTitle(R.string.rate_dialog_title)
//                    .setMessage(R.string.rate_dialog_message)
//                    .setTextLater(R.string.rate_dialog_cancel)
//                    .setTextNever(R.string.rate_dialog_no)
//                    .setTextRateNow(R.string.rate_dialog_ok)
//                    .monitor();
//            AppRate.showRateDialogIfMeetsConditions(this);
        }
        //for more customizations, look for AppRate and DialogManager
    }

    //Creating custom notifications with IDs
    public void show_notification(int type, int id) {
        long when = System.currentTimeMillis();
        swvp_notification = (NotificationManager) MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent i = new Intent();
        if (type == 1) {
            i.setClass(MainActivity.this, MainActivity.class);
        } else if (type == 2) {
            i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        } else {
            i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            i.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        }
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "");
        switch(type){
            case 1:
                builder.setTicker(getString(R.string.app_name));
                builder.setContentTitle(getString(R.string.loc_fail));
                builder.setContentText(getString(R.string.loc_fail_text));
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.loc_fail_more)));
                builder.setVibrate(new long[]{350,350,350,350,350});
                builder.setSmallIcon(R.mipmap.ic_launcher);
                break;

            case 2:
                builder.setTicker(getString(R.string.app_name));
                builder.setContentTitle(getString(R.string.loc_perm));
                builder.setContentText(getString(R.string.loc_perm_text));
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.loc_perm_more)));
                builder.setVibrate(new long[]{350, 700, 350, 700, 350});
                builder.setSound(alarmSound);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                break;
        }
        builder.setOngoing(false);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        builder.setWhen(when);
        builder.setContentIntent(pendingIntent);
        swvp_notification_new = builder.build();
        swvp_notification.notify(id, swvp_notification_new);
    }

    //Checking if users allowed the requested permissions or not
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                get_location();
            }
        }
    }

    //Action on back key tap/click
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (swvp_view.canGoBack()) {
                    swvp_view.goBack();
                }
                else if (Sngine_URL!=SngineConfig.Sngine_URL) {
                    Sngine_URL = SngineConfig.Sngine_URL;
                    swvp_view.loadUrl(Sngine_URL);
                    Sngine_ClearHistory = true;
//                    swvp_view.clearHistory();
                } else{
                    finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState ){
        super.onSaveInstanceState(outState);
        swvp_view.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        swvp_view.restoreState(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("testIntent","here in onNewIntent()");
        if (intent == null) return;
        String url = intent.getDataString();
        Log.d("testUrl", "URL : "+url);
        if (url != null  && !url.equalsIgnoreCase("null"))
            swvp_view.loadUrl(url);
//            url_actions(swvp_view,url);
//            aswm_view(url,false);
    }
}

