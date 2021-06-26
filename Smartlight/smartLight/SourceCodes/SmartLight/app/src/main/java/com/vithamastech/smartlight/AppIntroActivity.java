package com.vithamastech.smartlight;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.vithamastech.smartlight.Views.CircleIndicatorView;
import com.vithamastech.smartlight.Vo.VoAppIntro;
import com.vithamastech.smartlight.fragments.FragmentAppIntro;
import com.vithamastech.smartlight.helper.PreferenceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AppIntroActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private Integer[] colors;
    private CircleIndicatorView circleIndicatorView;
    private ViewPager vpOnboarderPager;
    private AppIntroAdapter mAppIntroAdapter;
    private ImageButton ibNext;
    private Button btnSkip, btnFinish;
    private RelativeLayout buttonsLayout;
    private FloatingActionButton fab;
    private View divider;
    private ArgbEvaluator evaluator;

    private boolean shouldDarkenButtonsLayout = false;
    private boolean shouldUseFloatingActionButton = false;
    private boolean isFirstTime = false;
    private PreferenceHelper mPreferenceHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_appintro);
        isFirstTime = getIntent().getBooleanExtra("isFirstTime", false);
        setStatusBackgroundColor();
        mPreferenceHelper = new PreferenceHelper(AppIntroActivity.this);
        circleIndicatorView = (CircleIndicatorView) findViewById(R.id.circle_indicator_view);
        ibNext = (ImageButton) findViewById(R.id.ib_next);
        btnSkip = (Button) findViewById(R.id.btn_skip);
        btnFinish = (Button) findViewById(R.id.btn_finish);
        buttonsLayout = (RelativeLayout) findViewById(R.id.buttons_layout);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        divider = findViewById(R.id.divider);
        vpOnboarderPager = (ViewPager) findViewById(R.id.vp_onboarder_pager);
        vpOnboarderPager.addOnPageChangeListener(this);
        ibNext.setOnClickListener(this);
        btnSkip.setOnClickListener(this);
        btnFinish.setOnClickListener(this);
        fab.setOnClickListener(this);
        evaluator = new ArgbEvaluator();

        List<VoAppIntro> pages = new ArrayList<>();
        // Add App Intro screen image is list.
        pages.add(new VoAppIntro("", "", R.drawable.screenshot_1));
        pages.add(new VoAppIntro("", "", R.drawable.screenshot_1));
        pages.add(new VoAppIntro("", "", R.drawable.screenshot_2));
        pages.add(new VoAppIntro("", "", R.drawable.screenshot_3));
        pages.add(new VoAppIntro("", "", R.drawable.screenshot_4));
        pages.add(new VoAppIntro("", "", R.drawable.screenshot_5));
        pages.add(new VoAppIntro("", "", R.drawable.screenshot_6));
        pages.add(new VoAppIntro("", "", R.drawable.screenshot_7));
        pages.add(new VoAppIntro("", "", R.drawable.screenshot_8));
        pages.add(new VoAppIntro("", "", R.drawable.screenshot_10));
        for (VoAppIntro page : pages) {
            page.setTitleColor(R.color.colorWhite);
            page.setDescriptionColor(R.color.colorWhite);
            page.setMultilineDescriptionCentered(true);
        }

