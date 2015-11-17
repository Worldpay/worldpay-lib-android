package com.worldpay;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.Serializable;

import static android.widget.Toast.LENGTH_SHORT;
import static com.worldpay.AlternativePaymentMethodValidationError.ERROR_APM_NAME;
import static com.worldpay.AlternativePaymentMethodValidationError.ERROR_NAME;
import static com.worldpay.AlternativePaymentMethodValidationError.ERROR_SHOPPER_COUNTRY_CODE;
import static com.worldpay.AlternativePaymentMethodValidationError.getDescription;

/**
 * An {@link Activity} that allows a user to enter APM details and create a token.
 * <p>
 * <p>
 * The form contains information for : <br>
 * <ul>
 * <li>First name</li>
 * <li>Last name</li>
 * <li>APM Type (paypal)</li>
 * <li>Shopper Country Code</li>
 * </ul>
 * <p>
 * <p>
 * <h3>Usage</h3>
 * <ol>
 * <li>Declare the activity in AndroidManifest.xml
 * <p>
 * <pre>
 *       &lt;activity android:name="com.worldpay.SaveAlternativePaymentMethodActivity"   android:label="APM Details" /&gt;
 * </pre>
 * <p>
 * </li>
 * <p>
 * <li>Open the activity :
 * <p>
 * <pre>
 *
 * Intent intent = new Intent(myactivity, SaveAlternativePaymentMethodActivity.class);
 * // SAVE_APM_REQUEST_CODE a custom request code for that activity
 * startActivityForResult(intent, SAVE_APM_REQUEST_CODE);
 * </pre>
 * <p>
 * </li>
 * <p>
 * <li>Handle activity response implementing {@link Activity#onActivityResult}
 * <p>
 * The developer uses Intent parameter from {@link Activity#onActivityResult} to
 * retrieve more information about the result.
 * </p>
 * <p/>
 * <p/>
 * </li>
 * </ol>
 * <p/>
 * <p/>
 * <h4>Theme Customization</h4>
 * <p/>
 * The developer can choose a theme by passing a bundle property the intent. The
 * property name is {@link #EXTRA_CUSTOMIZE_THEME} and a color int need to be
 * passed.
 * <p/>
 * <pre>
 *
 * intent.putExtra(SaveAlternativePaymentMethodActivity.EXTRA_CUSTOMIZE_THEME,
 * 		Color.parseColor(&quot;#ff0000&quot;));
 * </pre>
 * <p/>
 * <p/>
 * <h4>Results</h4>
 * <p/>
 * The activity returns the results with a result code and as extras into the
 * Intent parameter of onActivityResult.<br>
 * Possible results :
 * <ul>
 * <li>{@link #RESULT_RESPONSE_APM} : <br>
 * The activity had a successful transaction and returns result of a
 * {@link AlternativePaymentMethodToken}. <br>
 * It can be accessed with a statement: <br>
 * <p/>
 * <pre>
 * AlternativePaymentMethodToken apmToken = (ResponseCard) data
 * 		.getSerializableExtra(SaveAlternativePaymentMethodActivity.EXTRA_RESPONSE_APM);
 * </pre>
 * <p/>
 * </li>
 * <li>{@link #RESULT_RESPONSE_ERROR} : <br>
 * An error occured , the activity returns details in a {@link ResponseError}
 * object.<br>
 * <p/>
 * <pre>
 * ResponseError responseError = (ResponseError) data
 * 		.getSerializableExtra(SaveAlternativePaymentMethodActivity.EXTRA_RESPONSE_ERROR);
 * </pre>
 * <p/>
 * </li>
 * <li>{@link #RESULT_WORLDPAY_ERROR} : <br>
 * Generic worldpay error, details can be found in a {@link WorldPayError}:
 * <p/>
 * <pre>
 *
 * WorldPayError worldPayError = (WorldPayError) data
 * 		.getSerializableExtra(SaveAlternativePaymentMethodActivity.EXTRA_RESPONSE_WORLDPAY_ERROR);
 * </pre>
 * <p/>
 * </li>
 * </ul>
 */
public class SaveAlternativePaymentMethodActivity extends Activity implements OnClickListener {

