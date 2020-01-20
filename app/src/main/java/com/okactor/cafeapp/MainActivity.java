package com.okactor.cafeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.Browser;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    //WebView
    private WebView webView;
    private WebView childView;
    private WebSettings webSettings;
    private String myURL = "";
    private String childURL = "";
    int count = 1;
    private ProgressBar progressBar;
    private ValueCallback mFilePathCallBack;
    private String realURL = "https://okactor.co.kr/";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWebView();
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initWebView()
    {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        progressBar = findViewById(R.id.progressbar);

        webView = findViewById(R.id.webView);

        webView.setHorizontalScrollBarEnabled(false);
        webView.setVerticalScrollBarEnabled(false);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setTextZoom(100);

        webView.setWebChromeClient(new CWSWebChromeClient());
        webView.setWebViewClient(new CWSWebViewClient());

        webView.loadUrl(realURL);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        webView.resumeTimers();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        webView.pauseTimers();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 30 && resultCode == Activity.RESULT_OK)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mFilePathCallBack.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            else
                mFilePathCallBack.onReceiveValue(new Uri[]{data.getData()});
            mFilePathCallBack = null;
        }
    }

    private class CWSWebViewClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            if (url.startsWith("http://"))
                return false;
            else
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
                if (url.startsWith("sms:"))
                {
                    Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                else if (url.startsWith("tel:"))
                {
                    Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                else if (url.startsWith("mailto:"))
                {
                    Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                    startActivity(i);
                    return true;
                }
                else if (url.startsWith("intent:"))
                {
                    try
                    {
                        Intent i = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                        Intent existPackage = getPackageManager().getLaunchIntentForPackage(i.getPackage());
                        if (existPackage != null)
                        {
                            startActivity(i);
                        }
                        else
                        {
                            Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                            marketIntent.setData(Uri.parse("market://details?id=" + i.getPackage()));
                            startActivity(marketIntent);
                        }
                        return true;
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon)
        {
            super.onPageStarted(view, url, favicon);

            progressBar.setVisibility(View.VISIBLE);

            myURL = url;
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            super.onPageFinished(view, url);

            progressBar.setVisibility(View.INVISIBLE);

            myURL = url;

            if (!childURL.equals("") || !childURL.equals(null) || !childURL.isEmpty())
            {
                childURL = "";
                webView.removeView(childView);
            }
        }

        public void onReceivedError(WebView view, int errorCode, String description, String
                failingUrl)
        {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Toast.makeText(MainActivity.this, "Loading Err! Please check your network state." + description, Toast.LENGTH_SHORT).show();
        }
    }

    private class CWSWebChromeClient extends WebChromeClient
    {
        @SuppressLint("SetJavaScriptEnabled")
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg)
        {
            count = 1;
            webView.removeAllViews();

            childView = new WebView(MainActivity.this);
            childView.getSettings().setJavaScriptEnabled(true);
            childView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
            childView.setWebChromeClient(this);

            childView.setWebViewClient(new WebViewClient()
            {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon)
                {
                    super.onPageStarted(view, url, favicon);

                    childURL = url;

                    if (count == 1) count = 0;
                }

                @Override
                public void onPageFinished(WebView view, String url)
                {
                    super.onPageFinished(view, url);
                    count = 1;
                    webView.scrollTo(0, 0);
                }

            });

            childView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            webView.addView(childView);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(childView);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onCloseWindow(WebView window)
        {
            super.onCloseWindow(window);
            webView.removeView(window);
        }

        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]>
                filePathCallback, FileChooserParams fileChooserParams)
        {
            mFilePathCallBack = filePathCallback;

            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(i.CATEGORY_OPENABLE);
            i.setType("image/*");

            startActivityForResult(i, 30);
            return true;
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result)
        {
            final JsResult finalRes = result;
            myURL = url;
            //AlertDialog 생성
            new AlertDialog.Builder(view.getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    .setMessage(message)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            finalRes.confirm();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result)
        {
            final JsResult finalRes = result;
            myURL = url;
            //AlertDialog 생성
            new AlertDialog.Builder(view.getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    .setMessage(message)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            finalRes.confirm();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result)
        {
            final JsResult finalRes = result;
            myURL = url;
            //AlertDialog 생성
            new AlertDialog.Builder(view.getContext(), AlertDialog.THEME_DEVICE_DEFAULT_LIGHT)
                    .setMessage(message)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            finalRes.confirm();
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            finalRes.cancel();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress)
        {
            progressBar.setProgress(newProgress);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (myURL.equals(realURL) && (childURL.equals("") || childURL.equals(null) || childURL.isEmpty()))
        {
            new AlertDialog.Builder(MainActivity.this)
                    .setMessage(R.string.message_finish)
                    .setPositiveButton(R.string.answer_ok, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.answer_no, null)
                    .show();
        }
        else if (webView.canGoBack() && (childURL.equals("") || childURL.equals(null) || childURL.isEmpty()))
        {
            webView.goBack();
        }
        else if (!childURL.equals("") || !childURL.equals(null) || !childURL.isEmpty())
        {
            webView.removeView(childView);
            childURL = "";
            webView.reload();
        }
    }
}
