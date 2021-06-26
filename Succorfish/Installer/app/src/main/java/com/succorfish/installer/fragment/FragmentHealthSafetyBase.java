package com.succorfish.installer.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.succorfish.installer.MainActivity;
import com.succorfish.installer.R;
import com.succorfish.installer.interfaces.onFragmentBackPress;

/**
 * Created by Jaydeep on 27-02-2018.
 */

public class FragmentHealthSafetyBase extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_health_safety_base, container, false);
        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_frg_health_n_safety_title));

        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
        mActivity.mViewPagerQuestion = mViewRoot.findViewById(R.id.frg_h_s_base_container);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mActivity.mViewPagerQuestion.setAdapter(mSectionsPagerAdapter);
        mActivity.setOnBackFrgPress(new onFragmentBackPress() {
            @Override
            public void onFragmentBackPress(Fragment mFragment) {
                if (mFragment instanceof FragmentHealthSafetyBase) {
                    System.out.println("BackKK");
                    int currentPage = mActivity.mViewPagerQuestion.getCurrentItem();
                    Fragment page = getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.frg_h_s_base_container + ":" + mActivity.mViewPagerQuestion.getCurrentItem());
                    System.out.println("page-" + page.toString());
                    if (page instanceof FragmentHealthSafetyQuestionOne) {
                        mActivity.onBackPressedDirect();
                    }else if (page instanceof FragmentHealthSafetyQuestionTwo) {
                        mActivity.mViewPagerQuestion.setCurrentItem(0, true);
                    }else {
                        mActivity.onBackPressedDirect();
                    }
                }
            }
        });


        return mViewRoot;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mImageViewBack.setVisibility(View.GONE);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return FragmentHealthSafetyQuestionOne.newInstance();
                case 1:
                    return FragmentHealthSafetyQuestionTwo.newInstance();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return super.getItemPosition(object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "First Level";
                case 1:
                    return "Finish";
            }
            return null;
        }
    }
}
