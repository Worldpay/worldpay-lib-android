package com.example.worldfood.order;

import android.content.Context;
import android.util.Log;

import com.worldpay.ResponseCard;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

final class OnDiskCardRepository implements CardRepository {

    private static final String TAG = "OnDiskCardRepository";

    private static final String CARDS_FILE_NAME = "SavedCards";

    private List<ResponseCard> cachedCards;

    @Override
    public ResponseCard get(final Context context, final int index) {
        return findAll(context).get(index);
    }

    @Override
    public List<ResponseCard> findAll(final Context context) {
        if (cachedCards != null) {
            return cachedCards;
        }
        cachedCards = new ArrayList<>();
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(context.openFileInput(CARDS_FILE_NAME));
            populateListWithSavedResponseCards(cachedCards, is);
            Log.i(TAG, "Found " + cachedCards.size() + " saved card(s)");
        } catch (FileNotFoundException e) {
            Log.i(TAG, "No saved cards");
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "Could not load cards", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "ObjectInputStream could not be closed", e);
                }
            }
        }
        return cachedCards;
    }

    @Override
    public void save(final List<ResponseCard> cards, final Context context) {
        cachedCards = cards;
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(context.openFileOutput(CARDS_FILE_NAME, MODE_PRIVATE));
            oos.writeObject(cachedCards);
            oos.flush();
            Log.d(TAG, cards.size() + " card(s) saved.");
        } catch (IOException e) {
            Log.e(TAG, "Cards could not be saved", e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    Log.e(TAG, "OutputStream could not be closed", e);
                }
            }
        }
    }

    private void populateListWithSavedResponseCards(final List<ResponseCard> cards,
                                                    final ObjectInputStream is) throws ClassNotFoundException, IOException {
        final Object readObject = is.readObject();
        if (readObject instanceof ArrayList) {
            final ArrayList arrayList = (ArrayList) readObject;
            for (final Object entry : arrayList) {
                if (entry instanceof ResponseCard) {
                    final ResponseCard responseCard = (ResponseCard) entry;
                    Log.d(TAG, "Found " + responseCard);
                    cards.add(responseCard);
                }
            }
        }
    }
}
