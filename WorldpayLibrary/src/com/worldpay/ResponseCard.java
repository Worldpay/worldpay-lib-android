package com.worldpay;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Contains response from the server with card details.
 * 
 */

public class ResponseCard implements Serializable {
	private static final long serialVersionUID = -8273784902723750008L;

	private String token;
	private boolean reusable;

	private String type;
	private String name;
	private String expiryMonth;
	private String expiryYear;
	private String cardType;
	private String maskedCardNumber;

	/**
	 * Default constructor
	 */
	public ResponseCard() {
	}

	/**
	 * Creates a JSON Object with the information received as a response from the
	 * server. <br>
	 * <br>
	 * Values in mainObject:
	 * <ul>
	 * <li><b>token</b> - the token that is created by the server</li>
	 * <li><b>reusable</b> - if the token is reusable </li>
	 * <li><b>paymentMethodJSONObject</b> - a JSON Object</li>
	 * </ul>
	 * <p>
	 * Values in paymentMethodJSONObject:
	 * <ul>
	 * <li><b>type</b> - Card</li>
	 * <li><b>name</b> - Card holder name</li>
	 * <li><b>expiryMonth</b> - The month the card expires</li>
	 * <li><b>expiryYear</b> - The year the card expires</li>
	 * <li><b>cardType</b> - Type of the card</li>
	 * <li><b>maskedCardNumber</b> - Masked card number</li>
	 * </ul>
	 * </p>
	 * 
	 * @param jsonString
	 * @throws JSONException
	 */

	protected void parseJsonString(String jsonString) throws JSONException {
		JSONObject mainObject = new JSONObject(jsonString);
		token = mainObject.optString("token");
		reusable = mainObject.optBoolean("reusable");
		JSONObject paymentMethodJSONObject = mainObject.optJSONObject("paymentMethod");
		type = paymentMethodJSONObject.optString("type");
		name = paymentMethodJSONObject.optString("name");
		expiryMonth = paymentMethodJSONObject.optString("expiryMonth");
		expiryYear = paymentMethodJSONObject.optString("expiryYear");
		cardType = paymentMethodJSONObject.optString("cardType");
		maskedCardNumber = paymentMethodJSONObject.optString("maskedCardNumber");
	}

	public String getToken() {
		return token;
	}

	public boolean isReusable() {
		return reusable;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getExpiryMonth() {
		return expiryMonth;
	}

	public String getExpiryYear() {
		return expiryYear;
	}

	public String getCardType() {
		return cardType;
	}

	public String getMaskedCardNumber() {
		return maskedCardNumber;
	}

	public ResponseCard setToken(String token) {
		this.token = token;
		return this;
	}

	/**
	 * Shows some debugging info
	 */
	@Override
	public String toString() {
		return "Token=" + token + ", name=" + name + " cardNum=" + maskedCardNumber;
	}

}
