package com.example.worldfood;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.worldpay.Card;
import com.worldpay.ResponseCard;
import com.worldpay.ResponseError;
import com.worldpay.SaveCardActivity;
import com.worldpay.WorldPay;
import com.worldpay.WorldPayError;
import com.worldpay.WorldPayResponseReusableToken;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


/**
 * This class contains the dish that has been chosen, a form related to delivery
 * address and a form for the payment method. <br>
 * <h3>More Specific About Payment Method</h3><br >
 * It contains an add card button which calls the {@link SaveCardActivity} from
 * Worldpay library.<br >
 * As parameters are being sent the preferred color from the available and the
 * validation type. <br >
 * The card that is added is by default checked.<br >
 * <p>
 * When there are multiple cards available user can select only one. <br >
 * <br >
 * There is also a delete button which remove the card that has been selected.
 * <h3>Purchase Button</h3><br >
 * Purchase button completes the process. <br >
 * If there is no card available or no card has been checked the purchase stops
 * with an appropriate message. <br >
 * In any other case the process is successful.
 */
public class DetailsActivity extends Activity implements OnClickListener {


    // we use that file to get the 3DS order result
    public static final String THREE_DS_RESULT = "3DSresult";
    // we use that file to implement a 3DS order
    public static final String ORDER_DETAIL = "OrderDetails";
    // use this code in order to distinguish world pay call
    private final static int SAVE_CARD_REQUEST_CODE = 1500;
    // we use that file to save cards
    private final static String CARDS_FILE = "SavedCards";
    // use this code in order to distinguish 3DS call
    private static final int THREE_DS_RESULT_CODE = 1337;
    private ArrayList<ResponseCard> savedCards;

    private OrderDetails orderDetails;

    private TableLayout cardTable;

    private EditText deliveryAddress;

    private EditText cityEditText;

    private EditText postcodeEditText;

    private TextView dishText;

    private TextView priceText;

    private boolean is3DSenabled;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ActionBar bar = getActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        bar.setCustomView(R.layout.custom_action_bar);

        deliveryAddress = (EditText) findViewById(R.id.addressEditText);
        cityEditText = (EditText) findViewById(R.id.cityEditText);
        postcodeEditText = (EditText) findViewById(R.id.postcodeEditText);

        cardTable = (TableLayout) findViewById(R.id.cardTable);
        cardTable.setStretchAllColumns(false);

        dishText = (TextView) findViewById(R.id.dishText);
        priceText = (TextView) findViewById(R.id.priceText);

        Button addCardButton = (Button) findViewById(R.id.addCardButton);
        Button purchaseButton = (Button) findViewById(R.id.purchaseButton);

        purchaseButton.setOnClickListener(this);
        addCardButton.setOnClickListener(this);

        String dish = getIntent().getExtras().getString("dish");
        String price = getIntent().getExtras().getString("price");

        dishText.setText(dish);
        priceText.setText(price);

        // load saved cards
        savedCards = getSavedCards(this);
        if (savedCards == null) {
            savedCards = new ArrayList<ResponseCard>();
        }

		/*
         * Initialize the table with the cards
		 */
        boolean cardSelected = true;
        for (ResponseCard responseCard : savedCards) {
            createTableRow(responseCard, cardSelected);

            if (cardSelected) {
                // just select the first card
                cardSelected = false;
            }
        }


        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        is3DSenabled = SP.getBoolean(getResources().getString(R.string.three_ds_key), false);

