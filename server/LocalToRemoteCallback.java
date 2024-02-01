package server;

import utils.Callback;
import utils.Tuple;

/**
 * Local callback, that calls the remote one given.
 */
public class LocalToRemoteCallback implements Callback {

    IRemoteCallback cbr;

    public LocalToRemoteCallback (IRemoteCallback cbr) {
        this.cbr = cbr;
    }

    public void call (Tuple t){
        try {
            cbr.call(t);
        } catch (Exception I) {
            I.printStackTrace();
        }
    }
}