    /**
     * Code to retrieve {@link AlternativePaymentMethodToken} object from Intent as
     * {@link Serializable} object.
     */
    public final static String EXTRA_RESPONSE_APM = "com.worldpay.AlternativePaymentMethodToken";

    /**
     * Code to retrieve {@link ResponseError} object from Intent as
     * {@link Serializable} object.
     */
    public final static String EXTRA_RESPONSE_ERROR = "com.worldpay.ResponseError";

    /**
     * Code to retrieve {@link WorldPayError} object from Intent as
     * {@link Serializable} object.
     */
    public final static String EXTRA_RESPONSE_WORLDPAY_ERROR = "com.worldpay.WorldPayError";

    /**
     * Response code on {@link Activity#onActivityResult}, the response contains
     * a {@link AlternativePaymentMethodToken}.
     */
    public final static int RESULT_RESPONSE_APM = 20000;

    /**
     * Response code on {@link Activity#onActivityResult}, the response contains
     * a {@link ResponseError}.
     */
    public final static int RESULT_RESPONSE_ERROR = 20001;

    /**
     * Response code on {@link Activity#onActivityResult}, the response contains
     * a {@link WorldPayError}.
     */
    public final static int RESULT_WORLDPAY_ERROR = 20002;

    /**
     * Use that extra to customize the theme
     */
    public final static String EXTRA_CUSTOMIZE_THEME = "com.worldpay.THEME_COLOR";

    /**
     * Default theme color
     */
    protected final static int THEME_LIGHT = Color.parseColor("#ff33b5e5");

    private EditText fNameText;
    private EditText lNameText;
    private EditText apmTypeText;
    private EditText countryCodeType;

    private int theme;

