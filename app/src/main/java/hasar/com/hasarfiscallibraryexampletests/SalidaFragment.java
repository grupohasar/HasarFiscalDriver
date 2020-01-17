package hasar.com.hasarfiscallibraryexampletests;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class SalidaFragment extends Fragment {
    private ListView salidaList;
    public static ArrayList<String> names;
    public File file;
    private Button refreshButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_salida, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        salidaList = getView().findViewById(R.id.salidaList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, names);
        salidaList.setAdapter(adapter);

        refreshButton = getView().findViewById(R.id.buttonRefresh);

        refreshButton.setOnClickListener(v -> {
            Collections.sort(names);
            Collections.reverse(names);
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        });
    }

}










