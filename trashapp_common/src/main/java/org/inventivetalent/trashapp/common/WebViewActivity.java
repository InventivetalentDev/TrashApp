package org.inventivetalent.trashapp.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.*;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

	private WebView webView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view);

		webView = findViewById(R.id.webview);
		webView.setWebViewClient(new WebViewClient() {

			// Prevent redirects to trigger an "open with" dialog and load it directly in our webview instead
			// https://stackoverflow.com/questions/4066438/android-webview-how-to-handle-redirects-in-app-instead-of-opening-a-browser
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}
		});
		webView.addJavascriptInterface(this, "TrashApp");
		WebSettings webSettings = webView.getSettings();
		webSettings.setUserAgentString("TrashApp/" + Util.APP_VERSION_NAME);

		Intent intent = getIntent();
		if (intent != null) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				String title = extras.getString("title", getString(R.string.app_name));
				setTitle(title);

				String url = extras.getString("url", "https://trashapp.cc");
				webSettings.setJavaScriptEnabled(true);

				webView.loadUrl(url);

				CookieManager cookieManager = CookieManager.getInstance();
				String cookieString = cookieManager.getCookie("https://osmbridge.trashapp.cc");
				Log.i("WebViewActivity", "cookie: " + cookieString);
			}
		}
	}

	@JavascriptInterface
	public void osmAuthCallback() {
		Log.i("WebViewActivity", "osmAuthCallback()");

		setResult(RESULT_OK);
		finish();
	}

}
