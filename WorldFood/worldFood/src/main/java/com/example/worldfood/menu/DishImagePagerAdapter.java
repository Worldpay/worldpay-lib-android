package com.example.worldfood.menu;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

final class DishImagePagerAdapter extends PagerAdapter {

    private final Context context;
    private final DishRepository dishRepository;

    DishImagePagerAdapter(final Context context, final DishRepository dishRepository) {
        this.context = context;
        this.dishRepository = dishRepository;
    }

    @Override
    public int getCount() {
        return dishRepository.findAll().size();
    }

    @Override
    public boolean isViewFromObject(final View view, final Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        final ImageView imageView = new ImageView(context);
        imageView.setPadding(0, 0, 0, 0);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imageView.setImageResource(dishRepository.get(position).getMealImageId());
        container.addView(imageView, 0);

        return imageView;
    }

    @Override
    public void destroyItem(final ViewGroup container, final int position, final Object object) {
        container.removeView((ImageView) object);
    }

}
