package com.epam.app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.epam.app.R;
import com.epam.app.config.SngineConfig;
import com.epam.app.utils.DetectConnection;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

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

    private static String Sngine_URL      = SngineConfig.Sngine_URL;
    private String CURR_URL				 = Sngine_URL;
    private static String Sngine_F_TYPE   = SngineConfig.Sngine_F_TYPE;

    WebView swvp_view;
    ProgressBar swvp_progress;
    TextView swvp_loading_text;
    NotificationManager swvp_notification;
    Notification swvp_notification_new;

    //Security variables
    static boolean SngineApp_CERT_VERIFICATION = SngineConfig.SngineApp_CERT_VERIFICATION;

    public static String ASWV_HOST		= aswm_host(Sngine_URL);

    private static final String TAG = MainActivity.class.getSimpleName();

    private SecureRandom random = new SecureRandom();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isTaskRoot()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        swvp_view = findViewById(R.id.msw_view);

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

        //Getting basic device information
        get_info();


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

        swvp_view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });
        swvp_view.setHapticFeedbackEnabled(false);

        swvp_view.setVerticalScrollBarEnabled(true);
        swvp_view.setWebViewClient(new Callback());

        //Rendering the default URL
        aswm_view(Sngine_URL, false);

        swvp_view.setWebChromeClient(new WebChromeClient() {

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
        });

        if (getIntent().getData() != null) {
            String path     = getIntent().getDataString();
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
    }

    void aswm_view(String url, Boolean tab) {
        if (tab) {
//            Toast.makeText(this,"Flag : view: "+tab,Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } else {
//            Toast.makeText(this,"Flag : "+url+" view : "+tab,Toast.LENGTH_SHORT).show();
            if(url.contains("?")){ // check to see whether the url already has query parameters and handle appropriately.
                url += "&";
            } else {
                url += "?";
            }
            url += "rid="+random_id();
            swvp_view.loadUrl(url);
        }
    }

    //Getting device basic information
    public void get_info(){
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(Sngine_URL, "DEVICE=android");
        cookieManager.setCookie(Sngine_URL, "DEV_API=" + Build.VERSION.SDK_INT);
    }

    public String random_id() {
        return new BigInteger(130, random).toString(32);
    }

    //Reloading current page
    public void pull_fresh(){

        Log.d("yeyy", "refresh : "+CURR_URL);
        aswm_view((!CURR_URL.equals("")?CURR_URL:Sngine_URL),false);
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

    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (swvp_view.canGoBack()) {
                    swvp_view.goBack();
                    Log.d("yeyy",swvp_view.getUrl());
                } else {
                    finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
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


    //Setting activity layout visibility
    private class Callback extends WebViewClient {
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            swvp_progress.setVisibility(View.VISIBLE);
            swvp_progress.setProgress(0);
        }

        public void onPageFinished(WebView view, String url) {
            findViewById(R.id.msw_welcome).setVisibility(View.GONE);
            findViewById(R.id.msw_view).setVisibility(View.VISIBLE);

            swvp_progress = findViewById(R.id.msw_progress_outer);
//
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
            CURR_URL = url;
            return url_actions(view, url);
        }

        //Overriding webview URLs for API 23+ [suggested by github.com/JakePou]
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
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
}