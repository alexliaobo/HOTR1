package com.us.hotr.ui.activity.info;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

import com.us.hotr.R;
import com.us.hotr.ui.activity.BaseActivity;
import com.us.hotr.ui.fragment.info.OrderBeautyFragment;
import com.us.hotr.ui.fragment.info.OrderMasasgeFragment;
import com.us.hotr.ui.fragment.info.OrderPartyFragment;

import java.util.ArrayList;

/**
 * Created by Mloong on 2017/9/19.
 */

public class OrderListActivity extends BaseActivity {

    private ArrayList<String> titleList;
    private ArrayList<Fragment> fragmentList;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PagerAdapter adapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initStaticView();
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_order_list;
    }

    private void initStaticView(){
        titleList = new ArrayList<String>() {{
            add(getString(R.string.beauty_title));
            add(getString(R.string.massage_title));
            add(getString(R.string.party_title));
        }};

        fragmentList = new ArrayList<Fragment>() {{
            add(OrderBeautyFragment.newInstance());
            add(OrderMasasgeFragment.newInstance());
            add(OrderPartyFragment.newInstance());
        }};

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.pager);

        adapter = new PagerAdapter(getSupportFragmentManager(), titleList, fragmentList);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager, true);

    }

    public class PagerAdapter extends FragmentPagerAdapter {

        private ArrayList<String> titleList;
        private ArrayList<Fragment> fragmentList;

        public PagerAdapter(FragmentManager fm, ArrayList<String> titleList, ArrayList<Fragment> fragmentList) {
            super(fm);
            this.titleList = titleList;
            this.fragmentList = fragmentList;
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }
    }
}