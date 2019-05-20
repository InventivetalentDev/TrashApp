package org.inventivetalent.trashapp.ui.main;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import org.inventivetalent.trashapp.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

	@StringRes
	private static final int[]   TAB_TITLES = new int[] {
			R.string.tab_text_compass,
			R.string.tab_text_map };
	private final        Context mContext;

	public SectionsPagerAdapter(Context context, FragmentManager fm) {
		super(fm);
		mContext = context;
	}

	@Override
	public Fragment getItem(int position) {
		if (position == 0) {
			return new CompassFragment();
		}
		if (position == 1) {
			return new MapFragment();
		}
		throw new IllegalStateException("Tab position was " + position + " and had no fragment candidate");
	}

	@Nullable
	@Override
	public CharSequence getPageTitle(int position) {
		return mContext.getResources().getString(TAB_TITLES[position]);
	}

	@Override
	public int getCount() {
		return 2;
	}
}