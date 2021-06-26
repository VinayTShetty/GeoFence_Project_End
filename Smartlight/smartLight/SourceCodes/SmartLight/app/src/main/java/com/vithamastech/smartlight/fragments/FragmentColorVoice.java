package com.vithamastech.smartlight.fragments;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import androidx.annotation.Nullable;

import com.evergreen.ble.advertisement.ManufactureData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Views.flowlayout.FlowLayout;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.Vo.VoColor;
import com.vithamastech.smartlight.Vo.VoLanguages;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Jaydeep on 08-01-2018.
 */

public class FragmentColorVoice extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.fragment_color_voice_textview_result)
    TextView mTextViewResult;
    @BindView(R.id.fragment_color_voice_textview_selected_language)
    TextView mTextViewSelectedLanguage;
    @BindView(R.id.fragment_color_voice_imageview_voice)
    ImageView mImageViewVoice;
    @BindView(R.id.fragment_color_voice_floating_btn_language)
    FloatingActionButton mFloatingActionButtonLanguage;

    int mIntRandomNo = 0;
    boolean mIsFromGroup = false;
    boolean mIsFromAllGroup = false;
    private final int REQ_CODE_SPEECH_INPUT = 108;
    String mStringLocalId = "";
    String mStringServerId = "";
    String[] commandNamesEnglish = {"On", "Off", "Turn on", "Turn off", "Switch on", "Switch off"};
    ArrayList<VoColor> mArrayListColor = new ArrayList<>();
    String[] mArrayLanguage;

    int mIntSelectedItemTempPosition = 0;
    ArrayList<VoLanguages> mVoLanguagesList = new ArrayList<>();
    String mStringSelectedLanguage = "en-US";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            mIntRandomNo = getArguments().getInt("intent_device_id", 0);
            mIsFromGroup = getArguments().getBoolean("intent_from_group", false);
            mIsFromAllGroup = getArguments().getBoolean("intent_from_all_group", false);
            if (!mIsFromAllGroup) {
                mStringLocalId = getArguments().getString("intent_local_id");
                mStringServerId = getArguments().getString("intent_server_id");
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_color_voice, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mSwitchCompatOnOff.setOnCheckedChangeListener(powerChange);
        getDBVoiceColorList();
        initLanguages();

        if (mActivity.mPreferenceHelper.getIsFirstTime()) {
            /*Show How to use voice control on first time app install*/
            showVoiceShowCase();
        }
        return mViewRoot;
    }

    /*Initialize Language*/
    private void initLanguages() {
        mVoLanguagesList = new ArrayList<>();
        VoLanguages mVoLanguages = new VoLanguages();
        if (mActivity.getIsSDKAbove21()) {
            mFloatingActionButtonLanguage.setVisibility(View.VISIBLE);
            mVoLanguages = new VoLanguages();

            mVoLanguages.setLanguageName("English (United States)");
            mVoLanguages.setLanguageCode("en-US");
            mVoLanguagesList.add(mVoLanguages);

            mVoLanguages = new VoLanguages();
            mVoLanguages.setLanguageName("हिन्दी (भारत)");
            mVoLanguages.setLanguageCode("hi-IN");
            mVoLanguagesList.add(mVoLanguages);

            mVoLanguages = new VoLanguages();
            mVoLanguages.setLanguageName("ગુજરાતી (ભારત)");
            mVoLanguages.setLanguageCode("gu-IN");
            mVoLanguagesList.add(mVoLanguages);

            mVoLanguages = new VoLanguages();
            mVoLanguages.setLanguageName("ಕನ್ನಡ (ಭಾರತ)");
            mVoLanguages.setLanguageCode("kn-IN");
            mVoLanguagesList.add(mVoLanguages);

            mVoLanguages = new VoLanguages();
            mVoLanguages.setLanguageName("മലയാളം (ഇന്ത്യ)");
            mVoLanguages.setLanguageCode("ml-IN");
            mVoLanguagesList.add(mVoLanguages);

            mVoLanguages = new VoLanguages();
            mVoLanguages.setLanguageName("मराठी (भारत)");
            mVoLanguages.setLanguageCode("mr-IN");
            mVoLanguagesList.add(mVoLanguages);

            mVoLanguages = new VoLanguages();
            mVoLanguages.setLanguageName("தமிழ் (இந்தியா)");
            mVoLanguages.setLanguageCode("ta-IN");
            mVoLanguagesList.add(mVoLanguages);

            mVoLanguages = new VoLanguages();
            mVoLanguages.setLanguageName("বাংলা (ভারত)");
            mVoLanguages.setLanguageCode("bn-IN");
            mVoLanguagesList.add(mVoLanguages);

            mVoLanguages = new VoLanguages();
            mVoLanguages.setLanguageName("తెలుగు (భారతదేశం)");
            mVoLanguages.setLanguageCode("te-IN");
            mVoLanguagesList.add(mVoLanguages);

        } else {
            mFloatingActionButtonLanguage.setVisibility(View.GONE);
            mVoLanguages.setLanguageName("English (United States)");
            mVoLanguages.setLanguageCode("en-US");
            mVoLanguagesList.add(mVoLanguages);
        }
        mStringSelectedLanguage = mVoLanguagesList.get(mActivity.mPreferenceHelper.getSelectedLanguage()).getLanguageCode();
        mTextViewSelectedLanguage.setText(getResources().getString(R.string.str_choose_voice_language, mVoLanguagesList.get(mActivity.mPreferenceHelper.getSelectedLanguage()).getLanguageName()));
        mArrayLanguage = new String[mVoLanguagesList.size()];
        for (int i = 0; i < mVoLanguagesList.size(); i++) {
            mArrayLanguage[i] = mVoLanguagesList.get(i).getLanguageName();
        }
    }

    /*Get All Voice color list from database*/
    private void getDBVoiceColorList() {
        DataHolder mDataHolderLight;
        mArrayListColor = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableColorVoice;
            mDataHolderLight = mActivity.mDbHelper.read(url);
            if (mDataHolderLight != null) {
                VoColor mVoColor;
                for (int i = 0; i < mDataHolderLight.get_Listholder().size(); i++) {
                    mVoColor = new VoColor();
                    mVoColor.setId(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorId));
                    mVoColor.setColor_name(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorName));
                    mVoColor.setColor_name_gujarati(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorNameGujarati));
                    mVoColor.setColor_name_bengali(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorNameBengali));
                    mVoColor.setColor_name_kannada(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorNameKannada));
                    mVoColor.setColor_name_malayalam(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorNameMalayalam));
                    mVoColor.setColor_name_marathi(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorNameMarathi));
                    mVoColor.setColor_name_telugu(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorNameTelugu));
                    mVoColor.setColor_name_hindi(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorNameHindi));
                    mVoColor.setColor_name_tamil(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorNameTamil));
                    mVoColor.setColor_rgb(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorRGB));
                    mArrayListColor.add(mVoColor);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*Convert hex data to rgb*/
    public int hex2Rgb(String colorStr) {
        return Color.rgb(Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }

    /*SHow Voice show case*/
    private void showVoiceShowCase() {
        final Dialog myDialog = new Dialog(mActivity);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.layout_my_custom_view);
        myDialog.setCancelable(true);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorSemiTransparentWhite)));

        AppCompatImageView mAppCompatImageViewClose = (AppCompatImageView) myDialog
                .findViewById(R.id.layout_showcase_voice_iv_close);
        FlowLayout mFlowLayoutCommand = myDialog.findViewById(R.id.layout_showcase_voice_flowlayout_command);
        FlowLayout mFlowLayout = myDialog.findViewById(R.id.layout_showcase_voice_flowlayout_color);
        String[] colorNames = new String[mArrayListColor.size()];
        for (int i = 0; i < mArrayListColor.size(); i++) {
            if (mStringSelectedLanguage.equals("en-US")) {
                colorNames[i] = mArrayListColor.get(i).getColor_name();
            } else if (mStringSelectedLanguage.equals("hi-IN")) {
                colorNames[i] = mArrayListColor.get(i).getColor_name_hindi();
            } else if (mStringSelectedLanguage.equals("gu-IN")) {
                colorNames[i] = mArrayListColor.get(i).getColor_name_gujarati();
            } else if (mStringSelectedLanguage.equals("kn-IN")) {
                colorNames[i] = mArrayListColor.get(i).getColor_name_kannada();
            } else if (mStringSelectedLanguage.equals("ml-IN")) {
                colorNames[i] = mArrayListColor.get(i).getColor_name_malayalam();
            } else if (mStringSelectedLanguage.equals("mr-IN")) {
                colorNames[i] = mArrayListColor.get(i).getColor_name_marathi();
            } else if (mStringSelectedLanguage.equals("ta-IN")) {
                colorNames[i] = mArrayListColor.get(i).getColor_name_tamil();
            } else if (mStringSelectedLanguage.equals("bn-IN")) {
                colorNames[i] = mArrayListColor.get(i).getColor_name_bengali();
            } else if (mStringSelectedLanguage.equals("te-IN")) {
                colorNames[i] = mArrayListColor.get(i).getColor_name_telugu();
            } else {
                colorNames[i] = mArrayListColor.get(i).getColor_name();
            }
        }
        String[] commandNames = new String[commandNamesEnglish.length];
        for (int i = 0; i < commandNames.length; i++) {
            if (mStringSelectedLanguage.equals("en-US")) {
                commandNames[i] = commandNamesEnglish[i];
            } else if (mStringSelectedLanguage.equals("hi-IN")) {
                commandNames[i] = commandNamesEnglish[i];
            } else if (mStringSelectedLanguage.equals("gu-IN")) {
                commandNames[i] = commandNamesEnglish[i];
            } else if (mStringSelectedLanguage.equals("kn-IN")) {
                commandNames[i] = commandNamesEnglish[i];
            } else if (mStringSelectedLanguage.equals("ml-IN")) {
                commandNames[i] = commandNamesEnglish[i];
            } else if (mStringSelectedLanguage.equals("mr-IN")) {
                commandNames[i] = commandNamesEnglish[i];
            } else if (mStringSelectedLanguage.equals("ta-IN")) {
                commandNames[i] = commandNamesEnglish[i];
            } else if (mStringSelectedLanguage.equals("bn-IN")) {
                commandNames[i] = commandNamesEnglish[i];
            } else if (mStringSelectedLanguage.equals("te-IN")) {
                commandNames[i] = commandNamesEnglish[i];
            } else {
                commandNames[i] = commandNamesEnglish[i];
            }
        }
        for (int i = 0; i < commandNames.length; i++) {
            if (commandNames[i] != null && !commandNames[i].equals("") && !commandNames[i].equals("null")) {
                TextView mTextView = new TextView(mActivity);
                mTextView.setText(commandNames[i]);
                mTextView
                        .setBackgroundResource(R.drawable.rounded_button);
                mTextView.setPadding(dip2px(mActivity, 10), dip2px(mActivity, 5), dip2px(mActivity, 10), dip2px(mActivity, 5));
                mTextView.setSingleLine();
                mTextView.setTextColor(getResources()
                        .getColor(R.color.colorWhite));
                mTextView
                        .setEllipsize(TextUtils.TruncateAt.END);
                FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                        FlowLayout.LayoutParams.WRAP_CONTENT,
                        FlowLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, dip2px(mActivity, 3), dip2px(mActivity, 8), dip2px(mActivity, 2));
                mTextView.setLayoutParams(params);
                mFlowLayoutCommand
                        .addView(mTextView);
                int idx = mFlowLayoutCommand
                        .indexOfChild(mTextView);
                mTextView.setTag(Integer.toString(idx));
            }
        }
        for (int i = 0; i < colorNames.length; i++) {
            if (colorNames[i] != null && !colorNames[i].equals("") && !colorNames[i].equals("null")) {
                TextView mTextView = new TextView(mActivity);
                mTextView.setText(colorNames[i]);
                mTextView
                        .setBackgroundResource(R.drawable.rounded_button);
                mTextView.setPadding(dip2px(mActivity, 10), dip2px(mActivity, 5), dip2px(mActivity, 10), dip2px(mActivity, 5));
                mTextView.setSingleLine();
                mTextView.setTextColor(getResources()
                        .getColor(R.color.colorWhite));
                mTextView
                        .setEllipsize(TextUtils.TruncateAt.END);
                FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(
                        FlowLayout.LayoutParams.WRAP_CONTENT,
                        FlowLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, dip2px(mActivity, 3), dip2px(mActivity, 8), dip2px(mActivity, 2));
                mTextView.setLayoutParams(params);
                mFlowLayout
                        .addView(mTextView);
                int idx = mFlowLayout
                        .indexOfChild(mTextView);
                mTextView.setTag(Integer.toString(idx));
            }
        }

        mAppCompatImageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                myDialog.dismiss();

            }
        });
        myDialog.show();
