package com.jarvis.browser;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.text.TextUtils;

public class MainActivity extends Activity {
    private WebView web;
    private EditText address;
    private ProgressBar progress;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(12, 37, 48));
        getWindow().setNavigationBarColor(Color.rgb(12, 37, 48));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        LinearLayout bar = new LinearLayout(this);
        bar.setPadding(dp(8), dp(8), dp(8), dp(4));
        bar.setGravity(android.view.Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(Color.rgb(12, 37, 48));
        Button home = button("⌂");
        home.setContentDescription("Home");
        address = new EditText(this);
        address.setSingleLine(true);
        address.setTextColor(Color.WHITE);
        address.setHintTextColor(Color.LTGRAY);
        address.setHint("Search or enter website");
        address.setTextSize(15);
        address.setSelectAllOnFocus(true);
        address.setImeOptions(EditorInfo.IME_ACTION_GO);
        address.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_URI);
        LinearLayout.LayoutParams addrParams = new LinearLayout.LayoutParams(0, dp(48), 1);
        addrParams.setMargins(dp(6), 0, dp(6), 0);
        bar.addView(home);
        bar.addView(address, addrParams);
        Button go = button("Go");
        bar.addView(go);
        root.addView(bar);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        root.addView(progress, new LinearLayout.LayoutParams(-1, dp(3)));

        web = new WebView(this);
        WebSettings ws = web.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setMediaPlaybackRequiresUserGesture(true);
        ws.setSupportMultipleWindows(false);
        ws.setJavaScriptCanOpenWindowsAutomatically(false);
        ws.setAllowFileAccess(false);
        ws.setAllowContentAccess(false);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
                if ("http".equals(scheme) || "https".equals(scheme)) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); }
                catch (Exception e) {
                    ToastMessage("No app can open this link");
                }
                return true;
            }
            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                address.setText(url);
                progress.setVisibility(View.VISIBLE);
            }
            @Override public void onPageFinished(WebView view, String url) {
                address.setText(url);
                progress.setVisibility(View.GONE);
                status.setText(view.getTitle() == null ? "Ready" : view.getTitle());
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int newProgress) {
                progress.setProgress(newProgress);
                progress.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
            }
        });
        root.addView(web, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout controls = new LinearLayout(this);
        controls.setPadding(dp(4), dp(4), dp(4), dp(4));
        controls.setBackgroundColor(Color.rgb(245, 247, 248));
        Button back = button("‹");
        back.setContentDescription("Back");
        Button forward = button("›");
        forward.setContentDescription("Forward");
        Button reload = button("↻");
        reload.setContentDescription("Reload");
        Button external = button("↗");
        external.setContentDescription("Open in another app");
        status = new TextView(this);
        status.setText("Ready");
        status.setTextColor(Color.DKGRAY);
        status.setSingleLine(true);
        status.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(0, dp(44), 1);
        controls.addView(back);
        controls.addView(forward);
        controls.addView(reload);
        controls.addView(status, statusParams);
        controls.addView(external);
        root.addView(controls);

        home.setOnClickListener(v -> web.loadUrl("https://www.google.com"));
        go.setOnClickListener(v -> navigate());
        address.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO ||
                (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                 event.getAction() == KeyEvent.ACTION_DOWN)) {
                navigate();
                return true;
            }
            return false;
        });
        back.setOnClickListener(v -> { if (web.canGoBack()) web.goBack(); });
        forward.setOnClickListener(v -> { if (web.canGoForward()) web.goForward(); });
        reload.setOnClickListener(v -> web.reload());
        external.setOnClickListener(v -> {
            String url = web.getUrl();
            if (url != null) {
                try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); }
                catch (Exception e) { ToastMessage("Could not open link"); }
            }
        });
        setContentView(root);
        if (state != null) web.restoreState(state);
        else web.loadUrl("https://www.google.com");
    }

    private void ToastMessage(String message) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
    }
    private Button button(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(16);
        b.setMinWidth(dp(42));
        b.setPadding(dp(4), 0, dp(4), 0);
        return b;
    }
    private int dp(int n) {
        return (int) (n * getResources().getDisplayMetrics().density + 0.5f);
    }
    private void navigate() {
        String input = address.getText().toString().trim();
        if (input.isEmpty()) return;
        String url;
        if (input.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) url = input;
        else if (input.matches("(?i)^[a-z0-9.-]+\\.[a-z]{2,}(:\\d+)?(/.*)?$"))
            url = "https://" + input;
        else url = "https://www.google.com/search?q=" + Uri.encode(input);
        web.loadUrl(url);
        address.clearFocus();
        ((android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(address.getWindowToken(), 0);
    }
    @Override protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (web != null) web.saveState(outState);
    }
    @Override public void onBackPressed() {
        if (web != null && web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        if (web != null) {
            web.stopLoading();
            web.loadUrl("about:blank");
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
