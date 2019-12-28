package com.joecompany.pinit.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.joecompany.pinit.constants.LogTags;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapUtil {

    public static String getAddress(Context context, LatLng latLng){

        String mapAddress = "No address found. Probably in the ocean.";

        try {

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());

            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addressList != null && addressList.size() > 0) {

                Address address = addressList.get(0);

                StringBuilder sb = new StringBuilder();

                if(address.getSubThoroughfare() != null && address.getThoroughfare() != null)
                    sb.append(address.getSubThoroughfare() + " " + address.getThoroughfare()).append(", ");

                if(address.getLocality() != null)
                    sb.append(address.getLocality()).append(", ");

                if(address.getPostalCode() != null)
                    sb.append(address.getPostalCode()).append(", ");

                if(address.getCountryName() != null)
                    sb.append(address.getCountryName());

                mapAddress = sb.toString();
            }
        } catch (IOException e) {
            Log.e(LogTags.PINIT, "Unable to find address", e);
        }

        return mapAddress;
    }
}