    protected static Bitmap colorizeBitmap(final Resources res, final int bitmapResource,
                                           final int desiredColor) {
        int red = Color.red(desiredColor);
        int green = Color.green(desiredColor);
        int blue = Color.blue(desiredColor);

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap padlockBitmap = BitmapFactory.decodeResource(res,
                bitmapResource, opt);
        int width = padlockBitmap.getWidth();
        int height = padlockBitmap.getHeight();

        // extract pixels
        int[] pixels = new int[width * height];

        padlockBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < pixels.length; i++) {
            int alpha = Color.alpha(pixels[i]);
            if (alpha > 10) {
                pixels[i] = Color.argb(alpha, red, green, blue);
            }
        }
        Bitmap result = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);

        result.setPixels(pixels, 0, width, 0, 0, width, height);
        padlockBitmap.recycle();
        return result;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            theme = extras.getInt(EXTRA_CUSTOMIZE_THEME);
        } else {
            theme = THEME_LIGHT;
        }

        if (theme != THEME_LIGHT) {
            setTheme(R.style.AppBaseThemeDark);
        }

        setContentView(R.layout.save_apm);

        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setTitle(getString(R.string.apm_details));
            if (theme != THEME_LIGHT) {
                bar.setBackgroundDrawable(new ColorDrawable(theme));
            }
        }

        fNameText = (EditText) findViewById(R.id.firstNameEditText);
        lNameText = (EditText) findViewById(R.id.lastNameEditText);
        apmTypeText = (EditText) findViewById(R.id.apmEditText);
        countryCodeType = (EditText) findViewById(R.id.countryCodeEditText);

        final ImageView secureButton = (ImageView) findViewById(R.id.secureButton);

        if (theme == THEME_LIGHT) {
            secureButton.setImageResource(R.drawable.padlock_black_medium);
        } else {
            secureButton.setImageBitmap(colorizeBitmap(getResources(),
                    R.drawable.padlock_black_medium, theme));
        }

        secureButton.setOnClickListener(this);
        findViewById(R.id.textSecure).setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        int id = v.getId();
        if (id == R.id.textSecure || id == R.id.secureButton) {
            new DialogMessage(SaveAlternativePaymentMethodActivity.this, theme).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void newApm(final String name, final String apmType, final String countryCode) {
        final WorldPay worldPay = WorldPay.getInstance();
        final AlternativePaymentMethod alternativePaymentMethod = AlternativePaymentMethod.newApm(name, apmType, countryCode);
        final AlternativePaymentMethodValidationError validationError = alternativePaymentMethod.validate();

        Drawable errorIcon = getResources().getDrawable(R.drawable.rederror);

        if (validationError.hasErrors()) {
            if (validationError.hasError(ERROR_NAME)) {
                Toast.makeText(getApplicationContext(), getDescription(ERROR_APM_NAME), LENGTH_SHORT).show();

                fNameText.setCompoundDrawablesWithIntrinsicBounds(null, null, errorIcon, null);
                lNameText.setCompoundDrawablesWithIntrinsicBounds(null, null, errorIcon, null);
            }
            if (validationError.hasError(ERROR_APM_NAME)) {
                Toast.makeText(getApplicationContext(), getDescription(ERROR_APM_NAME), LENGTH_SHORT).show();

                apmTypeText.setCompoundDrawablesWithIntrinsicBounds(null, null, errorIcon, null);
            }
            if (validationError.hasError(ERROR_SHOPPER_COUNTRY_CODE)) {
                Toast.makeText(getApplicationContext(), getDescription(ERROR_SHOPPER_COUNTRY_CODE), LENGTH_SHORT).show();

                countryCodeType.setCompoundDrawablesWithIntrinsicBounds(null, null, errorIcon, null);
            }
        } else {
            final AsyncTask<Void, Void, HttpServerResponse> createTokenAsyncTask = worldPay
                    .createTokenAsyncTask(this, alternativePaymentMethod, new WorldPayApmResponse() {
                        @Override
                        public void onSuccess(final AlternativePaymentMethodToken apmToken) {
                            DebugLogger.d("# onSuccess : " + apmToken);
                            finishActivity(RESULT_RESPONSE_APM, EXTRA_RESPONSE_APM, apmToken);
                        }

                        @Override
                        public void onResponseError(final ResponseError responseError) {
                            DebugLogger.d("# onResponseError: " + responseError.getMessage());
                            finishActivity(RESULT_RESPONSE_ERROR, EXTRA_RESPONSE_ERROR, responseError);
                        }

                        @Override
                        public void onError(final WorldPayError worldPayError) {
                            DebugLogger.d("# onError: " + worldPayError.getMessage());

                            finishActivity(RESULT_WORLDPAY_ERROR, EXTRA_RESPONSE_WORLDPAY_ERROR, worldPayError);
                        }
                    });
            if (createTokenAsyncTask != null) {
                createTokenAsyncTask.execute();
            }
        }
    }

    private void finishActivity(final int resultCode, final String resultExtraName,
                                final Serializable serializable) {
        final Intent returnIntent = new Intent();
        if (resultExtraName != null) {
            returnIntent.putExtra(resultExtraName, serializable);
        }
        setResult(resultCode, returnIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.saveapm, menu);
        MenuItem save = menu.findItem(R.id.action_save_apm);
        Button itemSaveApm = (Button) save.getActionView();

        if (itemSaveApm != null) {
            itemSaveApm.setText(getResources().getString(R.string.saveApm));
            itemSaveApm.setBackgroundResource(R.drawable.ab_btn_bg);

            // show icon before text
            Drawable menuItemIcon;

            if (theme == THEME_LIGHT) {
                menuItemIcon = this.getResources().getDrawable(
                        R.drawable.save_cardblack);
                itemSaveApm.setTextColor(Color.parseColor("#000000"));
            } else {
                menuItemIcon = this.getResources().getDrawable(
                        R.drawable.save_cardwhite);
                itemSaveApm.setTextColor(Color.parseColor("#ffffff"));
            }

            itemSaveApm.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);

            itemSaveApm.setCompoundDrawablesWithIntrinsicBounds(menuItemIcon,
                    null, null, null);

            itemSaveApm.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    processApm();
                }
            });

        }
        return true;
    }

    private void processApm() {
        fNameText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        lNameText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        apmTypeText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        countryCodeType.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        final String name = fNameText.getText().toString() + " " + lNameText.getText().toString();
        final String apmType = apmTypeText.getText().toString();
        final String countryCode = countryCodeType.getText().toString();

        newApm(name, apmType, countryCode);
    }

}
