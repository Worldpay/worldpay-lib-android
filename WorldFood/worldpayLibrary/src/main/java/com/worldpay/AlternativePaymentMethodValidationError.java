package com.worldpay;

import java.util.ArrayList;

/**
 * Represents a list of APM validation errors , using error codes. <br>
 * <p>
 * <h4>Usage :</h4>
 * <p>
 * <ul>
 * <li>To check if contains any error : {@link AlternativePaymentMethodValidationError#hasErrors()}<br>
 * </li>
 * <li>To check if there is a specific error : {@link AlternativePaymentMethodValidationError#hasError(int)}</li>
 * <li>To get a description for an error : {@link AlternativePaymentMethodValidationError#getDescription(int)}</li>
 * </ul>
 * <p>
 * {@link AlternativePaymentMethod#validate()} returns this class in order to specify validation errors. An example
 * could be : <br>
 * <p>
 * <pre>
 *
 * AlternativePaymentMethodValidationError validationError = apm.validate();
 *
 * if (validationError != null) {
 * 	//we got errors
 * 	if (validationError.hasError(AlternativePaymentMethodValidationError.ERROR_APM_NAME)) {
 * 		//something is wrong with APM name
 *    }
 * 	if (validationError.hasError(AlternativePaymentMethodValidationError.ERROR_SHOPPER_COUNTRY_CODE)) {
 * 		//something is wrong with shopper country code
 *    }
 * 	// etc ...
 *
 * }
 * </pre>
 * <p>
 * </p>
 * <h4>Possible error values</h4>
 * <ul>
 * <li>{@link AlternativePaymentMethodValidationError#ERROR_NAME}</li>
 * <li>{@link AlternativePaymentMethodValidationError#ERROR_APM_NAME}</li>
 * <li>{@link AlternativePaymentMethodValidationError#ERROR_SHOPPER_COUNTRY_CODE}</li>
 * </ul>
 */
public class AlternativePaymentMethodValidationError {

    public final static int ERROR_NAME = 2 << 1;
    public final static int ERROR_APM_NAME = 2 << 2;
    public final static int ERROR_SHOPPER_COUNTRY_CODE = 2 << 3;

    //holds the error mask
    private int error = 0;

    public AlternativePaymentMethodValidationError() {

    }

    /**
     * Returns a description for the specified code.
     *
     * @param code
     * @return Returns null if the code is not valid.
     */
    public static int getDescription(int code) {
        switch (code) {
            case ERROR_NAME:
                return R.string.nameError;
            case ERROR_APM_NAME:
                return R.string.apmNameError;
            case ERROR_SHOPPER_COUNTRY_CODE:
                return R.string.apmShopperCountryError;
        }
        return -1;
    }

    public void addError(int errorToAdd) {
        error |= errorToAdd;
    }

    public boolean hasErrors() {
        return error != 0;
    }

    public void clearErrors() {
        error = 0;
    }

    public boolean hasError(int errorNum) {
        return (error & errorNum) == errorNum;
    }

    /**
     * This method exists for debugging purposes.
     *
     * @return
     */
    public ArrayList<Integer> getAllErrors() {
        ArrayList<Integer> errors = new ArrayList<>();
        if (error != 0) {
            if (hasError(ERROR_APM_NAME)) {
                errors.add(ERROR_APM_NAME);
            }
            if (hasError(ERROR_SHOPPER_COUNTRY_CODE)) {
                errors.add(ERROR_SHOPPER_COUNTRY_CODE);
            }
        }

        return errors;
    }

}
