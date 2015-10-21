package com.worldpay;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Contains response from the server with card details.
 *
 * @author Sotiris Chatzianagnostou - sotcha@arx.net
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

    private String cardSchemeType;
    private String cardSchemeName;
    private String cardIssuer;
    private String countryCode;
    private String cardClass;
    private String prepaid;

    private Map<String, Locale> localeMap;

    /**
     * Default constructor
     */
    public ResponseCard() {
    }

    /**
     * Creates a JSON Object with the information that are being received as a response from the
     * server. <br>
     * <br>
     * Values in mainObject:
     * <ul>
     * <li><b>token</b> - the token that is created by the server</li>
     * <li><b>reusable</b> - if the token is going to be used only once</li>
     * <li><b>paymentMethodJSONObject</b> - a JSON Object</li>
     * </ul>
     * <p>
     * Values in paymentMethodJSONObject:
     * <ul>
     * <li><b>type</b> - Card</li>
     * <li><b>name</b> - card holder name</li>
     * <li><b>expiryMonth</b> - the month the card expires</li>
     * <li><b>expiryYear</b> - the year the card expires</li>
     * <li><b>cardType</b> - type of the card</li>
     * <li><b>maskedCardNumber</b> - CVC number</li>
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


        cardSchemeType = paymentMethodJSONObject.optString("cardSchemeType");
        cardSchemeName = paymentMethodJSONObject.optString("cardSchemeName");
        cardIssuer = paymentMethodJSONObject.optString("cardIssuer");
        countryCode = paymentMethodJSONObject.optString("countryCode");
        if (!countryCode.equals("XXX")) {
            initCountryCodeMapping();
            countryCode = iso3CountryCodeToIso2CountryCode(countryCode);
        }
        cardClass = paymentMethodJSONObject.optString("cardClass");
        prepaid = paymentMethodJSONObject.optString("prepaid");


    }

    public String getToken() {
        return token;
    }

    public ResponseCard setToken(String token) {
        this.token = token;
        return this;
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

    public void setMaskedCardNumber(String maskedCardNumber) {
        this.maskedCardNumber = maskedCardNumber;
    }

    public String getCardSchemeType() {
        return cardSchemeType;
    }

    public void setCardSchemeType(String cardSchemeType) {
        this.cardSchemeType = cardSchemeType;
    }

    public String getCardSchemeName() {
        return cardSchemeName;
    }

    public void setCardSchemeName(String cardSchemeName) {
        this.cardSchemeName = cardSchemeName;
    }

    public String getCardIssuer() {
        return cardIssuer;
    }

    public void setCardIssuer(String cardIssuer) {
        this.cardIssuer = cardIssuer;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCardClass() {
        return cardClass;
    }

    public void setCardClass(String cardClass) {
        this.cardClass = cardClass;
    }

    public String getPrepaid() {
        return prepaid;
    }

    public void setPrepaid(String prepaid) {
        this.prepaid = prepaid;
    }

    /**
     * Shows some debugging info
     */
    @Override
    public String toString() {
        return "Token=" + token + ", name=" + name + " cardNum=" + maskedCardNumber + name + " cardNum=" + maskedCardNumber
                + " cardSchemeType=" + cardSchemeType
                + " cardSchemeName=" + cardSchemeName
                + " cardIssuer=" + cardIssuer
                + " countryCode=" + countryCode
                + " cardClass=" + cardClass
                + " prepaid=" + prepaid
                ;
    }


    private void initCountryCodeMapping() {
        String[] countries = Locale.getISOCountries();
        localeMap = new HashMap<String, Locale>(countries.length);
        for (String country : countries) {
            Locale locale = new Locale("", country);
            localeMap.put(locale.getISO3Country().toUpperCase(), locale);
        }
    }

    private String iso3CountryCodeToIso2CountryCode(String iso3CountryCode) {
        return localeMap.get(iso3CountryCode).getCountry();
    }

}
