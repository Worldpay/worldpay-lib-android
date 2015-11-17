package com.worldpay;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import static com.worldpay.AlternativePaymentMethodValidationError.ERROR_APM_NAME;
import static com.worldpay.AlternativePaymentMethodValidationError.ERROR_NAME;
import static com.worldpay.AlternativePaymentMethodValidationError.ERROR_SHOPPER_COUNTRY_CODE;

/**
 * Alternative Payment Method.
 */
public class AlternativePaymentMethod implements Serializable {

    private static final String COUNTRY_CODE_PATTERN = "^[A-Z]{2}$";

    private static final String PAYPAL_APM_NAME = "paypal";

    private final String name;
    private final String apmName;
    private final String shopperCountryCode;

    private AlternativePaymentMethod(final String name, final String apmName,
                                     final String shopperCountryCode) {
        this.name = name;
        this.apmName = apmName;
        this.shopperCountryCode = shopperCountryCode;
    }

    /**
     * Creates a new {@link AlternativePaymentMethod} for PayPal for the specified shopper and their
     * associated country code.
     *
     * @param name               Shopper's name.
     * @param shopperCountryCode Shopper's ISO 3166-1 alpha-2 country code.
     * @return New {@link AlternativePaymentMethod}
     */
    public static AlternativePaymentMethod newPayPalApm(final String name,
                                                        final String shopperCountryCode) {
        return new AlternativePaymentMethod(name, PAYPAL_APM_NAME, shopperCountryCode);
    }

    /**
     * Create a new {@link AlternativePaymentMethod} for the specified APM name, person and shopper
     * country code.
     *
     * @param name               Shopper name.
     * @param apmName            APM name.
     * @param shopperCountryCode Shoppers ISO 3166-1 alpha-2 country code.
     * @return New {@link AlternativePaymentMethod}
     */
    static AlternativePaymentMethod newApm(final String name, final String apmName,
                                                     final String shopperCountryCode) {
        return new AlternativePaymentMethod(name, apmName, shopperCountryCode);
    }

    /**
     * Returns a {@link JSONObject} representation of {@code this} object.
     * <p>
     * See the example below:
     * <pre>
     *     {
     *         "name": "First Last",
     *         "apmName": "paypal",
     *         "shopperCountryCode": "GB"
     *     }
     * </pre>
     * </p>
     *
     * @return A {@link JSONObject}
     * @throws JSONException
     * @see {@link JSONObject}
     */
    JSONObject getAsJSONObject() throws JSONException {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "APM");
        jsonObject.put("name", name);
        jsonObject.put("apmName", apmName);
        jsonObject.put("shopperCountryCode", shopperCountryCode);
        return jsonObject;
    }

    /**
     * Validate the APM details.<br>
     *
     * @return {@link AlternativePaymentMethodValidationError} If there are one or more validation
     * errors, otherwise returns null.
     * <p/>
     * Contains a list of errors with possible values : <br>
     * <ul>
     * <li>{@link AlternativePaymentMethodValidationError#ERROR_NAME}</li>
     * <li>{@link AlternativePaymentMethodValidationError#ERROR_APM_NAME}</li>
     * <li>{@link AlternativePaymentMethodValidationError#ERROR_SHOPPER_COUNTRY_CODE}</li>
     * </ul>
     * @see AlternativePaymentMethodValidationError
     */
    public AlternativePaymentMethodValidationError validate() {
        final AlternativePaymentMethodValidationError validationError = new AlternativePaymentMethodValidationError();
        if (isInvalidName()) {
            validationError.addError(ERROR_NAME);
        }
        if (isInvalidApmName()) {
            validationError.addError(ERROR_APM_NAME);
        }
        if (isInvalidShopperCountryCode()) {
            validationError.addError(ERROR_SHOPPER_COUNTRY_CODE);
        }
        return validationError;
    }

    private boolean isInvalidName() {
        return name == null || name.trim().isEmpty();
    }

    private boolean isInvalidApmName() {
        return apmName == null || apmName.trim().isEmpty();
    }

    private boolean isInvalidShopperCountryCode() {
        return !shopperCountryCode.matches(COUNTRY_CODE_PATTERN);
    }

    public String getName() {
        return name;
    }

    public String getApmName() {
        return apmName;
    }

    public String getShopperCountryCode() {
        return shopperCountryCode;
    }

    @Override
    public String toString() {
        return "AlternativePaymentMethod{" +
                "name='" + name + '\'' +
                ", apmName='" + apmName + '\'' +
                ", shopperCountryCode='" + shopperCountryCode + '\'' +
                '}';
    }
}
