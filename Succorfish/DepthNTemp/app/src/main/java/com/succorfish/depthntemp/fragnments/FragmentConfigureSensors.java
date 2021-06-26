package com.succorfish.depthntemp.fragnments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.succorfish.depthntemp.MainActivity;
import com.succorfish.depthntemp.R;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 16-02-2018.
 */

public class FragmentConfigureSensors extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    //    @BindView(R.id.frg_configure_sensor_smart_tab)
//    TabLayout mSmartTabLayout;
    //    @BindView(R.id.frg_configure_sensor_viewpager)
//    ViewPager mViewPager;
//    FragmentStatePagerItemAdapter mFragmentStatePagerItemAdapter;
//    MyPagerAdapter adapterViewPager;
//    private static List<Fragment> mFragmentList = new ArrayList<>();
//    private FragmentDeviceList mFragmentDeviceList;
//    private FragmentDeviceSetting mFragmentDeviceSetting;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_configure_sensors, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mToolbar.setVisibility(View.VISIBLE);
        mActivity.mTextViewTitle.setText(R.string.str_menu_configure_sensor);
        mActivity.mTextViewTitle.setVisibility(View.VISIBLE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAddDevice.setVisibility(View.VISIBLE);
        mActivity.mTextViewAdd.setVisibility(View.GONE);
        mActivity.mLeDevices = new ArrayList<>();
        mActivity.mSmartTabLayout = (TabLayout) mViewRoot.findViewById(R.id.frg_configure_sensor_smart_tab);
        mActivity.mSmartTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setCurrentTabFragment(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        setupTabLayout();
//        setupViewPager(mViewPager);
//        mSmartTabLayout.setupWithViewPager(mViewPager);

//        mViewPager.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Fragment mFragment =adapterViewPager.getRegisteredFragment(mViewPager.getCurrentItem());
//                if (mFragment instanceof FragmentDeviceList) {
//                    mFragment.updateView(0, mVoVesselList.get(0),MainActivity.this);
//                }else {
//
//                }
//                if (mVoVesselList != null) {
//                    if (mVoVesselList.size() > 0) {
//                        mFragmentAsset.updateView(0, mVoVesselList.get(0),MainActivity.this);
//                    }
//                }
//            }
//        }, 100);
//        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                System.out.println("JD -" + position);
////                if (!(adapterViewPager == null)) {
////                    adapterViewPager.notifyDataSetChanged();
////                }
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });

//        FragmentStatePagerItemAdapter  adapter = new FragmentStatePagerItemAdapter (
//                getChildFragmentManager(), FragmentPagerItems.with(mActivity)
//                .add(getResources().getString(R.string.str_pair_device), FragmentDeviceList.class)
//                .add(getResources().getString(R.string.str_device_setting), FragmentDeviceSetting.class)
//                .create());
//        FragmentPagerItems  pages = new FragmentPagerItems(mActivity);
//        pages.add(FragmentPagerItem.of(getResources().getString(R.string.str_pair_device), FragmentDeviceList.class));
//        pages.add(FragmentPagerItem.of(getResources().getString(R.string.str_device_setting), FragmentDeviceSetting.class));
//        pages.add(getResources().getString(R.string.str_pair_device), FragmentDeviceList.class);
//        pages.add("Pair Device", FragmentDeviceSetting.class);

//        mFragmentStatePagerItemAdapter = new FragmentStatePagerItemAdapter(getChildFragmentManager(), pages)
//        {				@Override
//        public int getItemPosition(Object object) {
//            return POSITION_NONE;
//        }
//
//        };
//        mViewPager.setAdapter(mFragmentStatePagerItemAdapter);
//        mSmartTabLayout.setViewPager(mViewPager);
        return mViewRoot;
    }

    private void setupTabLayout() {
//        mFragmentDeviceList = new FragmentDeviceList();
//        mFragmentDeviceSetting = new FragmentDeviceSetting();
        mActivity.mSmartTabLayout.addTab(mActivity.mSmartTabLayout.newTab().setText(getResources().getString(R.string.str_pair_device)), true);
        mActivity.mSmartTabLayout.addTab(mActivity.mSmartTabLayout.newTab().setText(getResources().getString(R.string.str_device_setting)));
    }

//    private void setupViewPager(ViewPager viewPager) {
//        adapterViewPager = new MyPagerAdapter(getChildFragmentManager());
////        ViewPagerAdapter adapterViewPager = new ViewPagerAdapter(getChildFragmentManager());
//        adapterViewPager.addFragment(new FragmentDeviceList(), getResources().getString(R.string.str_pair_device));
//        adapterViewPager.addFragment(new FragmentDeviceSetting(), getResources().getString(R.string.str_device_setting));
//        viewPager.setAdapter(adapterViewPager);
//    }

//    class ViewPagerAdapter extends FragmentPagerAdapter {
//        private final List<Fragment> mFragmentList = new ArrayList<>();
//        private final List<String> mFragmentTitleList = new ArrayList<>();
//
//        public ViewPagerAdapter(FragmentManager manager) {
//            super(manager);
//        }
//
//        @Override
//        public Fragment getItem(int position) {
//            return mFragmentList.get(position);
//        }
//
//        @Override
//        public int getCount() {
//            return mFragmentList.size();
//        }
//
//        public void addFragment(Fragment fragment, String title) {
//            mFragmentList.add(fragment);
//            mFragmentTitleList.add(title);
//        }
//
//        @Override
//        public CharSequence getPageTitle(int position) {
//            return mFragmentTitleList.get(position);
//        }
//    }

//    public static class MyPagerAdapter extends FragmentStatePagerAdapter {
//
//        private final List<String> mFragmentTitleList = new ArrayList<>();
//
//        public MyPagerAdapter(FragmentManager fragmentManager) {
//            super(fragmentManager);
//        }
//
//        // Returns total number of pages
//        @Override
//        public int getCount() {
//            return mFragmentList.size();
//        }
//
//        // Returns the fragment to display for that page
//        @Override
//        public Fragment getItem(int position) {
//            return mFragmentList.get(position);
//        }
//
//        // Returns the page title for the top indicator
//        @Override
//        public CharSequence getPageTitle(int position) {
//            return mFragmentTitleList.get(position);
//        }
//
//        public void addFragment(Fragment fragment, String title) {
//            mFragmentList.add(fragment);
//            mFragmentTitleList.add(title);
//        }
//
//        @Override
//        public int getItemPosition(@NonNull Object object) {
////            if (object instanceof FragmentAsset) {
////                ((FragmentAsset)object).updateView();
////            }
//            return POSITION_NONE;
//        }
//    }

    private void setCurrentTabFragment(int tabPosition) {
        switch (tabPosition) {
            case 0:
                replaceFragment(new FragmentDeviceList());
                break;
            case 1:
                replaceFragment(new FragmentDeviceSetting());
                break;
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame_container, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mToolbar.setVisibility(View.GONE);
        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
        if (mActivity.isDevicesConnected) {
            if (mActivity.mBluetoothDevice != null) {
                mActivity.disconnectDevices(mActivity.mBluetoothDevice, false);
            }
        }
    }


}
