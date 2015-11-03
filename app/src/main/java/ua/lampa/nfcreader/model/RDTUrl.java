package ua.lampa.nfcreader.model;

/**
 * Created by yep on 11/3/15.
 */
public class RDTUrl extends BaseRecord {
        public String url;
        public int prefix;


    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Prefix:" + prefix + "   " + NFCProtocol.getProtocol(prefix) +  url);
        return buffer.toString();
    }
}
