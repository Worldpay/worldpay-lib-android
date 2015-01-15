Worldpay Android Library
=============

##Integration

1. Before integrating Worldpay library make sure you have installed the Android SDK with API Level 16.
2. Import Worldpay library to your workspace.

##Initialise the library

1. Link the **WorldpayLibrary Project** to your project.
2. Declare internet permission in AndroidManifest.xml

        <uses-permission android:name="android.permission.INTERNET" />

3. Initialise the library

    	Worldpay worldpay = Worldpay.getInstance();
		worldpay.setClientKey(YOUR_CLIENT_KEY);
		// decide whether you want to charge this card multiple times or only once
		worldpay.setReusable(true);
		// set validation type advanced or basic
		Card.setValidationType(Card.VALIDATION_TYPE_ADVANCED);
		

##Choose library-provided or create your custom card submission form



###Default Library-provided form

Developers can use **SaveCardActivity** of the library.


1. Declare the activity in AndroidManifest.xml

        <activity android:name="com.worldpay.SaveCardActivity"   android:label="Card Details" />

2. Open the activity :

         	Intent intent = new Intent(myactivity, SaveCardActivity.class);
         	//SAVE_CARD_REQUEST_CODE a custom request code for that activity
         	startActivityForResult(intent, SAVE_CARD_REQUEST_CODE);
 
3. Handle activity response implementing **Activity.onActivityResult(int, int, android.content.Intent)**.
The developer can use Intent parameter from **Activity.onActivityResult(int, int, android.content.Intent)** to retrieve more information about the result.

###Custom form

1. Create a Card object, pass card details and validate it.

		Card card = new Card();
		CardValidationError validate = card.setHolderName("John Newman").
			setCardNumber("1234123412341234")
			.setCvc("123").setExpriryMonth("12").setExpiryYear("2018")
			.validate();
			
2. Use the Worldpay library to connect with Worldpay to store card details and create a token. This can be done by creating an AsyncTask and executing it. A callback interface should be implemented to handle the response.

		AsyncTask<Void, Void, HttpServerResponse> createTokenAsyncTask = worldpay.createTokenAsyncTask(this, card, new WorldpayResponse() {

			@Override
			public void onSuccess(ResponseCard responseCard) {
				//handle success
			}

			@Override
			public void onResponseError(ResponseError responseError) {
				//handle error
			}

			@Override
			public void onError(WorldpayError worldpayError) {
				//handle error
			}

		});

		if (createTokenAsyncTask != null) {
			createTokenAsyncTask.execute();
		}


