package org.inventivetalent.trashapp;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.inventivetalent.trashapp.common.Util;

public class AboutActivity extends AppCompatActivity {

	private TextView versionTextView;
	private TextView versionCodeTextView;
	private Button   button;

	private FirebaseAnalytics mFirebaseAnalytics;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Util.applyTheme(this);

		setContentView(R.layout.activity_about);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

			versionTextView = findViewById(R.id.versionTextView);
			versionTextView.setText(getResources().getString(R.string.about_version, pInfo.versionName));
			versionCodeTextView = findViewById(R.id.versionCodeTextView);
			versionCodeTextView.setText(String.valueOf(pInfo.versionCode) + " (" + (BuildConfig.DEBUG ? "debug" : "release") + ")");
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			Log.d("MyApp", "PackageManager Catch : " + e.toString());
		}

		button = findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//				if (TabActivity.SKU_INFO_PREMIUM != null) {
				//					TabActivity.SKU_INFO_PREMIUM.launchBilling();
				//				} else {
				//					Toast.makeText(AboutActivity.this, "Product not ready!", Toast.LENGTH_SHORT).show();
				//				}

				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.patreon.com/inventivetalent"));
				startActivity(browserIntent);

				mFirebaseAnalytics.logEvent("about_patreon_click", null);
			}
		});
	}

}
