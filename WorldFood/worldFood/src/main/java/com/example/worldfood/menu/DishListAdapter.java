package com.example.worldfood.menu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.worldfood.R;

final class DishListAdapter extends BaseAdapter {

    private final DishRepository dishRepository;
    private final LayoutInflater inflater;
    private final OnClickListener clickListener;

    DishListAdapter(final DishRepository dishRepository, final LayoutInflater layoutInflater,
                    final OnClickListener clickListener) {
        this.dishRepository = dishRepository;
        this.inflater = layoutInflater;
        this.clickListener = clickListener;
    }

    public int getCount() {
        return dishRepository.findAll().size();
    }

    public Object getItem(final int position) {
        return dishRepository.get(position);
    }

    public long getItemId(final int position) {
        return position;
    }

    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view = (convertView == null) ?
                inflater.inflate(R.layout.list_row_dish, null) :
                convertView;

        final Dish dish = dishRepository.get(position);

        if (dish != null) {
            final TextView foodName = (TextView) view.findViewById(R.id.foodName);
            foodName.setText(dish.getName());

            final TextView foodDesc = (TextView) view.findViewById(R.id.foodDesc);
            foodDesc.setText(dish.getDescription());

            final TextView price = (TextView) view.findViewById(R.id.price);
            price.setText(dish.getPrice());

            final ImageView flag = (ImageView) view.findViewById(R.id.flagImg);
            flag.setImageResource(dish.getCountryFlagImageId());

            final Button orderButton = (Button) view.findViewById(R.id.orderButton);
            orderButton.setTag(dish);
            orderButton.setOnClickListener(clickListener);
        }

        return view;
    }
}