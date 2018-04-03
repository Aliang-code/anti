package dna.origins.commons;

public abstract class HttpCallback {

    public abstract void onSuccess(String body);

    public void onError(String error) {
        //override this method if you need do something on request error
    }

    public void onComplete(String data) {
        //you maybe also need do something whenever success or error
    }

    public void throwError(String error) {
        onError(error);
    }
}
