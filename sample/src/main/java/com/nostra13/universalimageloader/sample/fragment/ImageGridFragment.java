/*******************************************************************************
 * Copyright 2011-2014 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.nostra13.universalimageloader.sample.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.sample.Constants;
import com.nostra13.universalimageloader.sample.R;
import com.nostra13.universalimageloader.sample.retrogram.Instagram;
import com.nostra13.universalimageloader.sample.retrogram.model.Media;
import com.nostra13.universalimageloader.sample.retrogram.model.SearchMediaResponse;
import com.nostra13.universalimageloader.sample.util.GPSTracker;
import com.nostra13.universalimageloader.sample.vk.VK;
import com.nostra13.universalimageloader.sample.vk.model.Items;
import com.nostra13.universalimageloader.sample.vk.model.VkResponse;

import java.util.ArrayList;

import retrofit.RestAdapter;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
public class ImageGridFragment extends AbsListViewBaseFragment {

	public static final int INDEX = 1;

	protected static String AccessToken = "398315918.19f142f.1a6004bc7ce04dc1bc0a7914095a30cb";
	protected static String ClientId = "19f142f8b92641e7b528497c9d206379";
	protected ArrayList<String> imagesList = new ArrayList<>();

	protected  Double latitude;
	protected  Double longitude;

	protected Instagram instagram;

	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

	// The minimum time between updates in milliseconds
	private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		latitude = getArguments().getDouble("lat");
		longitude = getArguments().getDouble("lon");
		new AsyncHttpTask().execute();
		View rootView = inflater.inflate(R.layout.fr_image_grid, container, false);
		listView = (GridView) rootView.findViewById(R.id.grid);
		((GridView) listView).setAdapter(new ImageAdapter(getActivity()));
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.i("pos - {}", String.valueOf(position));
				startImagePagerActivity(position, latitude, longitude);
			}
		});

		return rootView;
	}

	//Downloading data asynchronously
	public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			Integer result = 0;
			try {
				instagram = new Instagram(AccessToken, RestAdapter.LogLevel.BASIC);
				long min = (System.currentTimeMillis()/ 1000L) - 1000000000;
				long max = System.currentTimeMillis() / 1000L;
				final SearchMediaResponse response = instagram.getMediaEndpoint().search(5000 ,longitude, latitude);
//                if (response.getMediaList() != null) {
//                    for (Media media : response.getMediaList()) {
//                        logger.info("link: {}", media.getLink());
//                    }
				VK vk = new VK(RestAdapter.LogLevel.FULL);
				VkResponse resp = vk.getUsersEndpoint().search(latitude, longitude, 1000, 5000, 5.37);
				parseResultVk(resp);
				parseResult(response);
				result = 1; // Successful
			} catch (Exception e) {
				String msg = (e.getMessage()==null)?"Login failed!":e.getMessage();
				Log.i("error", msg);
				result = 0; //"Failed
			}

			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// Download complete. Lets update UI

			if (result == 1) {
				((ImageAdapter)listView.getAdapter()).setIMAGE_URLS(imagesList);
			} else {
//				Toast.makeText(GridViewActivity.this, "Failed1 to fetch data!", Toast.LENGTH_SHORT).show();
			}

			//Hide progressbar
//			mProgressBar.setVisibility(View.GONE);
		}
	}

	/**
	 * Parsing the feed results and get the list
	 *
	 * @param popular
	 */
	private void parseResult(SearchMediaResponse popular) {
		if (popular.getMediaList() != null) {
			for (Media media : popular.getMediaList()) {
				Log.i("link:", media.getImages().getLowResolution().getUrl());
				imagesList.add(media.getImages().getStandardResolution().getUrl());
			}
		}
	}

	/**
	 * Parsing the feed results and get the list
	 *
	 * @param popular
	 */
	private  void parseResultVk(VkResponse popular) {
		if (popular.getResponse().getItems() != null) {
			for (Items media : popular.getResponse().getItems()) {
				Log.i("link:", media.getPhoto_130());
				imagesList.add(media.getPhoto_604());
			}
		}
	}

	private static class ImageAdapter extends BaseAdapter {

		public void setIMAGE_URLS(ArrayList<String> IMAGE_URLS) {
			this.IMAGE_URLS = IMAGE_URLS;
			notifyDataSetChanged();
		}

		private  ArrayList<String> IMAGE_URLS = new ArrayList<>();


		private LayoutInflater inflater;

		private DisplayImageOptions options;

		ImageAdapter(Context context) {
			inflater = LayoutInflater.from(context);

			options = new DisplayImageOptions.Builder()
					.showImageOnLoading(R.drawable.ic_stub)
					.showImageForEmptyUri(R.drawable.ic_empty)
					.showImageOnFail(R.drawable.ic_error)
					.cacheInMemory(true)
					.cacheOnDisk(true)
					.considerExifParams(true)
					.bitmapConfig(Bitmap.Config.RGB_565)
					.build();
		}

		@Override
		public int getCount() {
			return IMAGE_URLS.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			View view = convertView;
			if (view == null) {
				view = inflater.inflate(R.layout.item_grid_image, parent, false);
				holder = new ViewHolder();
				assert view != null;
				holder.imageView = (ImageView) view.findViewById(R.id.image);
				holder.progressBar = (ProgressBar) view.findViewById(R.id.progress);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}
            Log.i("position {}", String.valueOf(position));
			ImageLoader.getInstance()
					.displayImage(IMAGE_URLS.get(position), holder.imageView, options, new SimpleImageLoadingListener() {
						@Override
						public void onLoadingStarted(String imageUri, View view) {
							holder.progressBar.setProgress(0);
							holder.progressBar.setVisibility(View.VISIBLE);
						}

						@Override
						public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
							holder.progressBar.setVisibility(View.GONE);
						}

						@Override
						public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
							holder.progressBar.setVisibility(View.GONE);
						}
					}, new ImageLoadingProgressListener() {
						@Override
						public void onProgressUpdate(String imageUri, View view, int current, int total) {
							holder.progressBar.setProgress(Math.round(100.0f * current / total));
						}
					});

			return view;
		}
	}

	static class ViewHolder {
		ImageView imageView;
		ProgressBar progressBar;
	}
}