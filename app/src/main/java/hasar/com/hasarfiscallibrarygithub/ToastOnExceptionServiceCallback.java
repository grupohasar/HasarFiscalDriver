package hasar.com.hasarfiscallibrarygithub;

import android.content.Context;
import android.widget.Toast;

import com.hasar.fiscal.exceptions.FiscalDriverException;
import com.hasar.fiscal.services.base.ServiceCallback;

public abstract class ToastOnExceptionServiceCallback<T> implements ServiceCallback<T> {

    private Context mContext;

    public ToastOnExceptionServiceCallback(Context ctx) {
        this.mContext = ctx;
    }

    @Override
    public void onError(FiscalDriverException e) {
        Toast.makeText(mContext, String.format("Exception: %s Type: %s", e.getMessage(), e.getClass().getSimpleName()), Toast.LENGTH_SHORT).show();
    }
}