//        myDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Window window = myDialog.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        mActivity.mPreferenceHelper.setFirstTime(false);


    }

    /* Density to pixel convert*/
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /*pixel to Density convert*/
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /*Language Chooser*/
    @OnClick(R.id.fragment_color_voice_floating_btn_language)
    public void onVoiceLanguageButtonClick(View mView) {
        if (isAdded()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getResources().getString(R.string.str_choose_voice_input_language));
            mIntSelectedItemTempPosition = mActivity.mPreferenceHelper.getSelectedLanguage();
            builder.setCancelable(true);
            builder.setSingleChoiceItems(mArrayLanguage, mActivity.mPreferenceHelper.getSelectedLanguage(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // user checked an item
                    mIntSelectedItemTempPosition = which;
                }
            });
            builder.setPositiveButton(getResources().getString(R.string.str_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // user clicked OK
                    mStringSelectedLanguage = mVoLanguagesList.get(mIntSelectedItemTempPosition).getLanguageCode();
                    mActivity.mPreferenceHelper.setSelectedLanguage(mIntSelectedItemTempPosition);
                    mTextViewSelectedLanguage.setText(getResources().getString(R.string.str_choose_voice_language, mVoLanguagesList.get(mIntSelectedItemTempPosition).getLanguageName()));
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.str_cancel), null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @OnClick(R.id.fragment_color_voice_floating_btn_info)
    public void onVoiceInfoButtonClick(View mView) {
        if (isAdded()) {
            showVoiceShowCase();
        }
    }

    @OnClick(R.id.fragment_color_voice_imageview_voice)
    public void onVoiceButtonClick(View mView) {
        if (isAdded()) {
            promptSpeechInput();
        }
    }

    @OnClick(R.id.fragment_color_voice_floating_btn_setting)
    public void onVoiceLanguageSettingClick(View mView) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName[] components = new ComponentName[]{
                    new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.apps.gsa.settingsui.VoiceSearchPreferences"),
                    new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.voicesearch.VoiceSearchPreferences"),
                    new ComponentName("com.google.android.googlequicksearchbox", "com.google.android.apps.gsa.velvet.ui.settings.VoiceSearchPreferences")
            };
            for (ComponentName componentName : components) {
                try {
                    intent.setComponent(componentName);
                    mActivity.startActivity(intent);
                    break;
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (ActivityNotFoundException a) {
            a.printStackTrace();
            Toast.makeText(mActivity,
                    "Activity Not found",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        if (!mActivity.mUtility.haveInternet()) {
            mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection), 3, true);
            return;
        }
        if (!SpeechRecognizer.isRecognitionAvailable(mActivity)) {
            Toast.makeText(mActivity,
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        } else {
            mStringSelectedLanguage = mVoLanguagesList.get(mActivity.mPreferenceHelper.getSelectedLanguage()).getLanguageCode();
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            if (mActivity.getIsSDKAbove21()) {
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, mVoLanguagesList.get(mActivity.mPreferenceHelper.getSelectedLanguage()).getLanguageCode());
            } else {
//                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
            }
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    getString(R.string.speech_prompt));
            try {
                getActivity().startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
            } catch (ActivityNotFoundException a) {
                Toast.makeText(mActivity,
                        getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*Get Result from voice*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQ_CODE_SPEECH_INPUT) {
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String mVoiceResult = result.get(0).toString();
                    if (mVoiceResult != null && !mVoiceResult.equalsIgnoreCase("") && !mVoiceResult.equalsIgnoreCase("null")) {
                        mTextViewResult.setText(mVoiceResult);
                        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                            if (mActivity.getIsDeviceSupportedAdvertisment()) {
                                if (mStringSelectedLanguage.equals("en-US")) {
                                    if (mVoiceResult.toLowerCase().contains("of") || mVoiceResult.toLowerCase().contains("off")) {
                                        mActivity.mSwitchCompatOnOff.setChecked(false);
                                    } else if (mVoiceResult.toLowerCase().contains("on")) {
                                        mActivity.mSwitchCompatOnOff.setChecked(true);
                                    } else {
                                        try {
                                            String colorName;
                                            for (int i = 0; i < mArrayListColor.size(); i++) {
                                                if (mStringSelectedLanguage.equals("en-US")) {
                                                    colorName = mArrayListColor.get(i).getColor_name().toLowerCase();
                                                } else {
                                                    colorName = mArrayListColor.get(i).getColor_name().toLowerCase();
                                                }
                                                if (colorName != null && !colorName.equalsIgnoreCase("")) {
                                                    if (mVoiceResult.toLowerCase().contains(colorName)) {
                                                        int colorToUse = hex2Rgb(mArrayListColor.get(i).getColor_rgb());
                                                        //log the color name and color resource id
                                                        mActivity.mSwitchCompatOnOff.setChecked(true);
                                                        if (mIsFromAllGroup) {
                                                            mActivity.setLightColor(BLEUtility.intToByte(100), colorToUse, 255, Short.parseShort(0 + ""), true);
                                                        } else {
                                                            mActivity.setLightColor(BLEUtility.intToByte(100), colorToUse, 255, Short.parseShort(mIntRandomNo + ""), false);
                                                        }
                                                        if (!mIsFromGroup) {
                                                            ContentValues mContentValues = new ContentValues();
                                                            mContentValues.put(mActivity.mDbHelper.mFieldDeviceColor, colorToUse);
                                                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", new String[]{mStringLocalId});
                                                        }
                                                        break;
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {
                                    if (mVoiceResult.toLowerCase().contains("of") || mVoiceResult.toLowerCase().contains("off")) {
                                        mActivity.mSwitchCompatOnOff.setChecked(false);
                                    } else if (mVoiceResult.toLowerCase().contains("on")) {
                                        mActivity.mSwitchCompatOnOff.setChecked(true);
                                    } else {
                                        try {
                                            String colorName;
                                            for (int i = 0; i < mArrayListColor.size(); i++) {
                                                if (mStringSelectedLanguage.equals("en-US")) {
                                                    colorName = mArrayListColor.get(i).getColor_name().toLowerCase();
                                                } else if (mStringSelectedLanguage.equals("hi-IN")) {
                                                    colorName = mArrayListColor.get(i).getColor_name_hindi();
                                                } else if (mStringSelectedLanguage.equals("gu-IN")) {
                                                    colorName = mArrayListColor.get(i).getColor_name_gujarati();
                                                } else if (mStringSelectedLanguage.equals("kn-IN")) {
                                                    colorName = mArrayListColor.get(i).getColor_name_kannada();
                                                } else if (mStringSelectedLanguage.equals("ml-IN")) {
                                                    colorName = mArrayListColor.get(i).getColor_name_malayalam();
                                                } else if (mStringSelectedLanguage.equals("mr-IN")) {
                                                    colorName = mArrayListColor.get(i).getColor_name_marathi();
                                                } else if (mStringSelectedLanguage.equals("ta-IN")) {
                                                    colorName = mArrayListColor.get(i).getColor_name_tamil();
                                                } else if (mStringSelectedLanguage.equals("bn-IN")) {
                                                    colorName = mArrayListColor.get(i).getColor_name_bengali();
                                                } else if (mStringSelectedLanguage.equals("te-IN")) {
                                                    colorName = mArrayListColor.get(i).getColor_name_telugu();
                                                } else {
                                                    colorName = mArrayListColor.get(i).getColor_name().toLowerCase();
                                                }
                                                if (colorName != null && !colorName.equalsIgnoreCase("")) {
                                                    if (mVoiceResult.contains(colorName)) {
                                                        int colorToUse = hex2Rgb(mArrayListColor.get(i).getColor_rgb());
                                                        //log the color name and color resource id
                                                        mActivity.mSwitchCompatOnOff.setChecked(true);
                                                        if (mIsFromAllGroup) {
                                                            mActivity.setLightColor(BLEUtility.intToByte(100), colorToUse, 255, Short.parseShort(0 + ""), true);
                                                        } else {
                                                            mActivity.setLightColor(BLEUtility.intToByte(100), colorToUse, 255, Short.parseShort(mIntRandomNo + ""), false);
                                                        }
                                                        if (!mIsFromGroup) {
                                                            ContentValues mContentValues = new ContentValues();
                                                            mContentValues.put(mActivity.mDbHelper.mFieldDeviceColor, colorToUse);
                                                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", new String[]{mStringLocalId});
                                                        }
                                                        break;
                                                    }
                                                }
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            } else {
                                if (mVoiceResult.toLowerCase().contains("of") || mVoiceResult.toLowerCase().contains("off")) {
                                    mActivity.mSwitchCompatOnOff.setChecked(false);
                                } else if (mVoiceResult.toLowerCase().contains("on")) {
                                    mActivity.mSwitchCompatOnOff.setChecked(true);
                                } else {
                                    try {
                                        String colorName;
                                        for (int i = 0; i < mArrayListColor.size(); i++) {
                                            if (mStringSelectedLanguage.equals("en-US")) {
                                                colorName = mArrayListColor.get(i).getColor_name().toLowerCase();
                                            } else {
                                                colorName = mArrayListColor.get(i).getColor_name().toLowerCase();
                                            }
                                            if (colorName != null && !colorName.equalsIgnoreCase("")) {
                                                if (mVoiceResult.contains(colorName) || mVoiceResult.equals(colorName)) {
                                                    int colorToUse = hex2Rgb(mArrayListColor.get(i).getColor_rgb());
                                                    //log the color name and color resource id
                                                    mActivity.mSwitchCompatOnOff.setChecked(true);
                                                    if (mIsFromAllGroup) {
                                                        mActivity.setLightColor(BLEUtility.intToByte(100), colorToUse, 255, Short.parseShort(0 + ""), true);
                                                    } else {
                                                        mActivity.setLightColor(BLEUtility.intToByte(100), colorToUse, 255, Short.parseShort(mIntRandomNo + ""), false);
                                                    }
                                                    if (!mIsFromGroup) {
                                                        ContentValues mContentValues = new ContentValues();
                                                        mContentValues.put(mActivity.mDbHelper.mFieldDeviceColor, colorToUse);
                                                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", new String[]{mStringLocalId});
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } else {
                            mActivity.connectDeviceWithProgress();
                        }
                    }
                } else {
                    mTextViewResult.setText("No result found. please try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when power button is pressed.
     */
    private CompoundButton.OnCheckedChangeListener powerChange = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //    mController.setLightPower(isChecked ? PowerState.ON : PowerState.OFF);
            if (isChecked) {
                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                    ContentValues mContentValues = new ContentValues();
                    String mSwitchStatus = "ON";
                    if (mIsFromAllGroup) {
                        mActivity.mPreferenceHelper.setIsAllDeviceOn(true);
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(1), Short.parseShort(0 + ""), true);
                        mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                        String[] mArray = new String[]{mActivity.mPreferenceHelper.getUserId()};
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceUserId + "=?", mArray);
                        String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                        mActivity.mDbHelper.exeQuery(url);
                    } else {
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(1), Short.parseShort(mIntRandomNo + ""), false);
                        if (mIsFromGroup) {
                            String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupCommId + "= '" + mIntRandomNo + "'";
                            mActivity.mDbHelper.exeQuery(url);
                        } else {
                            mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                            String[] mArray = new String[]{mStringLocalId};
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArray);
                        }
                    }
                } else {
                    mActivity.connectDeviceWithProgress();
                }
            } else {
                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                    ContentValues mContentValues = new ContentValues();
                    String mSwitchStatus = "OFF";
                    if (mIsFromAllGroup) {
                        mActivity.mPreferenceHelper.setIsAllDeviceOn(false);
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(0), Short.parseShort(0 + ""), true);
                        mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                        String[] mArray = new String[]{mActivity.mPreferenceHelper.getUserId()};
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceUserId + "=?", mArray);
                        String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                        mActivity.mDbHelper.exeQuery(url);
                    } else {
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(0), Short.parseShort(mIntRandomNo + ""), false);
                        if (mIsFromGroup) {
                            String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupCommId + "= '" + mIntRandomNo + "'";
                            mActivity.mDbHelper.exeQuery(url);
                        } else {
                            mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                            String[] mArray = new String[]{mStringLocalId};
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArray);
                        }
                    }
                } else {
                    mActivity.connectDeviceWithProgress();
                }
            }
        }
    };
}
