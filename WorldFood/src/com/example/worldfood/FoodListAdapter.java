package com.example.worldfood;

import java.util.ArrayList;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FoodListAdapter extends BaseAdapter {

	private ArrayList<Dish> dishes;
	private static LayoutInflater inflater = null;
	private OnClickListener clickListener;

	public FoodListAdapter(ArrayList<Dish> dishes , LayoutInflater layoutInflater,  OnClickListener clickListener) {
		this.dishes = dishes;
		inflater = layoutInflater;
		this.clickListener = clickListener;
	}

	public int getCount() {
		if (dishes != null)
			return dishes.size();
		return 0;
	}

	public Object getItem(int position) {
		if (dishes != null)
			return dishes.get(position);
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null)
			vi = inflater.inflate(R.layout.list_row, null);
		
		Dish dish = dishes.get(position);

		if (dish != null) {
			TextView foodName = (TextView) vi.findViewById(R.id.foodName);
			TextView foodDesc = (TextView) vi.findViewById(R.id.foodDesc);
			TextView price = (TextView) vi.findViewById(R.id.price);
			ImageView flag = (ImageView) vi.findViewById(R.id.flagImg);
			Button orderButton = (Button) vi.findViewById(R.id.orderButton);

			// Setting all values in listview

			foodName.setText(dish.getName());
			foodDesc.setText(dish.getDescription());
			price.setText(dish.getPrice());
			flag.setImageResource(dish.getFlagImageRecource());

			orderButton.setTag(dish);
			orderButton.setOnClickListener(clickListener);
		}

		return vi;
	}

}