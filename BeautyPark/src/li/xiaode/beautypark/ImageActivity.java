package li.xiaode.beautypark;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;



public class ImageActivity extends Activity {
	
	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		//Raw width and height of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float)height / (float)reqHeight);
			final int widthRatio = Math.round((float)width / (float)reqHeight);
			inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}
	
	public static Bitmap decodeSampleBitmapFromUrl(String url, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		
		Log.i("decodeSampleBitmapFromUrl", "url" + url);
		/*try {
			return BitmapFactory.decodeStream((InputStream)new URL(url).getContent());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;*/
		try {	
			BitmapFactory.decodeStream((InputStream)new URL(url).getContent(), null, options);
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeStream((InputStream)new URL(url).getContent(), null, options);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static DownloadImageTask getDownloadImageTask(ImageView view) {
		if (view != null) {
			final Drawable drawable = view.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable)drawable;
				return asyncDrawable.getDownloadImageTask();
			}
		}
		return null;
	}
	
	public static boolean cancelPotentialWork(String url, ImageView view){
		final DownloadImageTask task = getDownloadImageTask(view);
		
		if (task != null) {
			final String oldUrl = task.url;
			if (oldUrl != url) {
				task.cancel(true);
			} else {
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		String url = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
		
		//url = "http://image3.uuu9.com/war3/dota//UploadFiles_5254//201304/201304271757318511.jpg";
		ImageView imageView = new ImageView(this);
		
		if (cancelPotentialWork(url, imageView)) {
			final DownloadImageTask task = new DownloadImageTask(imageView);
			final AsyncDrawable drawable = new AsyncDrawable(task);
			imageView.setImageDrawable(drawable);
			task.execute(url);
		}
		setContentView(imageView);
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public class AsyncDrawable extends ColorDrawable {
		private final WeakReference<DownloadImageTask> downloadImageTaskReference;
		
		public AsyncDrawable(DownloadImageTask task) {
			downloadImageTaskReference = new WeakReference<DownloadImageTask>(task);
		}
		
		public DownloadImageTask getDownloadImageTask() {
			return downloadImageTaskReference.get();
		}
	}
	
	public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private String url;
		
		public DownloadImageTask(ImageView view) {
			imageViewReference = new WeakReference<ImageView>(view);
		}
		
		@Override
		protected Bitmap doInBackground(String... urls) {
			url = urls[0];
			Log.i("DownloadImageTask", "url" + url);
			Bitmap bitmap = decodeSampleBitmapFromUrl(url, 100, 100);
			return bitmap;
		}
		
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
			}
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				final DownloadImageTask task = getDownloadImageTask(imageView);
				if (this == task && imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}
}
