package com.worldpay;

import java.util.ArrayList;

/**
 * Represents a list of card validation errors , using error codes. <br>
 * <p>
 * <h4>Usage :</h4>
 * <p>
 * <ul>
 * <li>To check if contains any error : {@link CardValidationError#hasErrors()}<br>
 * </li>
 * <li>To check if there is a specific error : {@link CardValidationError#hasError(int)}</li>
 * <li>To get a description for an error : {@link CardValidationError#getDescription(int)}</li>
 * </ul>
 * <p>
 * {@link Card#validate()} returns this class in order to specify validation errors. An example
 * could be : <br>
 * <p>
 * <pre>
 *
 * CardValidationError validationError = card.validate();
 *
 * if (validationError != null) {
 * 	//we got errors
 * 	if (validationError.hasError(CardValidationError.ERROR_CARD_EXPIRY)) {
 * 		//something is wrong with expiry date
 *    }
 * 	if (validationError.hasError(CardValidationError.ERROR_HOLDER_NAME)) {
 * 		//something is wrong with the holder name
 *    }
 * 	// etc ...
 *
 * }
 * </pre>
 * <p>
 * </p>
 * <h4>Possible error values</h4>
 * <ul>
 * <li>{@link CardValidationError#ERROR_CARD_EXPIRY}</li>
 * <li>{@link CardValidationError#ERROR_CVC}</li>
 * <li>{@link CardValidationError#ERROR_HOLDER_NAME}</li>
 * <li>{@link CardValidationError#ERROR_CARD_NUMBER}</li>
 * </ul>
 */
public class CardValidationError {
    public final static int ERROR_CARD_EXPIRY = 2 << 1;
    public final static int ERROR_CVC = 2 << 2;
    public final static int ERROR_HOLDER_NAME = 2 << 3;
    public final static int ERROR_CARD_NUMBER = 2 << 4;

    //holds the error mask
    private int error = 0;

    public CardValidationError() {

    }

    /**
     * Returns a description for the specified code.
     *
     * @param code
     * @return Returns null if the code is not valid.
     */

    public static int getDescription(int code) {
        switch (code) {
            case ERROR_CARD_EXPIRY:
                return R.string.cardExpiryError;
            case ERROR_CVC:
                return R.string.cardCvcError;
            case ERROR_HOLDER_NAME:
                return R.string.cardNameError;
            case ERROR_CARD_NUMBER:
                return R.string.cardNumberError;
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
        ArrayList<Integer> errors = new ArrayList<Integer>();
        if (error != 0) {
            if (hasError(ERROR_CARD_EXPIRY)) {
                errors.add(getDescription(ERROR_CARD_EXPIRY));
            }
            if (hasError(ERROR_CVC)) {
                errors.add(getDescription(ERROR_CVC));
            }
            if (hasError(ERROR_HOLDER_NAME)) {
                errors.add(getDescription(ERROR_HOLDER_NAME));
            }
            if (hasError(ERROR_CARD_NUMBER)) {
                errors.add(getDescription(ERROR_CARD_NUMBER));
            }
        }

        return errors;
    }

}
