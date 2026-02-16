package com.shofyou.app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;

    private final String HOME_URL = "https://shofyou.com";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // جعل الخلفية شفافة
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // تغيير لون الايقونات حسب وضع الهاتف (نهار / ليل)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            int nightModeFlags =
                    getResources().getConfiguration().uiMode
                            & Configuration.UI_MODE_NIGHT_MASK;

            if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {

                // الوضع الليلي → ايقونات بيضاء
                getWindow().getDecorView().setSystemUiVisibility(0);

            } else {

                // الوضع النهاري → ايقونات سوداء
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        webView = findViewById(R.id.webview);

        WebSettings ws = webView.getSettings();

        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(true);
        ws.setMediaPlaybackRequiresUserGesture(false);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new Browser());
        webView.setWebChromeClient(new Chrome());

        if (savedInstanceState != null)
            webView.restoreState(savedInstanceState);
        else
            webView.loadUrl(HOME_URL);

        handleBack();
    }

    private class Browser extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view,
                                                WebResourceRequest request) {

            String url = request.getUrl().toString();

            if (url.contains("shofyou.com")) {

                view.loadUrl(url);
                return true;
            }

            Intent intent =
                    new Intent(MainActivity.this,
                            PopupActivity.class);

            intent.putExtra("url", url);

            startActivity(intent);

            return true;
        }
    }

    private class Chrome extends WebChromeClient {

        @Override
        public boolean onShowFileChooser(WebView webView,
                                         ValueCallback<Uri[]> callback,
                                         FileChooserParams params) {

            fileCallback = callback;

            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/* video/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

            startActivityForResult(intent, 100);

            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        if (fileCallback == null) return;

        Uri[] result = null;

        if (resultCode == RESULT_OK && data != null) {

            result = new Uri[]{data.getData()};
        }

        fileCallback.onReceiveValue(result);

        fileCallback = null;
    }

    private void handleBack() {

        getOnBackPressedDispatcher()
                .addCallback(this,
                        new OnBackPressedCallback(true) {

            @Override
            public void handleOnBackPressed() {

                if (webView.canGoBack())
                    webView.goBack();

                else {

                    new AlertDialog.Builder(MainActivity.this)
                            .setMessage("Exit app?")
                            .setPositiveButton("Yes",
                                    (d, i) -> finish())
                            .setNegativeButton("No", null)
                            .show();
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {

        webView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {

        webView.onResume();
        super.onResume();
    }
}
