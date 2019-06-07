package org.inventivetalent.trashapp.ui.main;

import android.location.Location;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import org.inventivetalent.trashapp.common.LatLon;

public class PageViewModel extends ViewModel {

	private MutableLiveData<Integer>  mIndex      = new MutableLiveData<>();
	private LiveData<String>          mText       = Transformations.map(mIndex, new Function<Integer, String>() {
		@Override
		public String apply(Integer input) {
			return "Hello world from section: " + input;
		}
	});
	public  MutableLiveData<Location> mLocation   = new MutableLiveData<>();
	public  MutableLiveData<LatLon>   mClosestCan = new MutableLiveData<>();
	public  MutableLiveData<Float>    mRotation   = new MutableLiveData<>();

	public void setIndex(int index) {
		mIndex.setValue(index);
	}

	public LiveData<String> getText() {
		return mText;
	}
}