package hasar.com.hasarfiscallibrarygithub;

import android.util.Log;

import com.hasar.fiscal.exceptions.FiscalDriverException;
import com.hasar.fiscal.services.base.ServiceCallback;

abstract class BaseServiceCallback<T> implements ServiceCallback<T> {

    abstract void onFinish(T response);

    @Override
    public void onResult(T response) {
        onFinish(response);
    }

    @Override
    public void onError(FiscalDriverException ex) {

        Log.e("Callback OnError: ", ex.toString());
    }
}