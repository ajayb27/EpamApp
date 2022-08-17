package com.epam.app.config;

public class SngineConfig {

    /* -- CONFIG VARIABLES -- */

    //complete URL of your Sngine website
    public static String Sngine_URL          = "http://myvillagekart.com";
//    public static String Sngine_URL          = "https://fineuploader.com/demos.html";


    /* -- PERMISSION VARIABLES -- */

    // enable JavaScript for webview
    public static boolean SngineApp_JSCRIPT     = true;

    // upload file from webview
    public static boolean SngineApp_FUPLOAD     = true;

    // enable upload from camera for photos
    public static boolean SngineApp_CAMUPLOAD   = true;

    // incase you want only camera files to upload
    public static boolean SngineApp_ONLYCAM	     = false;

    // upload multiple files in webview
    public static boolean SngineApp_MULFILE     = true;

    // track GPS locations
    public static boolean SngineApp_LOCATION    = true;

    // show ratings dialog; auto configured
    // edit method get_rating() for customizations
    public static boolean SngineApp_RATINGS     = true;

    // pull refresh current url
    public static boolean SngineApp_PULLFRESH	 = false;

    // show progress bar in app
    public static boolean SngineApp_PBAR        = true;

    // zoom control for webpages view
    public static boolean SngineApp_ZOOM        = true;

    // save form cache and auto-fill information
    public static boolean SngineApp_SFORM       = false;

    // whether the loading webpages are offline or online
    public static boolean SngineApp_OFFLINE     = false;

    // open external url with default browser instead of app webview
    public static boolean SngineApp_EXTURL      = true;


    /* -- SECURITY VARIABLES -- */

    // verify whether HTTPS port needs certificate verification
    public static boolean SngineApp_CERT_VERIFICATION = false;

    //to upload any file type using "*/*"; check file type references for more
    public static String Sngine_F_TYPE       = "*/*";


    /* -- RATING SYSTEM VARIABLES -- */

    public static int ASWR_DAYS            = 3;	// after how many days of usage would you like to show the dialoge
    public static int ASWR_TIMES           = 10;  // overall request launch times being ignored
    public static int ASWR_INTERVAL        = 2;   // reminding users to rate after days interval
}
