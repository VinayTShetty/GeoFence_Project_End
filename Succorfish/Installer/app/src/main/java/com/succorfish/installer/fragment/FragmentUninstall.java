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
import com.succorfish.installer.views.StepperIndicator;

/**
 * Created by Jaydeep on 27-02-2018.
 */

public class FragmentUninstall extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    private StepperIndicator stepperIndicator;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_uninstall, container, false);
        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_dashboard_menu_uninstall));
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
        mActivity.mViewPager = mViewRoot.findViewById(R.id.fragment_uninstaller_container);
        stepperIndicator = mViewRoot.findViewById(R.id.fragment_uninstaller_stepperIndicator);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mActivity.mViewPager.setAdapter(mSectionsPagerAdapter);
        mActivity.mTestDeviceStatus=0;
        mActivity.mStringTestDeviceReportedDate="";
        mActivity.mIsTestDeviceCheck = false;
        stepperIndicator.showLabels(false);
        stepperIndicator.setViewPager(mActivity.mViewPager);
        // or keep last page as "end page"
        stepperIndicator.setViewPager(mActivity.mViewPager, mActivity.mViewPager.getAdapter().getCount() - 1); //

        /*// or manual change
        indicator.setStepCount(3);
        indicator.setCurrentStep(2);
*/
        mActivity.setOnBackFrgPress(new onFragmentBackPress() {
            @Override
            public void onFragmentBackPress(Fragment mFragment) {
                if (mFragment instanceof FragmentUninstall) {
                    System.out.println("BackKK");
                    int currentPage = mActivity.mViewPager.getCurrentItem();
                    Fragment page = getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.fragment_uninstaller_container + ":" + mActivity.mViewPager.getCurrentItem());
                    System.out.println("page-" + page.toString());
                    if (page instanceof FragmentUninstallOne) {
                        mActivity.onBackPressedDirect();
//                        if (mActivity.mIntUnInstallationId != 0) {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
//                            builder.setCancelable(false);
//                            builder.setMessage(getResources().getString(R.string.str_back_confirmation));
//                            builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    mActivity.onBackPressedDirect();
//                                }
//                            });
//                            builder.setNegativeButton(getResources().getString(R.string.str_no), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                            builder.show();
//                        } else {
//                            mActivity.onBackPressedDirect();
//                        }
                    }else if (page instanceof FragmentUninstallTwo) {
                        FragmentUninstallTwo mFragmentUninstallTwo = (FragmentUninstallTwo) page;
                        mFragmentUninstallTwo.onBackPressCall(mActivity);
                        mActivity.mViewPager.setCurrentItem(0, true);
                    }else {
                        mActivity.onBackPressedDirect();
//                        if (mActivity.mIntUnInstallationId != 0) {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
//                            builder.setCancelable(false);
//                            builder.setMessage(getResources().getString(R.string.str_back_confirmation));
//                            builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    mActivity.onBackPressedDirect();
//                                }
//                            });
//                            builder.setNegativeButton(getResources().getString(R.string.str_no), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                            builder.show();
//                        } else {
//                            mActivity.onBackPressedDirect();
//                        }
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
                    return FragmentUninstallOne.newInstance();
                case 1:
                    return FragmentUninstallTwo.newInstance();
            }

            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
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
