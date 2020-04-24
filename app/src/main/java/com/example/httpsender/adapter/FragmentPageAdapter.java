package com.example.httpsender.adapter;



import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.Arrays;
import java.util.List;


/**
 * User: ljx
 * Date: 2018/6/9
 * Time: 13:53
 */
public class FragmentPageAdapter extends FragmentPagerAdapter {

    private List<? extends Fragment> mFragments;
    private List<? extends CharSequence> mTitles;


    public FragmentPageAdapter(FragmentManager fm, List<? extends Fragment> fragments, String[] titles) {
        this(fm, fragments, Arrays.asList(titles));
    }

    public FragmentPageAdapter(FragmentManager fm, List<? extends Fragment> fragments, List<? extends CharSequence> titles) {
        super(fm);
        mFragments = fragments;
        mTitles = titles;
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }
}
