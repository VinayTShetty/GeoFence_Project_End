package com.succorfish.installer.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.R;
import com.succorfish.installer.helper.URLCLASS;
import com.succorfish.installer.interfaces.onBackPressWithAction;
import com.succorfish.installer.interfaces.onFragmentBackPress;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 23-03-2018.
 */

public class FragmentSignature extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.fragment_signature_imageview_owner_signature)
    ImageView mImageViewOwnerSign;
    @BindView(R.id.fragment_signature_imageview_installer_signature)
    ImageView mImageViewInstallerSign;
    @BindView(R.id.fragment_signature_relativelayout_owner_sign)
    RelativeLayout mRelativeLayoutOwnerSign;
    @BindView(R.id.fragment_signature_relativelayout_installer_sign)
    RelativeLayout mRelativeLayoutInstallerSign;
    @BindView(R.id.fragment_signature_checkbox_owner_terms_and_condition)
    CheckBox mCheckBoxOwnerTermsAndCondition;
    @BindView(R.id.fragment_signature_checkbox_installer_terms_and_condition)
    CheckBox mCheckBoxInstallerTermsAndCondition;
    @BindView(R.id.fragment_signature_txt_installer_signature)
    TextView mTextViewInstallerSign;
    onBackPressWithAction mOnBackPressWithAction;
    String mStringOwnerSignatureImagePath = "";
    String mStringInstallerSignatureImagePath = "";

    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private File mFileSDCard;
    private File mFileAppDirectory;
    private File mFileInstallerDirectory;
    private File mFileSignatureDirectory;
    private String mStrDirectoryFolderName = URLCLASS.DIRECTORY_FOLDER_NAME;
    private String mStrDirectoryInstallationFolderName = URLCLASS.DIRECTORY_INSTALLATION_FOLDER_NAME;
    private String mStrDirectorySignatureFolderName = URLCLASS.DIRECTORY_SIGNATURE;
    private long mLastClickTime = 0;
    private Dialog mDialogSignature;

    private File mFileOwnerSignaturePath;
    private File mFileInstallerSignaturePath;
    private boolean isOwnerImageChange = false;
    private boolean isInstallerImageChange = false;
    private int isFromInstall = 1;

    public FragmentSignature() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            mStringOwnerSignatureImagePath = getArguments().getString("intent_owner_signature");
            mStringInstallerSignatureImagePath = getArguments().getString("intent_installer_signature");
            isFromInstall = getArguments().getInt("intent_is_from");
        }
        if (isFromInstall == 1) {
            mStrDirectoryInstallationFolderName = URLCLASS.DIRECTORY_INSTALLATION_FOLDER_NAME;
        } else if (isFromInstall == 2) {
            mStrDirectoryInstallationFolderName = URLCLASS.DIRECTORY_UNINSTALL_FOLDER_NAME;
        } else {
            mStrDirectoryInstallationFolderName = URLCLASS.DIRECTORY_INSPECTION_FOLDER_NAME;
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        System.out.println("mStringOwnerSignatureImagePath-" + mStringOwnerSignatureImagePath);
        System.out.println("mStringInstallerSignatureImagePath-" + mStringInstallerSignatureImagePath);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_signature, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_frg_three_sign));
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mTextViewTitle.setVisibility(View.VISIBLE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
        mActivity.mTextViewDone.setVisibility(View.VISIBLE);
        mActivity.mTextViewDone.setText(getResources().getString(R.string.str_save));
        if (isAdded()) {
            if (isFromInstall == 1) {
                mTextViewInstallerSign.setText(getResources().getString(R.string.str_frg_three_sign_here_installer));
                mCheckBoxOwnerTermsAndCondition.setText(getResources().getString(R.string.str_frg_three_terms_and_condition));
            } else if (isFromInstall == 2) {
                mTextViewInstallerSign.setText(getResources().getString(R.string.str_frg_three_sign_here_uninstaller));
                mCheckBoxOwnerTermsAndCondition.setText(getResources().getString(R.string.str_uninstall_terms_and_condition));
            } else {
                mTextViewInstallerSign.setText(getResources().getString(R.string.str_frg_three_sign_here_inspector));
                mCheckBoxOwnerTermsAndCondition.setText(getResources().getString(R.string.str_frg_three_inspection_terms_and_condition));
            }

        }
        if (mStringOwnerSignatureImagePath != null && !mStringOwnerSignatureImagePath.equalsIgnoreCase("") && !mStringOwnerSignatureImagePath.equalsIgnoreCase("null")) {
            mFileOwnerSignaturePath = new File(mStringOwnerSignatureImagePath);
            if (mFileOwnerSignaturePath != null && mFileOwnerSignaturePath.exists()) {
                mCheckBoxOwnerTermsAndCondition.setChecked(true);
                Glide.with(FragmentSignature.this)
                        .load(mFileOwnerSignaturePath)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .crossFade()
                        .dontAnimate()
                        .into(mImageViewOwnerSign);
            } else {
                mStringOwnerSignatureImagePath = "";
            }
        }
        if (mStringInstallerSignatureImagePath != null && !mStringInstallerSignatureImagePath.equalsIgnoreCase("") && !mStringInstallerSignatureImagePath.equalsIgnoreCase("null")) {
            mFileInstallerSignaturePath = new File(mStringInstallerSignatureImagePath);
            if (mFileInstallerSignaturePath != null && mFileInstallerSignaturePath.exists()) {
                mCheckBoxInstallerTermsAndCondition.setChecked(true);
                Glide.with(FragmentSignature.this)
                        .load(mFileInstallerSignaturePath)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .crossFade()
                        .dontAnimate()
                        .into(mImageViewInstallerSign);
            } else {
                mStringInstallerSignatureImagePath = "";
            }
        }

        mActivity.mTextViewDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.mUtility.hideKeyboard(mActivity);
                if (mStringOwnerSignatureImagePath == null || mStringOwnerSignatureImagePath.equalsIgnoreCase("") || mStringOwnerSignatureImagePath.equalsIgnoreCase("null")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_draw_sign));
                    return;
                }
                if (!mCheckBoxOwnerTermsAndCondition.isChecked()) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_accept_terms_and_condition));
                    return;
                }
                if (mStringInstallerSignatureImagePath == null || mStringInstallerSignatureImagePath.equalsIgnoreCase("") || mStringOwnerSignatureImagePath.equalsIgnoreCase("null")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_ask_installer_sign));
                    return;
                }
                if (!mCheckBoxInstallerTermsAndCondition.isChecked()) {

                    if (isFromInstall == 1) {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_accept_install_terms_and_condition));
                    } else if (isFromInstall == 2) {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_accept_uninstaller_terms_and_condition));
                    } else {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_accept_inspection_terms_and_condition));
                    }
                    return;
                }
                saveSignaturePhoto();

