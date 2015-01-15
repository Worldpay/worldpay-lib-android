package com.example.worldfood;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * This sample is an example of a take away food application.
 * <p>
 * At the home page screen (StartActivity) there is a static listview with three options. <br >
 * By pressing the order button user can select an item from the list.<br >
 * For the current purposes the quantity will always be one. <br >
 * <p>
 * <h3>More</h3>
 * <p>
 * This screen also contains a view pager with three images related to the items of the list.
 */
public class StartActivity extends Activity implements OnClickListener {
	// Images for the view pager
	public int[] viewPagerImages = new int[] { R.drawable.roastbeef, R.drawable.paella, R.drawable.moussaka, };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		ActionBar bar = getActionBar();
		bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		bar.setCustomView(R.layout.custom_action_bar);

		ViewPager pager = (ViewPager) findViewById(R.id.viewpager);
		ImagePagerAdapter adapter = new ImagePagerAdapter();
		pager.setAdapter(adapter);

		//get data of the list 
		ArrayList<Dish> dishes = initializeDishes();
		FoodListAdapter adapterList = new FoodListAdapter(dishes, getLayoutInflater(), this);

		ListView foodList = (ListView) findViewById(android.R.id.list);
		foodList.setAdapter(adapterList);
	}

	/*
	 * This adapter is responsible for the images in ViewPager.
	 */
	private class ImagePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return viewPagerImages.length;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == ((ImageView) object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Context context = getApplication();
			ImageView imageView = new ImageView(context);

			imageView.setPadding(0, 0, 0, 0);
			imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
			imageView.setImageResource(viewPagerImages[position]);
			((ViewPager) container).addView(imageView, 0);

			return imageView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((ImageView) object);
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		/*
		 * User clicked to order something, so we pass data of the dish and start a new activity 
		 */
			case R.id.orderButton:
				Dish dish = (Dish) v.getTag();
				if (dish != null) {
					Intent detailsIntent = new Intent(this, DetailsActivity.class);

					detailsIntent.putExtra("dish", dish.getName());
					detailsIntent.putExtra("price", dish.getPrice());
					detailsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(detailsIntent);
				}
				break;

		}
	}

	
	/*
	 * Initialize the data of the dishes
	 */
	private ArrayList<Dish> initializeDishes() {
		ArrayList<Dish> dishes = new ArrayList<Dish>();

		dishes.add(new Dish("Rare roast topside with Marmite & sweet onion gravy", "Roast beef", "£9.90", R.drawable.uk));
		dishes.add(new Dish("Chicken, seafood, vegetables, rice", "Paella", "£11.20", R.drawable.spain));
		dishes.add(new Dish(
				"Classic baked dish of layered ground beef,tomato, and sweet onion finished with bechamel sauce. Served with house salad",
				"Mousaka", "£8.50", R.drawable.greece));

		return dishes;
	}

}
