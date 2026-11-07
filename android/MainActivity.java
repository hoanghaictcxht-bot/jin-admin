package com.hoanghai.jinadmin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;

public class MainActivity extends Activity {
    private WebView webView;

    public class WebAppInterface {
        Context mContext;
        WebAppInterface(Context c) { mContext = c; }

        @JavascriptInterface
        public void copyToClipboard(String text) {
            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("JIN Admin", text);
            if (clipboard != null) clipboard.setPrimaryClip(clip);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#0f172a"));
            window.setNavigationBarColor(Color.parseColor("#0f172a"));
        }

        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor("#070b14"));
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(true);
            settings.setAllowUniversalAccessFromFileURLs(true);
        }
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.addJavascriptInterface(new WebAppInterface(this), "AndroidBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("tel:") || url.startsWith("mailto:") || url.endsWith(".apk"))) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception ignored) {}
                    return true;
                }
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("JIN Admin")
                    .setMessage(message)
                    .setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) { result.confirm(); }
                    })
                    .setCancelable(false)
                    .create().show();
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Xác nhận")
                    .setMessage(message)
                    .setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) { result.confirm(); }
                    })
                    .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) { result.cancel(); }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) { result.cancel(); }
                    })
                    .create().show();
                return true;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
                final EditText input = new EditText(MainActivity.this);
                input.setText(defaultValue != null ? defaultValue : "");
                new AlertDialog.Builder(MainActivity.this)
                    .setTitle(message)
                    .setView(input)
                    .setPositiveButton("Lưu", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            result.confirm(input.getText().toString());
                        }
                    })
                    .setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) { result.cancel(); }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        public void onCancel(DialogInterface dialog) { result.cancel(); }
                    })
                    .create().show();
                return true;
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