        initWorldpay();
    }

    /**
     * Initialize and customize the Worldpay Library.
     */
    private void initWorldpay() {
        // set your client key
        String clientKey = "T_C_20a3f5ad-11f9-409c-b7b6-7171f5269354";

        // choose if we want to show debug messages to logcat
        WorldPay.setDebug(true);

        // get an instance
        WorldPay worldPay = WorldPay.getInstance();

        // set your client key
        worldPay.setClientKey(clientKey);

        // choose if the library with use the cards as useable or not
        worldPay.setReusable(true);

        // set a validation type -- default value is
        // Card.VALIDATION_TYPE_ADVANCED
        Card.setValidationType(Card.VALIDATION_TYPE_BASIC);

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        switch (id) {
        /*
         * Ask to delete a card
		 */
            case R.id.deleteButton:
                ResponseCard responseCard = (ResponseCard) v.getTag();

                if (responseCard != null) {
                    deleteCard(responseCard, v);
                }

                break;

            case R.id.cardCheckBox:
                resetCheckBoxes();
                ((CheckBox) v).setChecked(true);

                break;
            case R.id.addCardButton:
                addCard();
                break;
        /*
         * Checks if there is any selected card and creates a virtual purchase.
		 */
            case R.id.purchaseButton:
                if (savedCards == null || savedCards.size() == 0) {
                    Toast.makeText(getApplicationContext(), "You must add card first", Toast.LENGTH_SHORT).show();

                } else {
                    if (getCheckedCardPostion() == -1) {
                        Toast.makeText(getApplicationContext(), "You have to select a card", Toast.LENGTH_SHORT).show();
                    } else {
                        if (deliveryAddress.getText().toString().equals("") || cityEditText.getText().toString().equals("") || postcodeEditText.getText().toString().equals("")) {
                            Toast.makeText(getApplicationContext(), "Please fill all delivery details", Toast.LENGTH_SHORT).show();
                        } else {
                            //This is necessary if you have CVC check enabled in your risk settings.
                            ResponseCard responseSelectedCard = savedCards.get(getCheckedCardPostion());

                            WorldPayResponseReusableToken worlPayResponceReusable = null;

                            if (responseSelectedCard != null) {
                                // your client key
                                String clientKey = "T_C_20a3f5ad-11f9-409c-b7b6-7171f5269354";

                                SaveCardActivity
                                        .requestCVC(
                                                DetailsActivity.this,
                                                getLayoutInflater(),
                                                responseSelectedCard.getToken(),
                                                clientKey,
                                                onhandleWorldpayReusableTokenResponse(worlPayResponceReusable));
                            }

                        }

                    }
                }

                break;
        }

    }

    /**
     * Handle here Worldpay Library response for reusable token
     *
     * @param worldPayResponceReusable
     */
    private WorldPayResponseReusableToken onhandleWorldpayReusableTokenResponse(WorldPayResponseReusableToken worldPayResponceReusable) {

        worldPayResponceReusable = new WorldPayResponseReusableToken() {

            @Override
            public void onSuccess() {


                if (savedCards == null || savedCards.size() == 0) {
                    Toast.makeText(getApplicationContext(), "You must add card first", Toast.LENGTH_SHORT).show();

                } else {
                    if (getCheckedCardPostion() == -1) {
                        Toast.makeText(getApplicationContext(), "You have to select a card", Toast.LENGTH_SHORT).show();
                    } else {
                        ResponseCard orderCard = savedCards.get(getCheckedCardPostion());
                        if (orderCard != null) {
                            //if 3DS is enabled OrderActivity is called, where the 3DS is implemented
                            if (is3DSenabled) {

                                String price = priceText.getText().toString().substring(1).replace(".", "");

                                if (orderDetails == null)
                                    orderDetails = new OrderDetails(deliveryAddress.getText().toString(), cityEditText.getText().toString(), postcodeEditText.getText().toString(), price);
                                else {
                                    orderDetails.updateOrder(deliveryAddress.getText().toString(), cityEditText.getText().toString(), postcodeEditText.getText().toString(), price);
                                }

                                Intent orderIntent = new Intent(DetailsActivity.this, OrderActivity.class);
                                orderIntent.putExtra(ORDER_DETAIL, orderDetails);
                                orderIntent.putExtra(OrderActivity.EXTRA_CARD_TOKEN, orderCard.getToken());
                                startActivityForResult(orderIntent, THREE_DS_RESULT_CODE);
                            } else {

                                //  At this point user makes an http post request at his server and passes the selected card

                            /*
                                try {
                                    HttpClient httpclient = new DefaultHttpClient();
                                    HttpPost httppost = new HttpPost(MY_SERVER_URL);

                                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                                    nameValuePairs.add(new BasicNameValuePair("responceCard", savedCards.get(getCheckedCardPostion()).));

                                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                                    httpclient.execute(httppost);
                                }catch (Exception e){

                                }
                                */

                                new AlertDialog.Builder(DetailsActivity.this)
                                        .setTitle("Confirm Purchase")
                                        .setMessage("At this point you should connect to your own server and complete the purchase from there.")
                                        .setPositiveButton("OK",
                                                new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        Intent successIntent = new Intent(getApplicationContext(), SuccessActivity.class);
                                                        startActivity(successIntent);
                                                    }

                                                }).show();

                            }
                        }

                    }
                }

            }

            @Override
            public void onResponseError(ResponseError responseError) {
                new AlertDialog.Builder(DetailsActivity.this)
                        .setTitle("Response Error")
                        .setMessage(responseError.getMessage())
                        .setNeutralButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }

                                }).show();
            }

            @Override
            public void onError(WorldPayError worldPayError) {
                new AlertDialog.Builder(DetailsActivity.this)
                        .setTitle("Worldpay Error")
                        .setMessage(worldPayError.getMessage())
                        .setNeutralButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }

                                }).show();
            }
        };

        return worldPayResponceReusable;

    }


    /**
     * Ask for the Worldpay Library to add a card
     */
    private void addCard() {
        // Start the activty to save a new card
        Intent intent = new Intent(this, SaveCardActivity.class);

        // User can customize the theme of the screen
        intent.putExtra(SaveCardActivity.EXTRA_CUSTOMIZE_THEME, Color.parseColor("#ff0000"));

        startActivityForResult(intent, SAVE_CARD_REQUEST_CODE);
    }

    /**
     * Handle here Worldpay Library response
     *
     * @param resultCode
     * @param data
     */
    private void handleWorldpayResult(int resultCode, Intent data) {
        switch (resultCode) {
            /**
             * Success response code
             */
            case SaveCardActivity.RESULT_RESPONSE_CARD:
                if (data != null) {
                    ResponseCard responseCard = (ResponseCard) data
                            .getSerializableExtra(SaveCardActivity.EXTRA_RESPONSE_CARD);

                    if (responseCard != null) {

                        // save the list of ResponseCard
                        savedCards.add(responseCard);
                        saveCards(this, savedCards);

                        resetCheckBoxes(); // first reset all the check boxes
                        createTableRow(responseCard, true); // add to the ui
                    }

                }
                break;
            /**
             * Response error from server
             */
            case SaveCardActivity.RESULT_RESPONSE_ERROR:
                if (data != null) {
                    // we are back
                    ResponseError responseCardError = (ResponseError) data
                            .getSerializableExtra(SaveCardActivity.EXTRA_RESPONSE_ERROR);
                    if (responseCardError != null) {
                        new AlertDialog.Builder(DetailsActivity.this)
                                .setTitle("Response Error")
                                .setMessage("There is an error : " + responseCardError.getMessage())
                                .setNeutralButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog, int which) {

                                            }

                                        }).show();
                    }
                }

                break;
            /**
             * Generic worldpay error
             */
            case SaveCardActivity.RESULT_WORLDPAY_ERROR:
                WorldPayError worldPayError = (WorldPayError) data
                        .getSerializableExtra(SaveCardActivity.EXTRA_RESPONSE_WORLDPAY_ERROR);
                if (worldPayError != null) {
                    new AlertDialog.Builder(DetailsActivity.this)
                            .setTitle("Worldpay Error")
                            .setMessage("There is an error : " + worldPayError.getMessage())
                            .setNeutralButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }

                                    }).show();

                }

                break;

        }
    }


    /**
     * Handle here 3DS OrderActivity result
     *
     * @param resultCode
     * @param data
     */
    private void handleThreeDsResult(int resultCode, Intent data) {
        /**
         * Success response code
         */
        switch (resultCode) {

            case RESULT_OK:
                if (data != null) {
                    String result = data.getStringExtra(THREE_DS_RESULT);
                    if (result.equals("SUCCESS")) {
                        Intent intent = new Intent(DetailsActivity.this, SuccessActivity.class);
                        startActivity(intent);

                    } else
                        new AlertDialog.Builder(DetailsActivity.this)
                                .setTitle("Order failed")
                                .setMessage("Unable to complete your purchase.\n" +
                                        "Please try again")
                                .setNeutralButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }

                                        }).show();

                }
                break;
            /**
             * Response error from server
             */
            case SaveCardActivity.RESULT_RESPONSE_ERROR:
                if (data != null) {
                    // we are back
                    ResponseError responseError = (ResponseError) data
                            .getSerializableExtra(SaveCardActivity.EXTRA_RESPONSE_ERROR);
                    if (responseError != null) {
                        new AlertDialog.Builder(DetailsActivity.this)
                                .setTitle("Response Error")
                                .setMessage("There is an error : " + ((responseError.getMessage() == null) ? responseError.getHttpStatusCode() : responseError.getMessage()))
                                .setNeutralButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog, int which) {

                                            }

                                        }).show();
                    }
                }
                break;
            /**
             * Generic worldpay error
             */
            case SaveCardActivity.RESULT_WORLDPAY_ERROR:
                WorldPayError worldPayError = (WorldPayError) data
                        .getSerializableExtra(SaveCardActivity.EXTRA_RESPONSE_WORLDPAY_ERROR);
                if (worldPayError != null) {
                    new AlertDialog.Builder(DetailsActivity.this)
                            .setTitle("Worldpay Error")
                            .setMessage("There is an error : " + worldPayError.getMessage())
                            .setNeutralButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }

                                    }).show();

                }

                break;
        }


    }

    /**
     * Deletes a card from the ui and from the savedCards ArrayList on memory.
     */
    private void deleteCard(final ResponseCard responseCard, final View v) {

        new AlertDialog.Builder(DetailsActivity.this)
                .setIcon(R.drawable.ic_dialog_alert_holo_light)
                .setTitle("Delete Card")
                .setMessage("Do you want to delete this card?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // just go back
                    }

                })
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // row is your row, the parent of the clicked button
                                View row = (View) v.getParent();
                                // container contains all the rows, you could
                                // keep a variable somewhere else to the
                                // container which you can refer to here
                                ViewGroup container = ((ViewGroup) row.getParent());
                                // delete the row and invalidate your view so it
                                // gets redrawn
                                container.removeView(row);
                                container.invalidate();

                                // scan the array list for the response card
                                savedCards.remove(responseCard);
                                // and then save the cards
                                saveCards(DetailsActivity.this, savedCards);
                            }
                        }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SAVE_CARD_REQUEST_CODE) {
            handleWorldpayResult(resultCode, data);
        } else if (requestCode == THREE_DS_RESULT_CODE) {
            handleThreeDsResult(resultCode, data);
        }


    }

    /*
     * When a new card is added any previous card which is selected will be
     * unchecked.
     */
    private void resetCheckBoxes() {
        // scan the checkboxes
        for (int i = 0, s = cardTable.getChildCount(); i < s; i++) {
            CheckBox cardCheckBox = (CheckBox) cardTable.getChildAt(i)
                    .findViewById(R.id.cardCheckBox);
            cardCheckBox.setChecked(false);
        }
    }

    private int getCheckedCardPostion() {
        for (int i = 0, s = cardTable.getChildCount(); i < s; i++) {
            CheckBox cardCheckBox = (CheckBox) cardTable.getChildAt(i)
                    .findViewById(R.id.cardCheckBox);
            if (cardCheckBox.isChecked()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * This method will inflate a tablerow
     *
     * @param responseCard The data that should contain
     * @param flagChecked  - If it will be checked after the creation
     */
    private void createTableRow(ResponseCard responseCard, boolean flagChecked) {
        String cardType = responseCard.getCardType();
        String cardNumber = responseCard.getMaskedCardNumber();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View tView = inflater.inflate(R.layout.card_tablerow, null);
        cardTable.addView(tView);

        CheckBox cardCheckBox = (CheckBox) tView
                .findViewById(R.id.cardCheckBox);
        cardCheckBox.setChecked(flagChecked);

        ImageView cardImage = (ImageView) tView.findViewById(R.id.cardImage);
        TextView numberCard = (TextView) tView.findViewById(R.id.cardNumber);
        Button deleteButton = (Button) tView.findViewById(R.id.deleteButton);

        /**
         * Choose the right icon for the card
         */
        if (cardType.equals("VISA")) {
            cardImage.setImageDrawable(getResources().getDrawable(
                    R.drawable.card_visa));
        } else if (cardType.equals("MASTERCARD")) {
            cardImage.setImageDrawable(getResources().getDrawable(
                    R.drawable.card_mastercard));
        } else if (cardType.equals("MAESTRO")) {
            cardImage.setImageDrawable(getResources().getDrawable(
                    R.drawable.card_maestro));
        } else if (cardType.equals("AMEX")) {
            cardImage.setImageDrawable(getResources().getDrawable(
                    R.drawable.card_amex));
        }

        deleteButton.setTag(responseCard);
        deleteButton.setOnClickListener(this);

        cardCheckBox.setOnClickListener(this);
        numberCard.setText(cardNumber);

    }

    /**
     * Save on internal storage, the ResponseCard list.
     */
    public void saveCards(Context context,
                          ArrayList<ResponseCard> reponseArrayList) {
        try {
            FileOutputStream fos = openFileOutput(CARDS_FILE,
                    Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(reponseArrayList);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This returns the cards that have been saved.
     */
    public ArrayList<ResponseCard> getSavedCards(Context context) {
        try {
            FileInputStream fis = openFileInput(CARDS_FILE);
            ObjectInputStream is = new ObjectInputStream(fis);
            Object readObject = is.readObject();

            is.close();
            return (ArrayList<ResponseCard>) readObject;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

}
