package ca.sfu.lifehackandroid;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


// Stolen from https://medium.com/@ssaurel/create-a-nfc-reader-application-for-android-74cf24f38a6f
public class NFCMessageParser implements NFCMessage {

    private final String mLanguageCode;
    private final String mText;

    public NFCMessageParser(String languageCode, String text) {
//        mLanguageCode = Preconditions.checkNotNull(languageCode);
//        mText = Preconditions.checkNotNull(text);
        mLanguageCode = languageCode;
        mText = text;
    }

    public String str() {
        return mText;
    }

    public String getText() {
        return mText;
    }

    /**
     * Returns the ISO/IANA language code associated with this text element.
     */
    public String getLanguageCode() {
        return mLanguageCode;
    }

    public static List<NFCMessage> parse(NdefMessage message) {
        return getRecords(message.getRecords());
    }

    public static List<NFCMessage> getRecords(NdefRecord[] records) {
        List<NFCMessage> elements = new ArrayList<NFCMessage>();

        for (final NdefRecord record : records) {
            if (isText(record)) {
                elements.add(parse(record));
            } else {
                elements.add(new NFCMessage() {
                    public String str() {
                        return new String(record.getPayload());
                    }
                });
            }
        }

        return elements;
    }

    public static NFCMessageParser parse(NdefRecord record) {
//        Preconditions.checkArgument(record.getTnf() == NdefRecord.TNF_WELL_KNOWN);
//        Preconditions.checkArgument(Arrays.equals(record.getType(), NdefRecord.RTD_TEXT));
        try {
            byte[] payload = record.getPayload();
            /*
             * payload[0] contains the "Status Byte Encodings" field, per the
             * NFC Forum "Text Record Type Definition" section 3.2.1.
             *
             * bit7 is the Text Encoding Field.
             *
             * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
             * The text is encoded in UTF16
             *
             * Bit_6 is reserved for future use and must be set to zero.
             *
             * Bits 5 to 0 are the length of the IANA language code.
             */
            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 0077;
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            String text =
                    new String(payload, languageCodeLength + 1,
                            payload.length - languageCodeLength - 1, textEncoding);
            return new NFCMessageParser(languageCode, text);
        } catch (UnsupportedEncodingException e) {
            // should never happen unless we get a malformed tag.
            throw new IllegalArgumentException(e);
        }
    }

    public static boolean isText(NdefRecord record) {
        try {
            parse(record);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}