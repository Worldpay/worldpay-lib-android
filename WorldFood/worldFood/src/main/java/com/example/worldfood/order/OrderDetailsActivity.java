package com.example.worldfood.order;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.worldfood.R;
import com.worldpay.AlternativePaymentMethodToken;
import com.worldpay.Card;
import com.worldpay.ResponseCard;
import com.worldpay.ResponseError;
import com.worldpay.SaveAlternativePaymentMethodActivity;
import com.worldpay.SaveCardActivity;
import com.worldpay.WorldPay;
import com.worldpay.WorldPayError;
import com.worldpay.WorldPayResponseReusableToken;

import java.util.List;

import static android.app.ActionBar.DISPLAY_SHOW_CUSTOM;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static com.worldpay.SaveAlternativePaymentMethodActivity.EXTRA_RESPONSE_APM;
import static com.worldpay.SaveAlternativePaymentMethodActivity.RESULT_RESPONSE_APM;
import static com.worldpay.SaveCardActivity.EXTRA_CUSTOMIZE_THEME;
import static com.worldpay.SaveCardActivity.EXTRA_RESPONSE_CARD;
import static com.worldpay.SaveCardActivity.EXTRA_RESPONSE_ERROR;
import static com.worldpay.SaveCardActivity.EXTRA_RESPONSE_WORLDPAY_ERROR;
import static com.worldpay.SaveCardActivity.RESULT_RESPONSE_CARD;
import static com.worldpay.SaveCardActivity.RESULT_RESPONSE_ERROR;
import static com.worldpay.SaveCardActivity.RESULT_WORLDPAY_ERROR;


/**
 * {@link Activity} that informs the user of the dish they have chosen and the delivery address.
 * <p/>
 * The user can pay for the order by card or PayPal. Stored payment cards can be select or deleted.
 * New cards be added by navigating to the WorldPay Android SDK
 * {@link SaveCardActivity}.
 */
public class OrderDetailsActivity extends Activity implements OnClickListener {

    public static final String EXTRA_DISH_NAME = "dish";
    public static final String EXTRA_DISH_PRICE = "price";
    public static final String EXTRA_ORDER_DETAIL = "OrderDetails";
    public static final String EXTRA_THREE_DS_RESULT = "3DSresult";
    public static final String EXTRA_PAYPAL_RESULT = "payPalResult";
    public static final String SUCCESS = "SUCCESS";

    private static final String BLANK = "";
    private static final String TAG = "OrderDetailsActivity";
    private static final int SAVE_CARD_REQUEST_CODE = 1500;
    private static final int SAVE_APM_REQUEST_CODE = 1600;
    private static final int THREE_DS_REQUEST_CODE = 1337;
    private static final int PAYPAL_REQUEST_CODE = 1700;

    private final CardRepository cardRepository = new OnDiskCardRepository();
    private String clientKey;
    private boolean threeDsEnabled;
    private List<ResponseCard> savedCards;

    private String payPalRedirect;
    private String payPalPending;
    private String payPalSuccess;
    private String payPalCancel;
    private String payPalFailure;

    private EditText deliveryAddress;
    private EditText cityEditText;
    private EditText postcodeEditText;
    private TextView dishText;
    private TextView priceText;
    private TableLayout cardTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();

        final SharedPreferences sp = getDefaultSharedPreferences(getBaseContext());
        clientKey = sp.getString(getString(R.string.client_key), getString(R.string.client_key_value));
        threeDsEnabled = sp.getBoolean(getResources().getString(R.string.three_ds_key), false);
        final boolean dummyAddressEnabled = sp.getBoolean(getResources().getString(R.string.dummy_address_key), false);

        payPalRedirect = sp.getString(getString(R.string.paypal_redirect_url_key), getString(R.string.paypal_redirect_url_value));
        payPalPending = sp.getString(getString(R.string.paypal_pending_url_key), getString(R.string.paypal_pending_url_default));
        payPalSuccess = sp.getString(getString(R.string.paypal_success_url_key), getString(R.string.paypal_success_url_default));
        payPalCancel = sp.getString(getString(R.string.paypal_cancel_url_key), getString(R.string.paypal_cancel_url_default));
        payPalFailure = sp.getString(getString(R.string.paypal_failure_url_key), getString(R.string.paypal_failure_url_default));

