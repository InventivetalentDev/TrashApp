package org.inventivetalent.trashapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("TrashApp", "Hello World!");

		Intent tabIntent = new Intent(this, TabActivity.class);
		startActivity(tabIntent);

	}

}