//        setSkipButtonTitle("Skip");
//        setFinishButtonTitle("Finish");
        shouldUseFloatingActionButton(true);
        setOnboardPagesReady(pages);
    }
    /* Get Color of the background*/
    public static Integer[] getPageBackgroundColors(Context context, List<VoAppIntro> pages) {
        List<Integer> colorsList = new ArrayList<>();
        for (VoAppIntro page : pages) {
            colorsList.add(ContextCompat.getColor(context, page.getBackgroundColor()));
        }
        return colorsList.toArray(new Integer[pages.size()]);
    }

    /*Initialize viewpager adapter*/
    public void setOnboardPagesReady(List<VoAppIntro> pages) {
        mAppIntroAdapter = new AppIntroAdapter(pages, getSupportFragmentManager());
        vpOnboarderPager.setAdapter(mAppIntroAdapter);
        colors = getPageBackgroundColors(this, pages);
        circleIndicatorView.setPageIndicators(pages.size());
    }

    public void setInactiveIndicatorColor(int color) {
        this.circleIndicatorView.setInactiveIndicatorColor(color);
    }

    public void setActiveIndicatorColor(int color) {
        this.circleIndicatorView.setActiveIndicatorColor(color);
    }

    public void shouldDarkenButtonsLayout(boolean shouldDarkenButtonsLayout) {
        this.shouldDarkenButtonsLayout = shouldDarkenButtonsLayout;
    }

    public void setDividerColor(@ColorInt int color) {
        if (!this.shouldDarkenButtonsLayout)
            this.divider.setBackgroundColor(color);
    }

    public void setDividerHeight(int heightInDp) {
        if (!this.shouldDarkenButtonsLayout)
            this.divider.getLayoutParams().height =
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightInDp,
                            getResources().getDisplayMetrics());
    }

    public void setDividerVisibility(int dividerVisibility) {
        this.divider.setVisibility(dividerVisibility);
    }

    public void setSkipButtonTitle(CharSequence title) {
        this.btnSkip.setText(title);
    }

    public void setSkipButtonHidden() {
        this.btnSkip.setVisibility(View.GONE);
    }

    public void setSkipButtonTitle(@StringRes int titleResId) {
        this.btnSkip.setText(titleResId);
    }

    public void setFinishButtonTitle(CharSequence title) {
        this.btnFinish.setText(title);
    }

    public void setFinishButtonTitle(@StringRes int titleResId) {
        this.btnFinish.setText(titleResId);
    }

    public void shouldUseFloatingActionButton(boolean shouldUseFloatingActionButton) {

        this.shouldUseFloatingActionButton = shouldUseFloatingActionButton;

        if (shouldUseFloatingActionButton) {
            this.fab.setVisibility(View.VISIBLE);
            this.setDividerVisibility(View.GONE);
            this.shouldDarkenButtonsLayout(false);
            this.btnFinish.setVisibility(View.GONE);
            this.ibNext.setVisibility(View.GONE);
            this.ibNext.setFocusable(false);
//            this.buttonsLayout.getLayoutParams().height =
//                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96,
//                            getResources().getDisplayMetrics());
        }

    }

    private int darkenColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.9f;
        return Color.HSVToColor(hsv);
    }

    public void setStatusBackgroundColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorSemiTransparent));
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        boolean isInLastPage = vpOnboarderPager.getCurrentItem() == mAppIntroAdapter.getCount() - 1;
        if (i == R.id.ib_next || i == R.id.fab && !isInLastPage) {
            // To change Next page of the screen
            vpOnboarderPager.setCurrentItem(vpOnboarderPager.getCurrentItem() + 1);
        } else if (i == R.id.btn_skip) {
            // To Skip the all step
            onSkipButtonPressed();
        } else if (i == R.id.btn_finish || i == R.id.fab && isInLastPage) {
            // Finish all the page and redirect to destination page.
//            onFinishButtonPressed();
            if (isFirstTime) {
                mPreferenceHelper.setIsShowIntro(false);
                // Generate Random No and store in self device id. we pass this self device id while communicate with light
                mPreferenceHelper.setSelfDeviceId(generateRandomNo() + "");
                navigateToLoginExplainActivity();
            } else {
                finishAffinity();
            }
        }
    }
    /* Generate 4 Digit No.*/
    private int generateRandomNo() {
        Random random = new Random();
        String generatedPassword = String.format("%04d", random.nextInt(9999));
        int generatedNo = Integer.parseInt(generatedPassword.toString());
        System.out.println("generatedNo-" + generatedNo);
        if (generatedNo < 999) {
            return generateRandomNo();
        }
        return Integer.parseInt(generatedPassword.toString());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (position < (mAppIntroAdapter.getCount() - 1) && position < (colors.length - 1)) {
            vpOnboarderPager.setBackgroundColor((Integer) evaluator.evaluate(positionOffset, colors[position], colors[position + 1]));
            if (shouldDarkenButtonsLayout) {
                buttonsLayout.setBackgroundColor(darkenColor((Integer) evaluator.evaluate(positionOffset, colors[position], colors[position + 1])));
                divider.setVisibility(View.GONE);
            }
        } else {
            vpOnboarderPager.setBackgroundColor(colors[colors.length - 1]);
            if (shouldDarkenButtonsLayout) {
                buttonsLayout.setBackgroundColor(darkenColor(colors[colors.length - 1]));
                divider.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onPageSelected(int position) {
        int lastPagePosition = mAppIntroAdapter.getCount() - 1;
        circleIndicatorView.setCurrentPage(position);
//        ibNext.setVisibility(position == lastPagePosition && !this.shouldUseFloatingActionButton ? View.GONE : View.VISIBLE);
//        btnFinish.setVisibility(position == lastPagePosition && !this.shouldUseFloatingActionButton ? View.VISIBLE : View.GONE);
        if (this.shouldUseFloatingActionButton)
            this.fab.setImageResource(position == lastPagePosition ? R.drawable.ic_check_white_24dp : R.drawable.ic_arrow_forward_white_24dp);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    protected void onSkipButtonPressed() {
//        vpOnboarderPager.setCurrentItem(mAppIntroAdapter.getCount());
        if (isFirstTime) {
            mPreferenceHelper.setIsShowIntro(false);
            // Generate Random No and store in self device id. we pass this self device id while communicate with light
            mPreferenceHelper.setSelfDeviceId(generateRandomNo() + "");
            navigateToLoginExplainActivity();
        } else {
            finishAffinity();
        }
//        if (!isFirstTime) {
//            if (mAppIntroAdapter.getCount() == 0) {
//                finish();
//            }
//        }
    }

    private void navigateToLoginExplainActivity(){
        Intent mIntent = new Intent(AppIntroActivity.this, LoginExplainActivity.class);
        mIntent.putExtra("is_from_add_account", false);
        startActivity(mIntent);
        finishAffinity();
    }

    public class AppIntroAdapter extends FragmentStatePagerAdapter {

        List<VoAppIntro> pages = new ArrayList<VoAppIntro>();

        public AppIntroAdapter(List<VoAppIntro> pages, FragmentManager fm) {
            super(fm);
            this.pages = pages;
        }

        @Override
        public Fragment getItem(int position) {
            return FragmentAppIntro.newInstance(pages.get(position));
        }

        @Override
        public int getCount() {
            return pages.size();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.activityPaused();
    }
}