        deliveryAddress = (EditText) findViewById(R.id.addressEditText);
        cityEditText = (EditText) findViewById(R.id.cityEditText);
        postcodeEditText = (EditText) findViewById(R.id.postcodeEditText);
        dishText = (TextView) findViewById(R.id.dishText);
        priceText = (TextView) findViewById(R.id.priceText);

        initDeliveryAddressPanel(dummyAddressEnabled);
        initOrderPanel();
        initPaymentMethodPanel();
        initWorldPay(clientKey);
    }

    private void initLayout() {
        setContentView(R.layout.activity_order_details);
        final ActionBar bar = getActionBar();
        if (bar != null) {
            bar.setDisplayOptions(DISPLAY_SHOW_CUSTOM);
            bar.setCustomView(R.layout.action_bar_worldfood_logo);
        }
    }

    private void initDeliveryAddressPanel(final boolean dummyAddressEnabled) {
        if (dummyAddressEnabled) {
            deliveryAddress.setText(R.string.dummy_address);
            cityEditText.setText(R.string.dummy_city);
            postcodeEditText.setText(R.string.dummy_postcode);
        }
    }

    private void initOrderPanel() {
        final String dish = getIntent().getExtras().getString(EXTRA_DISH_NAME);
        final String price = getIntent().getExtras().getString(EXTRA_DISH_PRICE);
        dishText.setText(dish);
        priceText.setText(price);
    }

    private void initPaymentMethodPanel() {
        cardTable = (TableLayout) findViewById(R.id.cardTable);
        cardTable.setStretchAllColumns(false);

        refreshStoredCardsPanel();

        final Button purchaseButton = (Button) findViewById(R.id.purchaseButton);
        purchaseButton.setOnClickListener(this);

        final Button addCardButton = (Button) findViewById(R.id.addCardButton);
        addCardButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                resetPayPalButton();
                final Intent intent = new Intent(OrderDetailsActivity.this, SaveCardActivity.class);
                intent.putExtra(EXTRA_CUSTOMIZE_THEME, Color.parseColor("#ff0000"));
                startActivityForResult(intent, SAVE_CARD_REQUEST_CODE);
            }
        });

        final Button buyWithPayPalButton = (Button) findViewById(R.id.buyWithPayPalButton);
        buyWithPayPalButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                clearCheckedSavedCard();
                final Button payPalButton = (Button) v.findViewById(R.id.buyWithPayPalButton);
                payPalButton.setTextColor(Color.BLACK);
                payPalButton.setBackground(getResources().getDrawable(R.drawable.roundwhite));
                payPalButton.setSelected(true);
            }
        });
    }

    private void initWorldPay(final String clientKey) {
        final WorldPay worldPay = WorldPay.getInstance();
        worldPay.setClientKey(clientKey);
        worldPay.setReusable(true);
        Card.setValidationType(Card.VALIDATION_TYPE_BASIC);
    }

    /**
     * On click handler for confirming a purchase.
     *
     * @param v Clicked {@link View}
     */
    @Override
    public void onClick(final View v) {
        final boolean buyWithPayPal = findViewById(R.id.buyWithPayPalButton).isSelected();

        if (noPaymentMethodSelected(buyWithPayPal)) {
            Toast.makeText(getApplicationContext(), R.string.select_payment_method, LENGTH_SHORT).show();
        } else if (noAddressProvided()) {
            Toast.makeText(getApplicationContext(), R.string.delivery_address_required, LENGTH_SHORT).show();
        } else if (buyWithPayPal) {
            final Intent intent = new Intent(OrderDetailsActivity.this, SaveAlternativePaymentMethodActivity.class);
            intent.putExtra(EXTRA_CUSTOMIZE_THEME, Color.parseColor("#ff0000"));
            startActivityForResult(intent, SAVE_APM_REQUEST_CODE);
        } else {
            final ResponseCard selectedCard = savedCards.get(getCheckedSavedCardPosition());
            if (selectedCard != null) {
                SaveCardActivity
                        .requestCVC(
                                OrderDetailsActivity.this,
                                getLayoutInflater(),
                                selectedCard.getToken(),
                                clientKey,
                                new WorldPayResponseReusableToken() {
                                    @Override
                                    public void onSuccess() {
                                        if (threeDsEnabled) {
                                            final OrderDetails orderDetails = new OrderDetails(
                                                    deliveryAddress.getText().toString(),
                                                    cityEditText.getText().toString(),
                                                    postcodeEditText.getText().toString(),
                                                    getPriceFromForm());
                                            final Intent orderIntent = new Intent(OrderDetailsActivity.this, ThreeDsOrderActivity.class);
                                            orderIntent.putExtra(EXTRA_ORDER_DETAIL, orderDetails);
                                            orderIntent.putExtra(ThreeDsOrderActivity.EXTRA_CARD_TOKEN, selectedCard.getToken());
                                            startActivityForResult(orderIntent, THREE_DS_REQUEST_CODE);
                                        } else {
                                            // At this point you can create an Order with the
                                            // new token via the WorldPay Orders API.
                                            new AlertDialog.Builder(OrderDetailsActivity.this)
                                                    .setTitle(R.string.confirm_purchase)
                                                    .setMessage(R.string.confirm_purchase_message)
                                                    .setPositiveButton(R.string.ok,
                                                            new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(final DialogInterface dialog, final int which) {
                                                                    startActivity(new Intent(getApplicationContext(), OrderConfirmationActivity.class));
                                                                }
                                                            }).show();
                                        }
                                    }

                                    @Override
                                    public void onResponseError(final ResponseError responseError) {
                                        showDialog(getString(R.string.response_error), responseError.getMessage());
                                    }

                                    @Override
                                    public void onError(final WorldPayError worldPayError) {
                                        showDialog(getString(R.string.worldpay_error), worldPayError.getMessage());
                                    }
                                });
            }
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        if (requestCode == SAVE_CARD_REQUEST_CODE) {
            handleCardTokenizationResult(resultCode, data);
        } else if (requestCode == SAVE_APM_REQUEST_CODE) {
            handlePayPalTokenizationResult(resultCode, data);
        } else if (requestCode == THREE_DS_REQUEST_CODE) {
            handleThreeDsResult(resultCode, data);
        } else if (requestCode == PAYPAL_REQUEST_CODE) {
            handlePayPalOrderResult(resultCode, data);
        }
    }

    private int getPriceFromForm() {
        return Integer.valueOf(((TextView) findViewById(R.id.priceText)).getText().toString()
                .substring(1).replace(".", ""));
    }

    /**
     * Handle response from a request to create a token. Stores the card information for use in
     * subsequent payments.
     *
     * @param resultCode The result code returned from {@link SaveCardActivity}
     * @param data       The result data returned.
     */
    private void handleCardTokenizationResult(final int resultCode, final Intent data) {
        switch (resultCode) {
            case RESULT_RESPONSE_CARD:
                final ResponseCard responseCard =
                        (ResponseCard) data.getSerializableExtra(EXTRA_RESPONSE_CARD);
                if (responseCard != null) {
                    savedCards.add(responseCard);
                    cardRepository.save(savedCards, OrderDetailsActivity.this);
                    refreshStoredCardsPanel();
                }
                break;
            case RESULT_RESPONSE_ERROR:
                final ResponseError rError = (ResponseError) data.getSerializableExtra(EXTRA_RESPONSE_ERROR);
                if (rError != null) {
                    showDialog(getString(R.string.response_error), rError.getMessage());
                }
                break;
            case RESULT_WORLDPAY_ERROR:
                final WorldPayError wError = (WorldPayError) data
                        .getSerializableExtra(EXTRA_RESPONSE_WORLDPAY_ERROR);
                if (wError != null) {
                    showDialog(getString(R.string.worldpay_error), wError.getMessage());
                }
                break;
        }
    }

    /**
     * Handle response from a request to create a PayPal token. Passes the order to be created to
     * the {@link PayPalOrderActivity}.
     *
     * @param resultCode The result code returned from {@link SaveAlternativePaymentMethodActivity}
     * @param data       The result data returned.
     */
    private void handlePayPalTokenizationResult(final int resultCode, final Intent data) {
        switch (resultCode) {
            case RESULT_RESPONSE_APM:
                final AlternativePaymentMethodToken payPalToken =
                        (AlternativePaymentMethodToken) data.getSerializableExtra(EXTRA_RESPONSE_APM);
                if (payPalToken != null) {
                    final Order order = Order.newPayPalOrder(
                            payPalToken.getToken(),
                            dishText.getText().toString(),
                            getPriceFromForm(),
                            "GBP",
                            payPalPending, payPalFailure, payPalCancel, payPalRedirect, payPalSuccess
                    );
                    final Intent payPalOrderIntent = new Intent(OrderDetailsActivity.this, PayPalOrderActivity.class);
                    payPalOrderIntent.putExtra(PayPalOrderActivity.EXTRA_APM_ORDER, order);
                    startActivityForResult(payPalOrderIntent, PAYPAL_REQUEST_CODE);
                }
                break;
            case SaveAlternativePaymentMethodActivity.RESULT_RESPONSE_ERROR:
                final ResponseError rError = (ResponseError) data.getSerializableExtra(SaveAlternativePaymentMethodActivity.EXTRA_RESPONSE_ERROR);
                if (rError != null) {
                    showDialog(getString(R.string.response_error), rError.getMessage());
                }
                break;
            case SaveAlternativePaymentMethodActivity.RESULT_WORLDPAY_ERROR:
                final WorldPayError wError = (WorldPayError) data
                        .getSerializableExtra(SaveAlternativePaymentMethodActivity.EXTRA_RESPONSE_WORLDPAY_ERROR);
                if (wError != null) {
                    showDialog(getString(R.string.worldpay_error), wError.getMessage());
                }
                break;
        }
    }

    /**
     * Handle the 3-D secure {@link ThreeDsOrderActivity} result.
     *
     * @param resultCode The result code returned from {@link ThreeDsOrderActivity}
     * @param data       The result data returned.
     */
    private void handleThreeDsResult(final int resultCode, final Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                final String result = data.getStringExtra(EXTRA_THREE_DS_RESULT);
                if (SUCCESS.equals(result)) {
                    startActivity(new Intent(OrderDetailsActivity.this, OrderConfirmationActivity.class));
                } else {
                    showDialog(getString(R.string.order_failed), getString(R.string.unable_to_order));
                }
                break;
            case RESULT_RESPONSE_ERROR:
                final ResponseError rError = (ResponseError) data.getSerializableExtra(EXTRA_RESPONSE_ERROR);
                if (rError != null) {
                    showDialog(getString(R.string.response_error), rError.getMessage());
                }
                break;
            case RESULT_WORLDPAY_ERROR:
                final WorldPayError wError = (WorldPayError) data
                        .getSerializableExtra(EXTRA_RESPONSE_WORLDPAY_ERROR);
                if (wError != null) {
                    showDialog(getString(R.string.worldpay_error), wError.getMessage());
                }
                break;
        }
    }

    /**
     * Handle the PayPal {@link PayPalOrderActivity} result.
     *
     * @param resultCode The result code returned from {@link PayPalOrderActivity}
     * @param data       The result data returned.
     */
    private void handlePayPalOrderResult(final int resultCode, final Intent data) {
        switch (resultCode) {
            case RESULT_OK:
                final String result = data.getStringExtra(EXTRA_PAYPAL_RESULT);
                if (SUCCESS.equals(result)) {
                    startActivity(new Intent(OrderDetailsActivity.this, OrderConfirmationActivity.class));
                } else {
                    Log.d(TAG, "Order was " + result);
                    showDialog(getString(R.string.order_failed), getString(R.string.unable_to_order));
                }
                break;
            case RESULT_RESPONSE_ERROR:
                final ResponseError rError = (ResponseError) data.getSerializableExtra(EXTRA_RESPONSE_ERROR);
                if (rError != null) {
                    showDialog(getString(R.string.response_error), rError.getMessage());
                }
                break;
            case RESULT_WORLDPAY_ERROR:
                final WorldPayError wError = (WorldPayError) data
                        .getSerializableExtra(EXTRA_RESPONSE_WORLDPAY_ERROR);
                if (wError != null) {
                    showDialog(getString(R.string.worldpay_error), wError.getMessage());
                }
                break;
        }
    }

    private boolean noPaymentMethodSelected(final boolean buyWithPayPal) {
        return (savedCards.isEmpty() || getCheckedSavedCardPosition() == -1) && !buyWithPayPal;
    }

    private boolean noAddressProvided() {
        return BLANK.equals(deliveryAddress.getText().toString().trim())
                || BLANK.equals(cityEditText.getText().toString().trim())
                || BLANK.equals(postcodeEditText.getText().toString().trim());
    }

    private void resetPayPalButton() {
        final Button buyWithPayPalButton = (Button) findViewById(R.id.buyWithPayPalButton);
        buyWithPayPalButton.setTextColor(Color.WHITE);
        buyWithPayPalButton.setBackground(getResources().getDrawable(R.drawable.roundgrey));
        buyWithPayPalButton.setSelected(false);
    }

    private void clearCheckedSavedCard() {
        for (int i = 0; i < cardTable.getChildCount(); i++) {
            final CheckBox cardCheckBox = (CheckBox) cardTable.getChildAt(i).findViewById(R.id.cardCheckBox);
            cardCheckBox.setChecked(false);
        }
    }

    private int getCheckedSavedCardPosition() {
        for (int i = 0; i < cardTable.getChildCount(); i++) {
            final CheckBox cardCheckBox = (CheckBox) cardTable.getChildAt(i)
                    .findViewById(R.id.cardCheckBox);
            if (cardCheckBox.isChecked()) {
                return i;
            }
        }
        return -1;
    }

    private void setCardDrawable(final ImageView cardImage, final String cardType) {
        Log.d(TAG, "Setting image for cardType " + cardType);
        switch (cardType) {
            case "MASTERCARD":
            case "MASTERCARD_DEBIT":
            case "MASTERCARD_CREDIT":
            case "MASTERCARD_CORPORATE_DEBIT":
            case "MASTERCARD_CORPORATE_CREDIT":
                cardImage.setImageDrawable(getResources().getDrawable(R.drawable.card_mastercard));
                break;
            case "VISA":
            case "VISA_DEBIT":
            case "VISA_CREDIT":
            case "VISA_CORPORATE_DEBIT":
            case "VISA_CORPORATE_CREDIT":
                cardImage.setImageDrawable(getResources().getDrawable(R.drawable.card_visa));
                break;
            case "MAESTRO":
                cardImage.setImageDrawable(getResources().getDrawable(R.drawable.card_maestro));
                break;
            case "AMEX":
                cardImage.setImageDrawable(getResources().getDrawable(R.drawable.card_amex));
                break;
        }
    }

    private void refreshStoredCardsPanel() {
        savedCards = cardRepository.findAll(this);
        if (savedCards.isEmpty()) {
            findViewById(R.id.storedPaymentMethods).setVisibility(INVISIBLE);
        } else {
            findViewById(R.id.storedPaymentMethods).setVisibility(VISIBLE);
        }
        cardTable.removeAllViews();
        for (int i = 0; i < savedCards.size(); i++) {
            createStoredCardRow(savedCards.get(i), i == 0);
        }
    }

    private void createStoredCardRow(final ResponseCard responseCard, final boolean flagChecked) {
        final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View view = inflater.inflate(R.layout.list_row_stored_card, null);
        cardTable.addView(view);

        final CheckBox cardCheckBox = (CheckBox) view.findViewById(R.id.cardCheckBox);
        cardCheckBox.setChecked(flagChecked);
        cardCheckBox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPayPalButton();
                clearCheckedSavedCard();
                ((CheckBox) v).setChecked(true);
            }
        });

        final ImageView cardImage = (ImageView) view.findViewById(R.id.cardImage);
        setCardDrawable(cardImage, responseCard.getCardType());

        final Button deleteButton = (Button) view.findViewById(R.id.deleteButton);
        deleteButton.setTag(responseCard);
        deleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                final ResponseCard responseCard = (ResponseCard) v.getTag();
                if (responseCard != null) {
                    new AlertDialog.Builder(OrderDetailsActivity.this)
                            .setIcon(R.drawable.ic_dialog_alert_holo_light)
                            .setTitle(R.string.delete_card_title)
                            .setMessage(R.string.delete_card_message)
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog,
                                                    final int which) {
                                    refreshStoredCardsPanel();
                                }
                            })
                            .setPositiveButton(R.string.yes,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(final DialogInterface dialog,
                                                            final int which) {
                                            final View row = (View) v.getParent();
                                            final ViewGroup container = ((ViewGroup) row.getParent());
                                            container.removeView(row);
                                            container.invalidate();
                                            savedCards.remove(responseCard);
                                            cardRepository.save(savedCards, OrderDetailsActivity.this);
                                            refreshStoredCardsPanel();
                                        }
                                    }).show();
                }
            }
        });

        final TextView numberCard = (TextView) view.findViewById(R.id.cardNumber);
        numberCard.setText(responseCard.getMaskedCardNumber());
    }

    private void showDialog(final String title, final String message) {
        new AlertDialog.Builder(OrderDetailsActivity.this)
                .setTitle(title)
                .setMessage(message)
                .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }
}
