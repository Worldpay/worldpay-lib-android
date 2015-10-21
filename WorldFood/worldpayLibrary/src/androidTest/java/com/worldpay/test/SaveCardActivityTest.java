package com.worldpay.test;

import android.test.ActivityInstrumentationTestCase2;

import com.worldpay.Card;
import com.worldpay.CardValidationError;
import com.worldpay.ResponseCard;
import com.worldpay.ResponseError;
import com.worldpay.ReusableToken;
import com.worldpay.SaveCardActivity;
import com.worldpay.WorldPay;
import com.worldpay.WorldPayError;
import com.worldpay.WorldPayResponse;
import com.worldpay.WorldPayResponseReusableToken;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


public class SaveCardActivityTest extends
        ActivityInstrumentationTestCase2<SaveCardActivity> {

    private static final String CARD_HOLDER_NAME = "John Newman";

    private static final String CARD_NUMBER_VISA = "4444333322221111";

    private static final String EXPIRY_MONTH = "12";

    private static final String EXPIRY_YEAR = "2015";


    private static final String CVC_NUMBER = "123";

    private static final String CLIENT_KEY = "T_C_27f88c79-9d94-4318-854b-1cdb1166f201";

    private SaveCardActivity saveCardActivity;

    private String token;

    private WorldPay worldpay;

    public SaveCardActivityTest() {
        super(SaveCardActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(true);

        saveCardActivity = getActivity();


        worldpay = WorldPay.getInstance();

        worldpay.setClientKey(CLIENT_KEY);
        worldpay.setReusable(true);

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testExpiryDate() {
        Card card = new Card();

		/*
         * Invalid data
		 */
        CardValidationError validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber(CARD_NUMBER_VISA).setCvc(CVC_NUMBER)
                .setExpiryYear("").setExpriryMonth("").validate();

        assertNotNull(validate);

        assertTrue("Expiry date error code should be 2 ",
                validate.hasError(CardValidationError.ERROR_CARD_EXPIRY));

		/*
         * Invalid data
		 */
        validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber(CARD_NUMBER_VISA).setCvc(CVC_NUMBER)
                .setExpiryYear(null).setExpriryMonth(null).validate();

        assertNotNull(validate);

        assertTrue("Expiry date error code should be 2 ",
                validate.hasError(CardValidationError.ERROR_CARD_EXPIRY));

		/*
         * Invalid data
		 */
        validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber(CARD_NUMBER_VISA).setCvc(CVC_NUMBER)
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth("15").validate();

        assertNotNull(validate);

        assertTrue("Expiry date error code should be 2 ",
                validate.hasError(CardValidationError.ERROR_CARD_EXPIRY));

		/*
		 * Invalid data
		 */
        validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber(CARD_NUMBER_VISA).setCvc(CVC_NUMBER)
                .setExpiryYear("-123").setExpriryMonth(EXPIRY_MONTH).validate();

        assertNotNull(validate);

        assertTrue("Expiry date error code should be 2 ",
                validate.hasError(CardValidationError.ERROR_CARD_EXPIRY));

		/*
		 * Valid data
		 */
        validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber(CARD_NUMBER_VISA).setCvc(CVC_NUMBER)
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH)
                .validate();

        assertNull(validate);

    }

    public void testExpiryDateSub() {
        Card card = new Card();

        try {
            /*
             * Invalid data
			 */
            card.setExpiryYear("").setExpriryMonth("");

            Class<?> c = card.getClass();

            Method methodToTest = c.getDeclaredMethod("validateExpiry");

            methodToTest.setAccessible(true);

            assertEquals(false, methodToTest.invoke(card));

			/*
			 * Invalid data
			 */
            card.setExpiryYear("-123").setExpriryMonth("10");

            assertEquals(false, methodToTest.invoke(card));

			/*
			 * Invalid data
			 */
            card.setExpiryYear("0").setExpriryMonth("10");

            assertEquals(false, methodToTest.invoke(card));

			/*
			 * Valid data
			 */
            card.setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH);

            assertEquals(true, methodToTest.invoke(card));

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    public void testHolderName() {
        Card card = new Card();

		/*
		 * Invalid data
		 */
        CardValidationError validate = card.setHolderName("")
                .setCardNumber(CARD_NUMBER_VISA).setCvc(CVC_NUMBER)
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH)
                .validate();

        assertNotNull(validate);

        assertTrue("Card holder name error code should be 8 ",
                validate.hasError(CardValidationError.ERROR_HOLDER_NAME));

		/*
		 * Invalid data
		 */
        validate = card.setHolderName("John Ne#m@n!1")
                .setCardNumber(CARD_NUMBER_VISA).setCvc(CVC_NUMBER)
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH)
                .validate();

        assertNotNull(validate);

        assertTrue("Card holder name error code should be 8 ",
                validate.hasError(CardValidationError.ERROR_HOLDER_NAME));

		/*
		 * Valid data
		 */
        validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber(CARD_NUMBER_VISA).setCvc(CVC_NUMBER)
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH)
                .validate();

        assertNull(validate);
    }

    public void testHolderNameSub() {
        Card card = new Card();

        try {
            /*
             * Invalid data
			 */
            card.setHolderName("");

            Class<?> c = card.getClass();

            Method methodToTest = c.getDeclaredMethod("validateCardHolderName");
            methodToTest.setAccessible(true);

            assertEquals(false, methodToTest.invoke(card));

			/*
			 * Invalid data
			 */
            card.setHolderName("!!*@^HBJDKS&*^");

            assertEquals(false, methodToTest.invoke(card));

			/*
			 * Valid data
			 */
            card.setHolderName(CARD_HOLDER_NAME);

            assertEquals(true, methodToTest.invoke(card));

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void testCardNumber() {
        Card card = new Card();

		/*
		 * Invalid data
		 */
        CardValidationError validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber("").setCvc(CVC_NUMBER)
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH)
                .validate();

        assertNotNull(validate);

        assertTrue("Card number error code should be 16 ",
                validate.hasError(CardValidationError.ERROR_CARD_NUMBER));

		/*
		 * Invalid data
		 */
        validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber("111122223333444-").setCvc(CVC_NUMBER)
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH)
                .validate();

        assertNotNull(validate);

        assertTrue("Card holder name error code should be 8 ",
                validate.hasError(CardValidationError.ERROR_CARD_NUMBER));

		/*
		 * Valid data
		 */
        validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber(CARD_NUMBER_VISA).setCvc(CVC_NUMBER)
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH)
                .validate();

        assertNull(validate);

		/*
		 * Valid data
		 */
        validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber(CARD_NUMBER_VISA).setCvc(null)
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH)
                .validate();

        assertNull(validate);
    }

    public void testCardNumberSub() {
        Card card = new Card();

        try {
            /*
             * Invalid data
			 */
            card.setCardNumber("");

            Class<?> c = card.getClass();

            Method methodToTest = c
                    .getDeclaredMethod("validateCardNumberBasic");
            Method methodToTest2 = c
                    .getDeclaredMethod("validateCardNumberAdvanced");

            methodToTest.setAccessible(true);
            methodToTest2.setAccessible(true);

            assertEquals(false, methodToTest.invoke(card));
            assertEquals(false, methodToTest2.invoke(card));

			/*
			 * Invalid data
			 */
            card.setCardNumber("!!*@^HBJDKS&*^");

            assertEquals(false, methodToTest.invoke(card));
            assertEquals(false, methodToTest2.invoke(card));

			/*
			 * Valid data
			 */
            card.setCardNumber(CARD_NUMBER_VISA);

            assertEquals(true, methodToTest.invoke(card));
            assertEquals(true, methodToTest2.invoke(card));

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void testCVC() {
        Card card = new Card();

		/*
		 * Invalid data
		 */
        CardValidationError validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber(CARD_NUMBER_VISA).setCvc("!1#@")
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH)
                .validate();

        assertNotNull(validate);

        assertTrue("CVC error code should be 4 ",
                validate.hasError(CardValidationError.ERROR_CVC));

		/*
		 * Valid data
		 */
        validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber(CARD_NUMBER_VISA).setCvc("")
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH)
                .validate();

        assertNull(validate);

		/*
		 * Valid data
		 */
        validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber(CARD_NUMBER_VISA).setCvc(null)
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH)
                .validate();

        assertNull(validate);

		/*
		 * Valid data
		 */
        validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber(CARD_NUMBER_VISA).setCvc("123")
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH)
                .validate();

        assertNull(validate);

		/*
		 * Valid data
		 */
        validate = card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber(CARD_NUMBER_VISA).setCvc("1234")
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH)
                .validate();

        assertNull(validate);

    }

    public void testCVCSub() {
        /*
         * Invalid data
		 */
        assertEquals(false, Card.validateCVC("hello"));
        assertEquals(false, Card.validateCVC("!&%"));

		
		/*
		 * Valid data
		 */
        assertEquals(true, Card.validateCVC("123"));
        assertEquals(true, Card.validateCVC(""));
        assertEquals(true, Card.validateCVC(null));

    }

    public void testCreateToken() throws Exception {
        final Card card = new Card();

        final CountDownLatch signal = new CountDownLatch(1);

		/*
		 * Failure
		 */
        card.setHolderName(CARD_HOLDER_NAME)
                .setCardNumber("!(@^&!(@%!^@( (*&#@^@&*(").setCvc(CVC_NUMBER)
                .setExpiryYear(EXPIRY_YEAR).setExpriryMonth(EXPIRY_MONTH);

        final WorldPayResponse worldPayResponseCallback = new WorldPayResponse() {

            @Override
            public void onSuccess(ResponseCard responseCard) {
            }

            @Override
            public void onResponseError(ResponseError responseError) {
                assertNotNull(responseError);
                assertEquals("Card number must have digits only",
                        responseError.getMessage());

                signal.countDown();
            }

            @Override
            public void onError(WorldPayError worldpayError) {
            }
        };

        // Execute the async task on the UI thread to create token
        getInstrumentation().runOnMainSync(new Runnable() {

            @Override
            public void run() {
                worldpay.createTokenAsyncTask(
                        getActivity().getApplicationContext(), card,
                        worldPayResponseCallback).execute();
            }
        });

		/*
		 * The testing thread will wait here until the UI thread releases it
		 * above with the countDown() or 10 seconds passes and it times out.
		 */
        signal.await(10, TimeUnit.SECONDS);

		/*
		 * Success
		 */
        card.setHolderName(CARD_HOLDER_NAME).setCardNumber(CARD_NUMBER_VISA)
                .setCvc(CVC_NUMBER).setExpiryYear(EXPIRY_YEAR)
                .setExpriryMonth(EXPIRY_MONTH);

        final WorldPayResponse worldpayResponseCallbackSucces = new WorldPayResponse() {

            @Override
            public void onSuccess(ResponseCard responseCard) {
                assertNotNull(responseCard);

                signal.countDown();
            }

            @Override
            public void onResponseError(ResponseError responseError) {

            }

            @Override
            public void onError(WorldPayError worldpayError) {

            }
        };

        // Execute the async task on the UI thread to create token
        getInstrumentation().runOnMainSync(new Runnable() {

            @Override
            public void run() {
                worldpay.createTokenAsyncTask(saveCardActivity, card,
                        worldpayResponseCallbackSucces).execute();
            }
        });

		/*
		 * The testing thread will wait here until the UI thread releases it
		 * above with the countDown() or 10 seconds passes and it times out.
		 */
        signal.await(10, TimeUnit.SECONDS);
    }

    public void testReuseToken() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        final ReusableToken resusableToken = new ReusableToken();

		/*
		 * Failure
		 */
        resusableToken.setClientKey(CLIENT_KEY).setCvc(CVC_NUMBER)
                .setToken("-123");

        final WorldPayResponseReusableToken worldpayResponseCallback = new WorldPayResponseReusableToken() {

            @Override
            public void onSuccess() {
            }

            @Override
            public void onResponseError(ResponseError responseError) {
                assertNotNull(responseError);

                assertEquals("Token -123 does not exist", responseError.getMessage());
                signal.countDown();
            }

            @Override
            public void onError(WorldPayError worldpayError) {
            }
        };

        // Execute the async task on the UI thread to check the reusable token
        getInstrumentation().runOnMainSync(new Runnable() {

            @Override
            public void run() {
                worldpay.reuseTokenAsyncTask(saveCardActivity, resusableToken,
                        worldpayResponseCallback).execute();
            }
        });

		/*
		 * The testing thread will wait here until the UI thread releases it
		 * above with the countDown() or 10 seconds passes and it times out.
		 */
        signal.await(10, TimeUnit.SECONDS);

		/*
		 * Success
		 */
        resusableToken.setClientKey(CLIENT_KEY).setCvc(CVC_NUMBER)
                .setToken(token);

        final WorldPayResponseReusableToken worldpayResponseCallbackSucces = new WorldPayResponseReusableToken() {

            @Override
            public void onSuccess() {
                assertTrue(true);
                signal.countDown();
            }

            @Override
            public void onResponseError(ResponseError responseError) {
            }

            @Override
            public void onError(WorldPayError worldpayError) {
            }
        };

        // Execute the async task on the UI thread to check the reusable token
        getInstrumentation().runOnMainSync(new Runnable() {

            @Override
            public void run() {
                worldpay.reuseTokenAsyncTask(saveCardActivity, resusableToken,
                        worldpayResponseCallbackSucces).execute();
            }
        });

		/*
		 * The testing thread will wait here until the UI thread releases it
		 * above with the countDown() or 10 seconds passes and it times out.
		 */
        signal.await(10, TimeUnit.SECONDS);

    }

}
