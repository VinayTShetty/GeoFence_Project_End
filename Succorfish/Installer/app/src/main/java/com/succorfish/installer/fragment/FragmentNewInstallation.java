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
import com.succorfish.installer.interfaces.onNewInstallationBackNext;
import com.succorfish.installer.views.StepperIndicator;

/**
 * Created by Jaydeep on 21-02-2018.
 */

public class FragmentNewInstallation extends Fragment {

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
        mViewRoot = inflater.inflate(R.layout.fragment_new_installation, container, false);
        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_dashboard_menu_new_installation));
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
        mActivity.mViewPagerInstallation = mViewRoot.findViewById(R.id.container);
        stepperIndicator = mViewRoot.findViewById(R.id.stepperIndicator);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        mActivity.mViewPagerInstallation.setAdapter(mSectionsPagerAdapter);
        mActivity.mTestDeviceStatus = 0;
        mActivity.mStringTestDeviceReportedDate = "";
        mActivity.mIsTestDeviceCheck = false;
        stepperIndicator.showLabels(false);
        stepperIndicator.setViewPager(mActivity.mViewPagerInstallation);
        /*// or manual change
        indicator.setStepCount(3);
        indicator.setCurrentStep(2);
*/
        mActivity.setOnBackFrgPress(new onFragmentBackPress() {
            @Override
            public void onFragmentBackPress(Fragment mFragment) {
                if (mFragment instanceof FragmentNewInstallation) {
                    System.out.println("BackKK");
                    int currentPage = mActivity.mViewPagerInstallation.getCurrentItem();
                    Fragment page = getChildFragmentManager().findFragmentByTag("android:switcher:" + R.id.container + ":" + mActivity.mViewPagerInstallation.getCurrentItem());
                    System.out.println("page-" + page.toString());
                    System.out.println("currentPage-" + currentPage);
                    if (page instanceof FragmentNewInstallationOne) {
                        mActivity.onBackPressedDirect();
//                        if (mActivity.mIntInstallationId != 0) {
//
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
                    } else if (page instanceof FragmentNewInstallationTwo) {
                        FragmentNewInstallationTwo mFragmentNewInstallationTwo = (FragmentNewInstallationTwo) page;
                        mFragmentNewInstallationTwo.onBackPressCall(mActivity);
                        mActivity.mViewPagerInstallation.setCurrentItem(0, true);
                    } else if (page instanceof FragmentNewInstallationThree) {
                        FragmentNewInstallationThree mFragmentNewInstallationThree = (FragmentNewInstallationThree) page;
                        mFragmentNewInstallationThree.onBackPressCall(mActivity);
//                        ((FragmentNewInstallationThree)  mSectionsPagerAdapter.getItem(currentPage)).onBackPressCall(mActivity);
                        mActivity.mViewPagerInstallation.setCurrentItem(1, true);
                    } else {
                        mActivity.onBackPressedDirect();
//                        if (mActivity.mIntInstallationId != 0) {
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
//                    if (currentPage == 0) {
//                        if (mActivity.mIntInstallationId != 0) {
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
//                    } else if (currentPage == 1) {
////                        ((FragmentNewInstallationTwo) mSectionsPagerAdapter.getItem(currentPage)).onBackPressCall(mActivity);
//                        mActivity.mViewPagerInstallation.setCurrentItem(0, true);
//                    } else if (currentPage == 2) {
////                        ((FragmentNewInstallationThree)  mSectionsPagerAdapter.getItem(currentPage)).onBackPressCall(mActivity);
//                        mActivity.mViewPagerInstallation.setCurrentItem(1, true);
//                    } else {
//                        if (mActivity.mIntInstallationId != 0) {
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
//                    }
                }
            }
        });

        mActivity.setNewInstallationBackNext(new onNewInstallationBackNext() {
            @Override
            public void onInstallFirstBack(Fragment fragment) {

            }

            @Override
            public void onInstallFirstNext(Fragment fragment) {
                System.out.println("FirstNExt");
                mActivity.mViewPagerInstallation.setCurrentItem(1, true);
            }

            @Override
            public void onInstallSecondBack(Fragment fragment) {
                System.out.println("SecondBAck");
                mActivity.mViewPagerInstallation.setCurrentItem(0, true);
            }

            @Override
            public void onInstallSecondNext(Fragment fragment) {
                System.out.println("SecondNext");
                mActivity.mViewPagerInstallation.setCurrentItem(2, true);
            }

            @Override
            public void onInstallThirdBack(Fragment fragment) {
                System.out.println("ThirdBAck");
                mActivity.mViewPagerInstallation.setCurrentItem(1, true);
            }

            @Override
            public void onInstallThirdComplete(Fragment fragment) {
                System.out.println("ThirdNext");

            }
        });

        return mViewRoot;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mImageViewBack.setVisibility(View.GONE);
    }

//    @Override
//    public void onNextPressed(Fragment fragment) {
//        mViewPager.setCurrentItem(1, true);
//    }
//
//    @Override
//    public void onBackTwoPressed(Fragment fragment) {
//        mViewPager.setCurrentItem(0, true);
//    }
//
//    @Override
//    public void onNextTwoPressed(Fragment fragment) {
//        mViewPager.setCurrentItem(2, true);
//    }
//
//    @Override
//    public void onBackThreePressed(Fragment fragment) {
//        mViewPager.setCurrentItem(1, true);
//    }
//
//    @Override
//    public void onNextThreePressed(Fragment fragment) {
//        Toast.makeText(mActivity, "Thanks For Registering", Toast.LENGTH_SHORT).show();
//    }

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
                    return FragmentNewInstallationOne.newInstance();
                case 1:
                    return FragmentNewInstallationTwo.newInstance();
                case 2:
                    return FragmentNewInstallationThree.newInstance();
            }

            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
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
                    return "Second Level";
                case 2:
                    return "Finish";
            }
            return null;
        }
    }
}
