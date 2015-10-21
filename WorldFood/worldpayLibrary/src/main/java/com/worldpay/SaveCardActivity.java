package com.worldpay;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An activity that shows a dialog to add card details and creates a
 * transaction.
 * <p>
 * <p>
 * The form contains information for : <br>
 * <ul>
 * <li>First name</li>
 * <li>Last name</li>
 * <li>Card Number</li>
 * <li>Expire date (month/year)</li>
 * <li>CVC</li>
 * </ul>
 * <p>
 * <p>
 * <h3>Usage</h3>
 * <ol>
 * <li>Declare the activity in AndroidManifest.xml
 * <p>
 * <pre>
 *       &lt;activity android:name="com.worldpay.SaveCardActivity"   android:label="Card Details" /&gt;
 * </pre>
 * <p>
 * </li>
 * <p>
 * <li>Open the activity :
 * <p>
 * <pre>
 *
 * Intent intent = new Intent(myactivity, SaveCardActivity.class);
 * // SAVE_CARD_REQUEST_CODE a custom request code for that activity
 * startActivityForResult(intent, SAVE_CARD_REQUEST_CODE);
 * </pre>
 * <p>
 * </li>
 * <p>
 * <li>Handle activity response implementing {@link Activity#onActivityResult}
 * <p>
 * The developer uses Intent parameter from {@link Activity#onActivityResult} to
 * retrieve more information about the result.
 * </p>
 * <p>
 * <p>
 * </li>
 * </ol>
 * <p>
 * <p>
 * <h4>Theme Customization</h4>
 * <p>
 * The developer can choose a theme by passing a bundle property the intent. The
 * property name is {@link #EXTRA_CUSTOMIZE_THEME} and a color int need to be
 * passed.
 * <p>
 * <pre>
 *
 * intent.putExtra(SaveCardActivity.EXTRA_CUSTOMIZE_THEME,
 * 		Color.parseColor(&quot;#ff0000&quot;));
 * </pre>
 * <p>
 * <p>
 * <h4>Results</h4>
 * <p>
 * The activity returns the results with a result code and as extras into the
 * Intent parameter of onActivityResult.<br>
 * Possible results :
 * <ul>
 * <li>{@link #RESULT_RESPONSE_CARD} : <br>
 * The activity had a successful transaction and returns result of a
 * {@link ResponseCard}. <br>
 * It can be accessed with a statement: <br>
 * <p>
 * <pre>
 *
 * ResponseCard responseCard = (ResponseCard) data
 * 		.getSerializableExtra(SaveCardActivity.EXTRA_RESPONSE_CARD);
 * </pre>
 * <p>
 * </li>
 * <li>{@link #RESULT_RESPONSE_ERROR} : <br>
 * An error occured , the activity returns details in a {@link ResponseError}
 * object.<br>
 * <p>
 * <pre>
 *
 * ResponseError responseCardError = (ResponseError) data
 * 		.getSerializableExtra(SaveCardActivity.EXTRA_RESPONSE_ERROR);
 * </pre>
 * <p>
 * </li>
 * <li>{@link #RESULT_WORLDPAY_ERROR} : <br>
 * Generic worldpay error, details can be found in a {@link WorldPayError}:
 * <p>
 * <pre>
 *
 * WorldPayError worldPayError = (WorldPayError) data
 * 		.getSerializableExtra(SaveCardActivity.EXTRA_RESPONSE_WORLDPAY_ERROR);
 * </pre>
 * <p>
 * </li>
 * </ul>
 */

public class SaveCardActivity extends Activity implements OnClickListener {

    /**
     * Code to retrieve {@link ResponseCard} object from Intent as
     * {@link Serializable} object.
     */
    public final static String EXTRA_RESPONSE_CARD = "com.worldpay.ResponseCard";

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
     * a {@link ResponseCard}.
     */
    public final static int RESULT_RESPONSE_CARD = 10000;

    /**
     * Response code on {@link Activity#onActivityResult}, the response contains
     * a {@link ResponseError}.
     */
    public final static int RESULT_RESPONSE_ERROR = 10001;

    /**
     * Response code on {@link Activity#onActivityResult}, the response contains
     * a {@link WorldPayError}.
     */
    public final static int RESULT_WORLDPAY_ERROR = 10002;

    /**
     * Use that extra to customize the theme
     */
    public final static String EXTRA_CUSTOMIZE_THEME = "com.worldpay.THEME_COLOR";

    /**
     * Default theme color
     */
    protected final static int THEME_LIGHT = Color.parseColor("#ff33b5e5");

    /**
     * Credit card patterns
     */
    private static final String PATTERN_MAESTRO_CARDTYPE = "^(5018|5020|5038|5612|5893|6304|6759|6761|6762|6763|0604|6390|6799)\\d+$";

    private static final String PATTERN_VISA_CARDTYPE = "^4[0-9]{12}(?:[0-9]{3})?$";

    private static final String PATTERN_MASTERCARD_CARDTYPE = "^5[1-5][0-9]{14}$";

    private static final String PATTERN_AMEX_CARDTYPE = "^3[47][0-9]{13}$";

    private static final String MAESTRO = "maestro";

    private static final String AMEX = "amex";

    private static final String VISA = "visa";

    private static final String MASTERCARD = "mastercard";

    // key value pairs of pattern and card names
    private final static String[][] CREDITCARD_PATTERNS = {
            {PATTERN_MAESTRO_CARDTYPE, MAESTRO},
            {PATTERN_VISA_CARDTYPE, VISA}, //
            {PATTERN_MASTERCARD_CARDTYPE, MASTERCARD}, //
            {PATTERN_AMEX_CARDTYPE, AMEX}, //

    };

    private String fName;

    private String lName;

    private String cNumber;

    private String cvc;

    private String expMonth;

    private String expYear;

    private String name = "";

    private EditText fNameText;

    private EditText lNameText;

    private EditText cNumberText;

    private EditText expiryText;

    private EditText cvcText;

    // save the theme
    private int theme;
    private TextWatcher expiryTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            int l = s.length();

            if (l >= 2 && !s.toString().contains("/") && start != l) {
                if (l == 2) {
                    expiryText.setText(s + "/");
                } else {
                    expiryText.setText(s.subSequence(0, 2) + "/"
                            + s.subSequence(2, l));
                }
                expiryText.setSelection(l + 1); // go to the end
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    /**
     * TextWatcher for the card number
     */
    private TextWatcher cardNumberTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            String numberString = cNumberText.getText().toString();

            int length = numberString.length();

            if ((length == 5 || length == 10 || length == 15 || length == 20)
                    && start != length && !numberString.endsWith(" ")) {

                cNumberText.setText(numberString.substring(0, length - 1) + " "
                        + numberString.subSequence(length - 1, length));
                cNumberText.setSelection(length + 1);
            }
            if (length > 12) {
                findCardType(numberString.replaceAll("\\s", ""));
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    public static void requestCVC(final Activity activity, LayoutInflater inflater,
                                  final String token, final String clientKey, final WorldPayResponseReusableToken worldPayResponceReusable) {

        View dialogLayout = inflater.inflate(R.layout.dialog_layout, null);

        final EditText cvcEditText = (EditText) dialogLayout
                .findViewById(R.id.cvcEditText);

        new AlertDialog.Builder(activity).setTitle(R.string.enterCVC)
                .setView(dialogLayout)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Card.validateCVC(cvcEditText.getText().toString())) {
                            ReusableToken reusableToken = new ReusableToken();

                            reusableToken.setClientKey(clientKey).setToken(token)
                                    .setCvc(cvcEditText.getText().toString());

                            reuseToken(activity, reusableToken, worldPayResponceReusable);
                        } else {
                            Toast.makeText(activity, CardValidationError.getDescription(CardValidationError.ERROR_CVC), Toast.LENGTH_SHORT).show();
                        }

                    }

                }).show();

    }

    private static void reuseToken(final Activity activity, ReusableToken reusableToken, WorldPayResponseReusableToken worldPayResponceReusable) {
        WorldPay worldPay = WorldPay.getInstance();

        AsyncTask<Void, Void, HttpServerResponse> reuseTokenAsyncTask = worldPay
                .reuseTokenAsyncTask(activity, reusableToken, worldPayResponceReusable);

        if (reuseTokenAsyncTask != null) {
            reuseTokenAsyncTask.execute();
        }
    }

    protected static Bitmap colorizeBitmap(Resources res, int bitmapResource,
                                           int desiredColor) {

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            theme = extras.getInt(EXTRA_CUSTOMIZE_THEME);
        } else {
            // no parameter passed -- default theme
            theme = THEME_LIGHT;
        }

        // first set a theme on the app
        if (theme != THEME_LIGHT) {
            setTheme(R.style.AppBaseThemeDark);
        }

        ActionBar bar = getActionBar();
        bar.setTitle(getString(R.string.cardDetails));

        // action bar background
        if (theme != THEME_LIGHT) {
            bar.setBackgroundDrawable(new ColorDrawable(theme));
        }

        setContentView(R.layout.save_on_card);

        fNameText = (EditText) findViewById(R.id.firstNameEditText);
        lNameText = (EditText) findViewById(R.id.lastNameEditText);
        cNumberText = (EditText) findViewById(R.id.cardNumberEditText);
        expiryText = (EditText) findViewById(R.id.expiryEditText);
        cvcText = (EditText) findViewById(R.id.cvcEditText);

        ImageView secureButton = (ImageView) findViewById(R.id.secureButton);

        if (theme == THEME_LIGHT) {
            secureButton.setImageResource(R.drawable.padlock_black_medium);
        } else {
            secureButton.setImageBitmap(colorizeBitmap(getResources(),
                    R.drawable.padlock_black_medium, theme));
        }

        secureButton.setOnClickListener(this);
        findViewById(R.id.textSecure).setOnClickListener(this);

		/*
         * A textwatcher for the expiry edit text in order two add "/" after the
		 * first two characters.
		 */
        expiryText.addTextChangedListener(expiryTextWatcher);

		/*
         * A textwatcher for the card number edit text in order to add space
		 * every 4 characters. While typing it shows, on the right of the
		 * editext, the icon of the type of the card. It also does not allow to
		 * type any more characters when the length is more than 23(19
		 * characters + 4 space characters).
		 */
        cNumberText.addTextChangedListener(cardNumberTextWatcher);

        cNumberText.setNextFocusDownId(R.id.expiryEditText);
        expiryText.setNextFocusDownId(R.id.cvcEditText);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

		/*
         * Secure buttons shows the pop
		 */
        if (id == R.id.textSecure || id == R.id.secureButton) {
            new DialogMessage(SaveCardActivity.this, theme).show();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // icon in action bar clicked; goto parent activity.
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Checks the type of the card and sets the right icon.
     */
    private void findCardType(String cardNumber) {
        boolean found = false;
        for (int i = 0; i < CREDITCARD_PATTERNS.length; i++) {
            String pattern = CREDITCARD_PATTERNS[i][0];
            Pattern r = Pattern.compile(pattern);

            // Now create matcher object.
            Matcher m = r.matcher(cardNumber);

            if (m.find()) {
                int id = this.getResources().getIdentifier(
                        "card_" + CREDITCARD_PATTERNS[i][1], "drawable",
                        this.getPackageName());
                Drawable cardIcon = this.getResources().getDrawable(id);
                cNumberText.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        cardIcon, null);
                found = true;
                break;
            }
        }

        if (!found) {
            // finally reset it
            cNumberText.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    null, null);
        }

    }

    private void newCard() {
        WorldPay worldPay = WorldPay.getInstance();

        Card card = new Card();

        CardValidationError validate = card.setHolderName(name).//
                setCardNumber(cNumber)//
                .setCvc(cvc).setExpiryYear(expYear).setExpriryMonth(expMonth)//
                .validate();

        Drawable errorIcon = getResources().getDrawable(R.drawable.rederror);

        if (validate != null) {

            /**
             * Checks if there are any errors in user's data. In case there is,
             * the error is highlighted with an icon on the appropriate edit
             * text.
             */
            if (validate.hasErrors()) {
                if (validate.hasError(CardValidationError.ERROR_CARD_EXPIRY)) {
                    // something is wrong with expiry date
                    Toast.makeText(getApplicationContext(), CardValidationError.getDescription(CardValidationError.ERROR_CARD_EXPIRY), Toast.LENGTH_SHORT).show();

                    expiryText.setCompoundDrawablesWithIntrinsicBounds(null, null, errorIcon, null);
                }

                if (validate.hasError(CardValidationError.ERROR_HOLDER_NAME)) {
                    // something is wrong with the holder name
                    Toast.makeText(getApplicationContext(), CardValidationError.getDescription(CardValidationError.ERROR_HOLDER_NAME), Toast.LENGTH_SHORT).show();

                    fNameText.setCompoundDrawablesWithIntrinsicBounds(null, null, errorIcon, null);

                    lNameText.setCompoundDrawablesWithIntrinsicBounds(null, null, errorIcon, null);
                }

                if (validate.hasError(CardValidationError.ERROR_CVC)) {
                    // something is wrong with cvc
                    Toast.makeText(getApplicationContext(), CardValidationError.getDescription(CardValidationError.ERROR_CVC), Toast.LENGTH_SHORT).show();

                    cvcText.setCompoundDrawablesWithIntrinsicBounds(null, null, errorIcon, null);
                }

                if (validate.hasError(CardValidationError.ERROR_CARD_NUMBER)) {
                    // something is wrong with card number
                    Toast.makeText(getApplicationContext(), CardValidationError.getDescription(CardValidationError.ERROR_CARD_NUMBER), Toast.LENGTH_SHORT).show();

                    cNumberText.setCompoundDrawablesWithIntrinsicBounds(null, null, errorIcon, null);
                }

            }

        } else {

            AsyncTask<Void, Void, HttpServerResponse> createTokenAsyncTask = worldPay
                    .createTokenAsyncTask(this, card, new WorldPayResponse() {

                        @Override
                        public void onSuccess(ResponseCard responseCard) {
                            DebugLogger.d("# onSuccess : " + responseCard);
                            finishActivity(RESULT_RESPONSE_CARD, EXTRA_RESPONSE_CARD, responseCard);
                        }

                        @Override
                        public void onResponseError(ResponseError responseError) {
                            DebugLogger.d("# onResponseError: " + responseError.getMessage());
                            finishActivity(RESULT_RESPONSE_ERROR, EXTRA_RESPONSE_ERROR, responseError);
                        }

                        @Override
                        public void onError(WorldPayError worldPayError) {
                            DebugLogger.d("# onError: " + worldPayError.getMessage());

                            finishActivity(RESULT_WORLDPAY_ERROR, EXTRA_RESPONSE_WORLDPAY_ERROR, worldPayError);
                        }

                    });

            if (createTokenAsyncTask != null) {
                createTokenAsyncTask.execute();
            }
        }

    }

	/*
     * TextWatchers
	 */

    private void finishActivity(int resultCode, String resultExtraName,
                                Serializable serializable) {
        Intent returnIntent = new Intent();
        if (resultExtraName != null) {
            returnIntent.putExtra(resultExtraName, serializable);
        }
        setResult(resultCode, returnIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.savecard, menu);
        MenuItem save = menu.findItem(R.id.action_save);
        Button itemSaveCard = (Button) save.getActionView();

        if (itemSaveCard != null) {
            itemSaveCard.setText(getResources().getString(R.string.saveCard));
            itemSaveCard.setBackgroundResource(R.drawable.ab_btn_bg);

            // show icon before text
            Drawable menuItemIcon;

            if (theme == THEME_LIGHT) {
                menuItemIcon = this.getResources().getDrawable(
                        R.drawable.save_cardblack);
                itemSaveCard.setTextColor(Color.parseColor("#000000"));
            } else {
                menuItemIcon = this.getResources().getDrawable(
                        R.drawable.save_cardwhite);
                itemSaveCard.setTextColor(Color.parseColor("#ffffff"));
            }

            // do it a little bit smaller
            // itemSaveCard.setTextSize(itemSaveCard.getTextSize() * 0.6f);
            itemSaveCard.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);

            itemSaveCard.setCompoundDrawablesWithIntrinsicBounds(menuItemIcon,
                    null, null, null);

            itemSaveCard.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    proccessCard();
                }
            });

        }
        return true;
    }

    private void proccessCard() {
        /*
         * If there is an error from previous try in this part the error icon is
		 * being removed
		 */
        expiryText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        fNameText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        lNameText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        cNumberText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        cvcText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

		/*
         *
		 */
        fName = fNameText.getText().toString();
        lName = lNameText.getText().toString();
        cNumber = cNumberText.getText().toString();
        String expiry = expiryText.getText().toString();
        cvc = cvcText.getText().toString();

		/*
         * The format for expire month and year must be XX/XXXX where the the
		 * first two characters are referring to month and the last four
		 * characters are referring to year. In any other case there is an
		 * error.
		 */
        if (expiry.length() == 7) {
            expMonth = expiry.substring(0, 2);
            expYear = expiry.substring(3, 7);
        } else {
            expMonth = "";
            expYear = "";
        }

        name = fName + "  " + lName;

        newCard();
    }

}
