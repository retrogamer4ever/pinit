package com.joecompany.pinit.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.internal.LinkedTreeMap;
import com.joecompany.pinit.MainActivity;
import com.joecompany.pinit.R;
import com.joecompany.pinit.data.PinData;
import com.joecompany.pinit.utils.DialogUtil;
import com.joecompany.pinit.utils.StorageUtil;

import java.sql.Array;
import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                ViewModelProviders.of(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        ArrayList<LinkedTreeMap> myStoredData = (ArrayList<LinkedTreeMap>) StorageUtil.get(getActivity(), "joespins", ArrayList.class);

        if(myStoredData != null && myStoredData.size() != 0) {
            final ListView list = root.findViewById(R.id.list);
            ArrayList<String> arrayList = new ArrayList<>();

            for(int i = 0; i < myStoredData.size(); i++){
                LinkedTreeMap tree = myStoredData.get(i);
                arrayList.add((String)tree.get("name"));
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(root.getContext(), android.R.layout.simple_list_item_1, arrayList);
            list.setAdapter(arrayAdapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String clickedItem=(String) list.getItemAtPosition(position);
                    //Toast.makeText(MainActivity.this,clickedItem,Toast.LENGTH_LONG).show();
                }
            });
        }




        return root;
    }
}