package com.worldpay;

import java.io.Serializable;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents details of a card. Developer has to create this object and pass it to
 * {@link Worldpay#createTokenAsyncTask(android.content.Context, Card, WorldpayResponse)} in order
 * to create a token for this card.
 * 
 * <p>
 * A {@link #validate()} method is also available in order to validate the cards data.
 * </p>
 * 
 * @see CardValidationError
 */
public class Card implements Serializable {

	private static final long serialVersionUID = 2614432468043116204L;

	private static final String PATTERN_ADVANCED_CARD_NUMBER = "[^0-9-\\s]";
	private static final String PATTERN_BASIC_CARD_NUMBER = "[^0-9]+";
	private static final String PATTERN_CVC_NUMBER = "[^0-9]+";
	private static final String PATTERN_CARD_HOLDER_NAME = "^[a-zA-Z()]+$";

	/**
	 * Validation type Basic
	 */
	public static final int VALIDATION_TYPE_BASIC = 100;
	/**
	 * Validation type Advanced, default value.
	 */
	public static final int VALIDATION_TYPE_ADVANCED = 200;

	//the validation type, it is static so it is the same for all cards
	private static int validationType = VALIDATION_TYPE_ADVANCED;

	private String holderName;
	private String expriryMonth;
	private String expiryYear;
	private String cardNumber;

	private String cvc;

	public Card(String name, String expriryMonth, String expiryYear, String cardNumber, String cvc) {
		this.holderName = name;
		this.expriryMonth = expriryMonth;
		this.expiryYear = expiryYear;
		this.cardNumber = cardNumber;
		this.cvc = cvc;
	}

	public Card() {
	}

	public Card setHolderName(String holderName) {
		this.holderName = holderName;
		return this;
	}

	public Card setExpriryMonth(String expriryMonth) {
		this.expriryMonth = expriryMonth;
		return this;
	}

	public Card setExpiryYear(String expiryYear) {
		this.expiryYear = expiryYear;
		return this;
	}

	public Card setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber.replace(" ", "").replace("-", "");
		return this;
	}

	public Card setCvc(String cvc) {
		this.cvc = cvc;
		return this;
	}

	/**
	 * Returns the data of the card as a {@link JSONObject}
	 * 
	 * @return A {@link JSONObject}
	 * @throws JSONException
	 * @see {@link JSONObject}
	 */
	protected JSONObject getAsJSONObject() throws JSONException {
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("type", "Card");
		jsonObject.put("name", holderName);
		jsonObject.put("expiryMonth", expriryMonth);
		jsonObject.put("expiryYear", expiryYear);
		jsonObject.put("cardNumber", cardNumber);
		jsonObject.put("cvc", cvc);

		return jsonObject;
	}

	/**
	 * Validate the card details.<br>
	 * 
	 * @return {@link CardValidationError} If there is one or more validation errors, otherwise
	 *         returns null.
	 *         <p>
	 *         Contains a list of errors with possible values : <br>
	 *         <ul>
	 *         <li>{@link CardValidationError#ERROR_CARD_EXPIRY}</li>
	 *         <li>{@link CardValidationError#ERROR_CVC}</li>
	 *         <li>{@link CardValidationError#ERROR_HOLDER_NAME}</li>
	 *         <li>{@link CardValidationError#ERROR_CARD_NUMBER}</li>
	 *         </ul>
	 * @see CardValidationError
	 */
	public CardValidationError validate() {
		CardValidationError validationError = new CardValidationError();
		//card holdername 
		if (!validateCardHolderName()) {
			validationError.addError(CardValidationError.ERROR_HOLDER_NAME);
		}
		//cvc
		if (!validateCVC(cvc)) {
			validationError.addError(CardValidationError.ERROR_CVC);
		}
		//expiry
		if (!validateExpiry()) {
			validationError.addError(CardValidationError.ERROR_CARD_EXPIRY);
		}

		//card number
		if (!validateCardNumber()) {
			validationError.addError(CardValidationError.ERROR_CARD_NUMBER);
		}

		return validationError.hasErrors() ? validationError : null;
	}

	/**
	 * Set the validation type for the cards. <br>
	 * Possible values :
	 * <ul>
	 * <li>{@link #VALIDATION_TYPE_BASIC}</li>
	 * <li>{@link #VALIDATION_TYPE_ADVANCED}</li>
	 * </ul>
	 * The default value is {@link #VALIDATION_TYPE_ADVANCED}.
	 * 
	 * @param validationType
	 * 
	 * @throws IllegalArgumentException
	 */

	public static void setValidationType(int validationType) throws IllegalArgumentException {
		if (validationType != VALIDATION_TYPE_ADVANCED && validationType != VALIDATION_TYPE_BASIC) {
			throw new IllegalArgumentException("Validation type" + validationType + " do not exist.");
		}
		Card.validationType = validationType;
	}

	/**
	 * Returns true if the validation of the card holder name of the card is correct
	 * 
	 * @return true if the the card holder name of the card is correct
	 */

	private boolean validateCardHolderName() {
		holderName = holderName.replace(" ", "");
		// Create a Pattern object
		Pattern r = Pattern.compile(PATTERN_CARD_HOLDER_NAME);

		// Now create matcher object.
		Matcher m = r.matcher(holderName);
		return m.find();
	}

	/**
	 * Returns true if the validation of the CVC of the card is correct
	 * 
	 * @return true if the the CVC of the card is correct
	 */

	public static boolean validateCVC(String cvc) {
		
		if (cvc == null || cvc == "") {
			return true;
		}
		
		// Create a Pattern object
		Pattern r = Pattern.compile(PATTERN_CVC_NUMBER);

		// Now create matcher object.
		Matcher m = r.matcher(cvc);
		
		return !m.find();
	}

	private boolean validateCardNumber() {
		if (validationType == VALIDATION_TYPE_ADVANCED) {
			return validateCardNumberAdvanced();
		} else {
			//then is basic
			return validateCardNumberBasic();
		}
	}

	/**
	 * Returns true if the validation of the number of the card is correct Advanced Validation
	 * 
	 * @return true if the the number of the card is correct
	 */

	private boolean validateCardNumberAdvanced() {

		if (cardNumber == null || cardNumber.equals("")) {
			return false;
		} else {
			// Create a Pattern object
			Pattern r = Pattern.compile(PATTERN_ADVANCED_CARD_NUMBER);

			// Now create matcher object.
			Matcher m = r.matcher(cardNumber);

			boolean check = false;
			int c = 0, d = 0;

			String newCardNumber = cardNumber.replace(" ", "");
			cardNumber = cardNumber.replace("/\\D/g", "");

			if (m.find()) {
				return false;
			}
			if (newCardNumber.length() < 12 || newCardNumber.length() > 19) {
				return false;
			}

			for (int f = cardNumber.length() - 1; f >= 0; f--) {
				char g = cardNumber.charAt(f);

				int x;

				if (Character.isDigit(g)) {
					x = Character.getNumericValue(g);
					d = x;

					if (check == true) {
						d = d * 2;
						if (d > 9) {
							d = d - 9;
							c = c + d;
						} else {
							c = c + d;
						}
					} else {
						c = c + d;
					}

					check = !check;
				} else {
				}

			}

			int sum = -1;

			if (c > 0) {
				sum = c % 10;
			}

			if (sum == 0) {
				return true;
			} else {
				return false;
			}

		}

	}

	/**
	 * Returns true if the validation of the number of the card is correct Basic Validation
	 * 
	 * @param cardNumber
	 *            the number of the card that is going to be checked
	 * @return true if the the number of the card is correct
	 */

	private boolean validateCardNumberBasic() {

		if (cardNumber == null || cardNumber.length() == 0) {
			return false;
		} else {
			// Create a Pattern object
			Pattern r = Pattern.compile(PATTERN_BASIC_CARD_NUMBER);

			// Now create matcher object.
			Matcher m = r.matcher(cardNumber);

			return !m.find();

		}

	}

	/**
	 * Returns true if the validation of the month and year of the card is correct Expiry Validation
	 * 
	 * @param month
	 *            the month the card expires
	 * @param year
	 *            the year the card expires
	 * @return true if the the month and year are correct
	 */

	private boolean validateExpiry() {

		if ((expriryMonth == null || expriryMonth.equals("")) || (expiryYear == null || expiryYear.equals(""))) {
			return false;
		} else if (expriryMonth.length() > 2 || expiryYear.length() != 4) {
			return false;
		} else {

			//TODO check if that is right - changes the original value  

			Calendar c = Calendar.getInstance();
			int currentYr = c.get(Calendar.YEAR);
			int currentMth = c.get(Calendar.MONTH);

			int expYear = Integer.parseInt(expiryYear);
			int expMonth = Integer.parseInt(expriryMonth);

			if ((Double.isNaN(currentYr)) || (Double.isNaN(currentMth))) {

				return false;

			} else if (expMonth > 12 || 1 > expMonth) {

				return false;
			}

			if (expYear > currentYr) {
				return true;
			} else if (expYear == currentYr && expMonth >= currentMth) {
				return true;
			} else {
				return false;
			}

		}
	}

}
