package com.joecompany.pinit.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.gson.internal.LinkedTreeMap;
import com.joecompany.pinit.R;
import com.joecompany.pinit.constants.StorageKeys;
import com.joecompany.pinit.data.PinData;
import com.joecompany.pinit.utils.StorageUtil;

import java.util.ArrayList;


public class PinsFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_pins, container, false);

        String fbId = (String)StorageUtil.get(getActivity(), StorageKeys.FB_ID, String.class);

        ArrayList<LinkedTreeMap> myStoredData = (ArrayList<LinkedTreeMap>) StorageUtil.get(getActivity(), fbId, ArrayList.class);

        if(myStoredData != null && myStoredData.size() != 0) {

            final ListView list = root.findViewById(R.id.list);

            ArrayList<String> pinAddressNames = new ArrayList<>();

            for(int i = 0; i < myStoredData.size(); i++){
                LinkedTreeMap storedPin = myStoredData.get(i);
                pinAddressNames.add((String)storedPin.get(PinData.PIN_FIELD_NAME));
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(root.getContext(), android.R.layout.simple_list_item_1, pinAddressNames);

            list.setAdapter(arrayAdapter);
        }

        return root;
    }
}