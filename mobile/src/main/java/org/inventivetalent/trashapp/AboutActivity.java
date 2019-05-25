package org.inventivetalent.trashapp;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

	private TextView versionTextView;
	private TextView versionCodeTextView;
	private Button button;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		try {
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

			versionTextView = findViewById(R.id.versionTextView);
			versionTextView.setText(getResources().getString(R.string.about_version, pInfo.versionName));
			versionCodeTextView = findViewById(R.id.versionCodeTextView);
			versionCodeTextView.setText(String.valueOf(pInfo.versionCode));
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
			Log.d("MyApp", "PackageManager Catch : " + e.toString());
		}

		button = findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//TODO
				Toast.makeText(AboutActivity.this, "paypal@inventivetalent.org", Toast.LENGTH_LONG).show();
			}
		});
	}

}
