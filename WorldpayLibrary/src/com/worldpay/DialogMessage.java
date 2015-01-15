package com.worldpay;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A custom dialog in order to show Secure and Safe message.
 */
final class DialogMessage extends Dialog implements android.view.View.OnClickListener {
	private int color;

	public DialogMessage(Activity a, int color) {
		super(a);
		this.color = color;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.custom_dialog);

		ImageView imageTitle = (ImageView) findViewById(R.id.imageTitle);
		View customLine = (View) findViewById(R.id.customLine);
		TextView titleText = (TextView) findViewById(R.id.titleText);
		Button ok = (Button) findViewById(R.id.okButton);

		customLine.setBackground(new ColorDrawable(color));

		titleText.setTextColor(color);

		imageTitle.setImageBitmap(SaveCardActivity.colorizeBitmap(getContext().getResources(), R.drawable.padlock_black_medium,
				color));

		ok.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		dismiss();
	}

}