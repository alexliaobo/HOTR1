package com.us.hotr.ui.fragment.info;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.us.hotr.R;

/**
 * Created by Mloong on 2017/9/19.
 */

public class OrderPartyFragment extends Fragment{

    public static OrderPartyFragment newInstance() {
        OrderPartyFragment orderPartyFragment = new OrderPartyFragment();
        return orderPartyFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_beauty, container, false);
    }
}