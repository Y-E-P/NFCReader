package ua.lampa.nfcreader.model;

/**
 * Created by yep on 11/3/15.
 */
public class NDEFExternalType extends BaseRecord {

    public String extContent;

    public String toString() {
        StringBuffer buffer = new StringBuffer(super.toString());
        buffer.append("Content: " + extContent);
        return buffer.toString();
    }


    public static NDEFExternalType createRecord(byte[] payload) {
        NDEFExternalType result = new NDEFExternalType();
        StringBuffer pCont = new StringBuffer();
        for (int rn=0; rn < payload.length;rn++) {
            pCont.append(( char) payload[rn]);
        }

        result.extContent = pCont.toString();
        return result;
    }
}