//                if (mArrayListPhotoList != null) {
//                    boolean isAnyImageUpload = false;
//                    for (int i = 0; i < mArrayListPhotoList.size(); i++) {
//                        if (mArrayListPhotoList.get(i).getIsModifyImage()) {
//                            isAnyImageUpload = true;
//                        }
//                    }
//                    if (isAnyImageUpload) {
//                        saveInstallationPhoto();
//                    } else {
//                        mActivity.onBackPressedDirect();
//                    }
//                }
            }
        });
        mActivity.setOnBackFrgPress(new onFragmentBackPress() {
            @Override
            public void onFragmentBackPress(Fragment mFragment) {
                if (mFragment instanceof FragmentSignature) {
                    System.out.println("BackKK");
                    mActivity.mUtility.hideKeyboard(mActivity);
                    if (isOwnerImageChange || isInstallerImageChange) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                        builder.setTitle(getResources().getString(R.string.str_signature_back_title));
                        builder.setCancelable(false);
                        builder.setMessage(getResources().getString(R.string.str_signature_back_confirmation));
                        builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (mStringOwnerSignatureImagePath == null || mStringOwnerSignatureImagePath.equalsIgnoreCase("") || mStringOwnerSignatureImagePath.equalsIgnoreCase("null")) {
                                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_draw_sign));
                                    return;
                                }
                                if (!mCheckBoxOwnerTermsAndCondition.isChecked()) {
                                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_accept_terms_and_condition));
                                    return;
                                }
                                if (mStringInstallerSignatureImagePath == null || mStringInstallerSignatureImagePath.equalsIgnoreCase("") || mStringOwnerSignatureImagePath.equalsIgnoreCase("null")) {
                                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_ask_installer_sign));
                                    return;
                                }
                                if (!mCheckBoxInstallerTermsAndCondition.isChecked()) {
                                    if (isFromInstall == 1) {
                                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_accept_install_terms_and_condition));
                                    } else if (isFromInstall == 2) {
                                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_accept_uninstaller_terms_and_condition));
                                    } else {
                                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_accept_inspection_terms_and_condition));
                                    }
                                    return;
                                }
                                saveSignaturePhoto();
                            }
                        });
                        builder.setNegativeButton(getResources().getString(R.string.str_no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                if (isOwnerImageChange) {
                                    try {
                                        if (mFileOwnerSignaturePath != null && mFileOwnerSignaturePath.exists()) {
                                            mFileOwnerSignaturePath.delete();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (isInstallerImageChange) {
                                    try {
                                        if (mFileInstallerSignaturePath != null && mFileInstallerSignaturePath.exists()) {
                                            mFileInstallerSignaturePath.delete();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                mActivity.onBackPressedDirect();
                            }
                        });
                        builder.show();
                    } else {
                        mActivity.onBackPressedDirect();
                    }
                }
            }
        });
        return mViewRoot;
    }

    /*Save Signature photo*/
    private void saveSignaturePhoto() {
        Calendar cal = Calendar.getInstance();
        ContentValues mContentValues = new ContentValues();
        if (isFromInstall == 1) {
            mContentValues.put(mActivity.mDbHelper.mFieldInstallLocalSignUrl, mStringOwnerSignatureImagePath);
            mContentValues.put(mActivity.mDbHelper.mFieldInstallLocalInstallerSignUrl, mStringInstallerSignatureImagePath);
            mContentValues.put(mActivity.mDbHelper.mFieldInstallUpdatedDate, cal.getTimeInMillis() + "");
            String[] mArray = new String[]{String.valueOf(mActivity.mIntInstallationId)};
            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValues, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
        } else if (isFromInstall == 2) {
            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallLocalSignUrl, mStringOwnerSignatureImagePath);
            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallLocalUninstallerSignUrl, mStringInstallerSignatureImagePath);
            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallUpdatedDate, cal.getTimeInMillis() + "");
            String[] mArray = new String[]{String.valueOf(mActivity.mIntUnInstallationId)};
            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableUnInstall, mContentValues, mActivity.mDbHelper.mFieldUnInstallLocalId + "=?", mArray);
        } else {
            mContentValues.put(mActivity.mDbHelper.mFieldInspectionLocalSignUrl, mStringOwnerSignatureImagePath);
            mContentValues.put(mActivity.mDbHelper.mFieldInspectionLocalInspectorSignUrl, mStringInstallerSignatureImagePath);
            mContentValues.put(mActivity.mDbHelper.mFieldInspectionUpdatedDate, cal.getTimeInMillis() + "");
            String[] mArray = new String[]{String.valueOf(mActivity.mIntInspectionId)};
            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspection, mContentValues, mActivity.mDbHelper.mFieldInspectionLocalId + "=?", mArray);
        }

        System.out.println("Signature updated In Local Db");
        if (mOnBackPressWithAction != null) {
            mOnBackPressWithAction.onBackWithAction(mStringOwnerSignatureImagePath, mStringInstallerSignatureImagePath);
        }
        mActivity.onBackPressedDirect();
    }

    /*Ask owner Signature*/
    @OnClick(R.id.fragment_signature_relativelayout_owner_sign)
    public void onOwnerSignatureClick(View mView) {
        if (isAdded()) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            if (Build.VERSION.SDK_INT >= 23) {
                checkMarshMallowPermission();
            }
            if (Build.VERSION.SDK_INT >= 23) {
                if (hasPermissions(mActivity, new String[]{
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE})) {
                    createAllFolderDir();
                    selectSignatureDialog(true);
                }
            } else {
                createAllFolderDir();
                selectSignatureDialog(true);
            }
        }
    }

    @OnClick(R.id.fragment_signature_relativelayout_installer_sign)
    public void onInstallerSignatureClick(View mView) {
        if (isAdded()) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            if (Build.VERSION.SDK_INT >= 23) {
                checkMarshMallowPermission();
            }
            if (Build.VERSION.SDK_INT >= 23) {
                if (hasPermissions(mActivity, new String[]{
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE})) {
                    createAllFolderDir();
                    selectSignatureDialog(false);
                }
            } else {
                createAllFolderDir();
                selectSignatureDialog(false);
            }
        }
    }

    /*Singnature Dialog*/
    public void selectSignatureDialog(final boolean isOwnerSignature) {
        if (mDialogSignature != null && mDialogSignature.isShowing()) {
            return;
        } else {
            mDialogSignature = new Dialog(mActivity);
            mDialogSignature.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mDialogSignature.setContentView(R.layout.popup_signature);
            mDialogSignature.setCancelable(false);
            mDialogSignature.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorSemiTransparent)));

            TextView mTextViewClear = (TextView) mDialogSignature
                    .findViewById(R.id.popup_signature_textview_header_clear);
            TextView mTextViewCancel = (TextView) mDialogSignature
                    .findViewById(R.id.popup_signature_textview_header_cancel);
            TextView mTextViewSave = (TextView) mDialogSignature
                    .findViewById(R.id.popup_signature_textview_bottom_save);
            final SignaturePad mSignaturePad = (SignaturePad) mDialogSignature
                    .findViewById(R.id.popup_signature_signature_pad);
            mTextViewClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSignaturePad.clear();
                }
            });
            mTextViewCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDialogSignature.isShowing()) {
                        mDialogSignature.dismiss();
                    }
                }
            });
            mTextViewSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mSignaturePad.isEmpty()) {
                        if (mDialogSignature.isShowing()) {
                            mDialogSignature.dismiss();
                        }
                        Bitmap signatureBitmap = mSignaturePad.getSignatureBitmap();
                        if (isOwnerSignature) {
                            mImageViewOwnerSign.setImageBitmap(signatureBitmap);
                        } else {
                            mImageViewInstallerSign.setImageBitmap(signatureBitmap);
                        }
                        if (addJpgSignatureToGallery(signatureBitmap, isOwnerSignature)) {
//                        Toast.makeText(mActivity, "Signature saved into the Gallery", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(mActivity, "Unable to store the signature. Please retry", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mActivity.mUtility.errorDialog("Please draw signature.");
                    }
                }
            });
            mSignaturePad.setOnSignedListener(new SignaturePad.OnSignedListener() {
                @Override
                public void onStartSigning() {
                }

                @Override
                public void onSigned() {

                }

                @Override
                public void onClear() {
                }
            });
            mDialogSignature.show();
            Window window = mDialogSignature.getWindow();
            window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    public boolean addJpgSignatureToGallery(Bitmap signature, boolean isOwnerSignature) {
        boolean result = false;
        try {
            String fileName = mFileSignatureDirectory.getAbsolutePath();
            String outFileName = "";
            if (isOwnerSignature) {
                if (isFromInstall == 1) {
                    outFileName = fileName + "/" + String.format("install-ownsign" + "-%d.jpg", System.currentTimeMillis());
                } else if (isFromInstall == 2) {
                    outFileName = fileName + "/" + String.format("uninstall-ownsign" + "-%d.jpg", System.currentTimeMillis());
                } else {
                    outFileName = fileName + "/" + String.format("inspect-ownsign" + "-%d.jpg", System.currentTimeMillis());
                }
                mFileOwnerSignaturePath = new File(outFileName);
                mStringOwnerSignatureImagePath = mFileOwnerSignaturePath.getPath();
                saveBitmapToJPG(signature, mFileOwnerSignaturePath);
                scanMediaFile(mFileOwnerSignaturePath);
                isOwnerImageChange = true;
            } else {
                if (isFromInstall == 1) {
                    outFileName = fileName + "/" + String.format("install-instsign" + "-%d.jpg", System.currentTimeMillis());
                } else if (isFromInstall == 2) {
                    outFileName = fileName + "/" + String.format("uninstall-instsign" + "-%d.jpg", System.currentTimeMillis());
                } else {
                    outFileName = fileName + "/" + String.format("inspect-instsign" + "-%d.jpg", System.currentTimeMillis());
                }

                mFileInstallerSignaturePath = new File(outFileName);
                mStringInstallerSignatureImagePath = mFileInstallerSignaturePath.getPath();
                saveBitmapToJPG(signature, mFileInstallerSignaturePath);
                scanMediaFile(mFileInstallerSignaturePath);
                isInstallerImageChange = true;
            }
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void saveBitmapToJPG(Bitmap bitmap, File photo) throws IOException {
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(photo);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        stream.close();
    }

    private void scanMediaFile(File photo) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        mActivity.sendBroadcast(mediaScanIntent);
    }

    public void createAllFolderDir() {
        mFileSDCard = Environment.getExternalStorageDirectory();
        mFileAppDirectory = new File(mFileSDCard.getAbsolutePath() + mStrDirectoryFolderName);
        if (!mFileAppDirectory.exists()) {
            mFileAppDirectory.mkdirs();
        }
        mFileInstallerDirectory = new File(mFileAppDirectory.getAbsolutePath() + mStrDirectoryInstallationFolderName);
        if (!mFileInstallerDirectory.exists()) {
            mFileInstallerDirectory.mkdirs();
        }
        mFileSignatureDirectory = new File(mFileInstallerDirectory.getAbsolutePath() + mStrDirectorySignatureFolderName);
        if (!mFileSignatureDirectory.exists()) {
            mFileSignatureDirectory.mkdirs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    // Permission Denied
                }
                if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    // Permission Denied#858585
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkMarshMallowPermission() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write External Storage");
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read External Storage");
        if (permissionsList.size() > 0) {
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {

        if (mActivity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        boolean hasAllPermissions = true;
        for (String permission : permissions) {
            //you can return false instead of assigning, but by assigning you can log all permission values
            if (!hasPermission(context, permission)) {
                hasAllPermissions = false;
            }
        }
        return hasAllPermissions;
    }

    public static boolean hasPermission(Context context, String permission) {
        int res = context.checkCallingOrSelfPermission(permission);
        return res == PackageManager.PERMISSION_GRANTED;
    }

    public void setOnScanResultSet(onBackPressWithAction mScanResultSet) {
        mOnBackPressWithAction = mScanResultSet;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mUtility.hideKeyboard(mActivity);
        mActivity.mTextViewDone.setVisibility(View.GONE);
    }

}
