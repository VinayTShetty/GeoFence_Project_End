package com.succorfish.installer.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoInspectionPhoto;
import com.succorfish.installer.Vo.VoInstallationPhoto;
import com.succorfish.installer.db.DataHolder;
import com.succorfish.installer.helper.InternalStorageContentProvider;
import com.succorfish.installer.helper.PreferenceHelper;
import com.succorfish.installer.helper.URLCLASS;
import com.succorfish.installer.interfaces.onFragmentBackPress;
import com.succorfish.installer.views.BottomDialog;
import com.succorfish.installer.views.TouchImageView;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 27-02-2018.
 */

public class FragmentInspectionPhoto extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.fragment_inspection_photo_recyclerView)
    RecyclerView mRecyclerViewPhoto;

    PhotoAdapter mPhotoAdapter;
    private GridLayoutManager lLayout;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    public static int CAMERA_REQUEST = 185;
    public static int CROP_REQUEST = 168;
    public static int PICTURE_TAKEN_FROM_GALLERY = 412;
    private File mFileProfileTemp;
    private String mStringPicturePath = "";
    private int mIntClickPosition = 0;
    private File mFileSDCard;
    private File mFileAppDirectory;
    private File mFileFolderDirectory;
    private String mStrDirectoryFolderName = URLCLASS.DIRECTORY_FOLDER_NAME;
    private String mStrDirectoryInstallationFolderName = URLCLASS.DIRECTORY_INSPECTION_FOLDER_NAME;
    ArrayList<VoInspectionPhoto> mArrayListPhotoList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        mArrayListPhotoList = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            VoInspectionPhoto mVoInspectionPhoto = new VoInspectionPhoto();
            mVoInspectionPhoto.setInsp_photo_local_url("");
            mVoInspectionPhoto.setInsp_photo_type(i + "");
            mVoInspectionPhoto.setIsHasImage(false);
            mArrayListPhotoList.add(mVoInspectionPhoto);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_inspection_photo, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_inspection_photo));
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mTextViewTitle.setVisibility(View.VISIBLE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
        mActivity.mTextViewDone.setVisibility(View.VISIBLE);
        mActivity.mTextViewDone.setText(getResources().getString(R.string.str_save));
        /*Get Inspection photo list from local database*/
        getDBInspectionPhotoList();

        mActivity.mTextViewDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.mUtility.hideKeyboard(mActivity);
                if (mArrayListPhotoList != null) {
//                    boolean isAnyImageUpload = false;
                    int imageCount = 0;
                    for (int i = 0; i < mArrayListPhotoList.size(); i++) {
                        if (mArrayListPhotoList.get(i).getInsp_photo_local_url() != null && !mArrayListPhotoList.get(i).getInsp_photo_local_url().equals("") && !mArrayListPhotoList.get(i).getInsp_photo_local_url().equals("null")) {
                            imageCount = imageCount + 1;
                        }
//                        if (mArrayListPhotoList.get(i).getIsModifyImage()) {
//                            isAnyImageUpload = true;
//                        }
                    }
                    if (imageCount == 4) {
                        saveInspectionPhoto();
                    } else {
                        mActivity.mUtility.errorDialog("Please add 4 photos of inspection.");
                    }
//                    if (isAnyImageUpload) {
//                        saveInspectionPhoto();
//                    } else {
//                        mActivity.onBackPressedDirect();
//                    }
                }
            }
        });

        mActivity.setOnBackFrgPress(new onFragmentBackPress() {
            @Override
            public void onFragmentBackPress(Fragment mFragment) {
                if (mFragment instanceof FragmentInspectionPhoto) {
                    System.out.println("BackKK");
                    mActivity.mUtility.hideKeyboard(mActivity);
                    mActivity.onBackPressedDirect();
//                    if (mArrayListPhotoList != null) {
//                        boolean isAnyImageUpload = false;
//                        for (int i = 0; i < mArrayListPhotoList.size(); i++) {
//                            if (mArrayListPhotoList.get(i).getIsModifyImage()) {
//                                isAnyImageUpload = true;
//                            }
//                        }
//                        if (isAnyImageUpload) {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
//                            builder.setTitle(getResources().getString(R.string.str_installation_photo_back_title));
//                            builder.setCancelable(false);
//                            builder.setMessage(getResources().getString(R.string.str_installation_photo_back_confirmation));
//                            builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    saveInspectionPhoto();
//                                }
//                            });
//                            builder.setNegativeButton(getResources().getString(R.string.str_no), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    for (int i = 0; i < mArrayListPhotoList.size(); i++) {
//                                        if (mArrayListPhotoList.get(i).getIsModifyImage()) {
//                                            if (mArrayListPhotoList.get(i).getIsHasImage()) {
//                                                try {
//                                                    File mFileDelete = new File(mArrayListPhotoList.get(i).getInsp_photo_local_url());
//                                                    if (mFileDelete != null && mFileDelete.exists()) {
//                                                        mFileDelete.delete();
//                                                    }
//                                                    mArrayListPhotoList.get(i).setInsp_photo_local_url("");
//                                                    mArrayListPhotoList.get(i).setIsHasImage(false);
//                                                    mArrayListPhotoList.get(i).setIsModifyImage(false);
//                                                } catch (Exception e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//                                        }
//                                    }
//                                    mActivity.onBackPressedDirect();
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
        return mViewRoot;
    }

    /*Get inspection photo list from local db*/
    private void getDBInspectionPhotoList() {
        DataHolder mDataHolder;
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInspectionPhoto + " where " + mActivity.mDbHelper.mFieldInspcLocalId + "= '" + mActivity.mIntInspectionId + "'";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Photo List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoInspectionPhoto mVoInspectionPhoto = new VoInspectionPhoto();
                    mVoInspectionPhoto.setInsp_photo_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoLocalID));
                    mVoInspectionPhoto.setInsp_photo_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoServerID));
                    mVoInspectionPhoto.setInsp_photo_local_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoLocalURL));
                    mVoInspectionPhoto.setInsp_photo_server_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoServerURL));
                    mVoInspectionPhoto.setInsp_photo_type(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoType));
                    mVoInspectionPhoto.setInsp_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcLocalId));
                    mVoInspectionPhoto.setInsp_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcServerId));
                    mVoInspectionPhoto.setInsp_photo_user_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoUserId));
                    mVoInspectionPhoto.setInsp_photo_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoCreatedDate));
                    mVoInspectionPhoto.setInsp_photo_update_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoUpdateDate));
                    mVoInspectionPhoto.setInsp_photo_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoIsSync));
                    if (mVoInspectionPhoto.getInsp_photo_local_url() != null && !mVoInspectionPhoto.getInsp_photo_local_url().equalsIgnoreCase("")) {
                        mVoInspectionPhoto.setIsHasImage(true);
                    } else {
                        mVoInspectionPhoto.setIsHasImage(false);
                    }
                    System.out.println("Type-" + mVoInspectionPhoto.getInsp_photo_type());
                    System.out.println("URL-" + mVoInspectionPhoto.getInsp_photo_local_url());
                    if (mVoInspectionPhoto.getInsp_photo_type() != null && !mVoInspectionPhoto.getInsp_photo_type().equalsIgnoreCase("")) {
                        mArrayListPhotoList.set(((Integer.parseInt(mVoInspectionPhoto.getInsp_photo_type())) - 1), mVoInspectionPhoto);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        lLayout = new GridLayoutManager(mActivity, 2);
        mRecyclerViewPhoto.setHasFixedSize(true);
        mRecyclerViewPhoto.setLayoutManager(lLayout);
        mPhotoAdapter = new PhotoAdapter();
        mRecyclerViewPhoto.setAdapter(mPhotoAdapter);
    }

    /* Save inspection photo*/
    private void saveInspectionPhoto() {
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < mArrayListPhotoList.size(); i++) {
            if (mArrayListPhotoList.get(i).getIsModifyImage()) {
                mArrayListPhotoList.get(i).setIsModifyImage(false);
                ContentValues mContentValues = new ContentValues();
                System.out.println("SAVEURL-" + mArrayListPhotoList.get(i).getInsp_photo_local_url());
                mContentValues.put(mActivity.mDbHelper.mFieldInspcPhotoLocalURL, mArrayListPhotoList.get(i).getInsp_photo_local_url());
                mContentValues.put(mActivity.mDbHelper.mFieldInspcPhotoType, mArrayListPhotoList.get(i).getInsp_photo_type());
                mContentValues.put(mActivity.mDbHelper.mFieldInspcLocalId, mActivity.mIntInspectionId);
                mContentValues.put(mActivity.mDbHelper.mFieldInspcPhotoUserId, PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
                mContentValues.put(mActivity.mDbHelper.mFieldInspcPhotoUpdateDate, cal.getTimeInMillis() + "");
                mContentValues.put(mActivity.mDbHelper.mFieldInspcPhotoIsSync, "0");
                String isExistInDB = CheckRecordExistInInspectionPhotoDB(String.valueOf(mActivity.mIntInspectionId), mArrayListPhotoList.get(i).getInsp_photo_type());
                if (isExistInDB.equalsIgnoreCase("-1")) {
                    mContentValues.put(mActivity.mDbHelper.mFieldInspcPhotoCreatedDate, cal.getTimeInMillis() + "");
                    int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInspectionPhoto, mContentValues);
                    if (isInsertInstall != -1) {
                        System.out.println("Inspect Photo Added In Local Db");
                    } else {
                        System.out.println("Inspect Photo Adding In Local DB");
                    }
                } else {
                    String[] mArray = new String[]{isExistInDB};
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspectionPhoto, mContentValues, mActivity.mDbHelper.mFieldInspcPhotoLocalID + "=?", mArray);
                    System.out.println("Inspect Photo updated In Local Db");
                }
            }
        }
        mActivity.onBackPressedDirect();
    }

    /*Check record exist or not*/
    public String CheckRecordExistInInspectionPhotoDB(String localInspecId, String imageType) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableInspectionPhoto + " where " + mActivity.mDbHelper.mFieldInspcLocalId + "= '" + localInspecId + "'" + " AND " + mActivity.mDbHelper.mFieldInspcPhotoType + "= '" + imageType + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" Inspec Photo List : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldInspcPhotoLocalID);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            callMarshMallowParmession();
        }
    }

    /*Check permission*/
    @TargetApi(Build.VERSION_CODES.M)
    private void callMarshMallowParmession() {
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

    public void createAllFolderDir() {
        mFileSDCard = Environment.getExternalStorageDirectory();
        mFileAppDirectory = new File(mFileSDCard.getAbsolutePath() + mStrDirectoryFolderName);
        if (!mFileAppDirectory.exists()) {
            mFileAppDirectory.mkdirs();
        }
        mFileFolderDirectory = new File(mFileAppDirectory.getAbsolutePath() + mStrDirectoryInstallationFolderName);
        if (!mFileFolderDirectory.exists()) {
            mFileFolderDirectory.mkdirs();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("----Result----");
        if (requestCode == CAMERA_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                System.out.println("mFileTemp-Camera-" + mFileProfileTemp.getPath());
                Uri selectedUri = Uri.fromFile(mFileProfileTemp);
                if (selectedUri != null) {
                    startCropActivity(selectedUri);
                }
            }
        } else if (requestCode == PICTURE_TAKEN_FROM_GALLERY) {
            if (resultCode == Activity.RESULT_OK) {
                System.out.println("----Result-2---");
                if (data != null) {
                    try {
                        final Uri selectedUri = data.getData();
                        if (selectedUri != null) {
                            System.out.println("----STARTED-2---");
                            startCropActivity(data.getData());
                        } else {
                        }
                    } catch (Exception e) {
                        System.out.println("onActivityResult Exception.." + e.toString());
                        e.printStackTrace();
                    }
                }
            }
        } else if (requestCode == CROP_REQUEST) {
            System.out.println("----Result-1---");
            if (resultCode == Activity.RESULT_OK) {
                System.out.println("----Result-1---OK---");
                if (data != null) {
                    try {
                        InputStream inputStream = mActivity.getContentResolver().openInputStream(UCrop.getOutput(data));
                        FileOutputStream fileOutputStream = new FileOutputStream(mFileProfileTemp);
                        copyStream(inputStream, fileOutputStream);
                        fileOutputStream.close();
                        inputStream.close();
                        System.out.println("mFileTemp-Gallery-" + mFileProfileTemp.getPath());
                        mStringPicturePath = mFileProfileTemp.getPath();
                        if (mStringPicturePath != null && !mStringPicturePath.equalsIgnoreCase("")) {
                            new ImageCompressionAsyncTask().execute(mStringPicturePath);
                        }
                    } catch (Exception e) {
                        System.out.println("onActivityResult Exception.." + e.toString());
                    }
                }
            } else {
                System.out.println("Image Path Not Founds");
            }
        }
    }

    /*Camera Picture*/
    private void takePicture() {
        String state = Environment.getExternalStorageState();
        String fileName = mFileFolderDirectory.getAbsolutePath();
        String filePrefixName;
        if (mIntClickPosition == 0) {
            filePrefixName = "inspect-device";
        } else if (mIntClickPosition == 1) {
            filePrefixName = "inspect-location";
        } else if (mIntClickPosition == 2) {
            filePrefixName = "inspect-power";
        } else if (mIntClickPosition == 3) {
            filePrefixName = "inspect-asset";
        } else {
            filePrefixName = "inspect-device";
        }
        String outFileName = fileName + "/" + String.format(filePrefixName + "-%d.jpg", System.currentTimeMillis());
        mFileProfileTemp = new File(outFileName);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            Uri mImageCaptureUri = null;
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mImageCaptureUri = Uri.fromFile(mFileProfileTemp);
            } else {
                mImageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("return-data", true);
            mActivity.startActivityForResult(intent, CAMERA_REQUEST);
        } catch (ActivityNotFoundException e) {

        }
    }

    /*Gallery Picture*/
    private void openGallery() {
        String state = Environment.getExternalStorageState();
        String fileName = mFileFolderDirectory.getAbsolutePath();
        String filePrefixName;
        if (mIntClickPosition == 0) {
            filePrefixName = "inspect-device";
        } else if (mIntClickPosition == 1) {
            filePrefixName = "inspect-location";
        } else if (mIntClickPosition == 2) {
            filePrefixName = "inspect-power";
        } else if (mIntClickPosition == 3) {
            filePrefixName = "inspect-asset";
        } else {
            filePrefixName = "inspect-device";
        }
        String outFileName = fileName + "/" + String.format(filePrefixName + "-%d.jpg", System.currentTimeMillis());
        mFileProfileTemp = new File(outFileName);
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        mActivity.startActivityForResult(photoPickerIntent, PICTURE_TAKEN_FROM_GALLERY);
    }

    public void copyStream(InputStream input, OutputStream output)
            throws IOException {

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    private void startCropActivity(@NonNull Uri uri) {
        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(mActivity.getCacheDir(), ".jpg")));
        uCrop = advancedConfig(uCrop);
        uCrop.start(mActivity, CROP_REQUEST);
    }

    /*Image Crop*/
    private UCrop advancedConfig(@NonNull UCrop uCrop) {
        UCrop.Options options = new UCrop.Options();
        options.setDimmedLayerColor(getResources().getColor(R.color.colorWhite));
        options.setCropGridColor(getResources().getColor(R.color.colorPrimary));
        options.setCropFrameColor(getResources().getColor(R.color.colorPrimary));
        // Color palette
        options.setToolbarColor(ContextCompat.getColor(mActivity, R.color.colorPrimary));
        options.setStatusBarColor(ContextCompat.getColor(mActivity, R.color.colorPrimaryDark));
        options.setActiveWidgetColor(ContextCompat.getColor(mActivity, R.color.colorPrimaryDark));
        options.setLogoColor(ContextCompat.getColor(mActivity, R.color.colorPrimary));
        options.setToolbarWidgetColor(ContextCompat.getColor(mActivity, R.color.colorWhite));
        return uCrop.withOptions(options);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mTextViewDone.setVisibility(View.GONE);
    }

    /*Photo Adapter*/
    public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

        @Override
        public PhotoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_installation_photo_item, parent, false);
            return new PhotoAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final PhotoAdapter.ViewHolder mViewHolder, final int position) {
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListPhotoList != null) {
                        if (position < mArrayListPhotoList.size()) {
                            createAllFolderDir();
                            mIntClickPosition = position;
                            final BottomDialog dialog;
                            if (mArrayListPhotoList.get(position).getIsHasImage()) {
                                dialog = BottomDialog.newInstance(getResources().getString(R.string.str_choose_option), getResources().getString(R.string.str_cancel), new String[]{getResources().getString(R.string.str_camera), getResources().getString(R.string.str_gallery), getResources().getString(R.string.str_view_photo)});
                            } else {
                                dialog = BottomDialog.newInstance(getResources().getString(R.string.str_choose_option), getResources().getString(R.string.str_cancel), new String[]{getResources().getString(R.string.str_camera), getResources().getString(R.string.str_gallery)});
                            }
                            dialog.show(getChildFragmentManager(), "dialog");
                            dialog.setListener(new BottomDialog.OnClickListener() {
                                @Override
                                public void click(int position) {
                                    if (position == 0) {
                                        takePicture();
                                    } else if (position == 1) {
                                        openGallery();
                                    } else if (position == 2) {
                                        zoomImageDialog(mArrayListPhotoList.get(mIntClickPosition).getInsp_photo_local_url());
                                    } else {
                                        dialog.dismiss();
                                    }
                                }
                            });
                        }
                    }
                }
            });
            if (mArrayListPhotoList.get(position).getIsHasImage()) {
                mViewHolder.mImageViewDelete.setVisibility(View.VISIBLE);
            } else {
                mViewHolder.mImageViewDelete.setVisibility(View.GONE);
            }
            if (mArrayListPhotoList.get(position).getInsp_photo_type().equalsIgnoreCase("1")) {
                mViewHolder.mTextViewImageType.setText("Device Image");
            } else if (mArrayListPhotoList.get(position).getInsp_photo_type().equalsIgnoreCase("2")) {
                mViewHolder.mTextViewImageType.setText("Location Image");
            } else if (mArrayListPhotoList.get(position).getInsp_photo_type().equalsIgnoreCase("3")) {
                mViewHolder.mTextViewImageType.setText("Power Connection");
            } else if (mArrayListPhotoList.get(position).getInsp_photo_type().equalsIgnoreCase("4")) {
                mViewHolder.mTextViewImageType.setText("Asset Image");
            } else {
                mViewHolder.mTextViewImageType.setText("Device Image");
            }

            Glide.with(FragmentInspectionPhoto.this)
                    .load(mArrayListPhotoList.get(position).getInsp_photo_local_url())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .crossFade()
                    .dontAnimate()
                    .placeholder(R.drawable.ic_add_photos_default)
                    .into(mViewHolder.mImageViewImage);
            mViewHolder.mImageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle(getResources().getString(R.string.str_remove));
                    builder.setCancelable(false);
                    builder.setMessage(getResources().getString(R.string.str_installation_photo_remove_confirmation));
                    builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (mArrayListPhotoList.get(position).getInsp_photo_local_url() != null && !mArrayListPhotoList.get(position).getInsp_photo_local_url().equals("")) {
//                                File mFile = new File(mArrayListPhotoList.get(position).getInsp_photo_local_url());
//                                if (mFile.exists()) {
//                                    mFile.delete();
                                mArrayListPhotoList.get(position).setInsp_photo_local_url("");
                                mArrayListPhotoList.get(position).setIsHasImage(false);
                                mArrayListPhotoList.get(position).setIsModifyImage(true);
                                notifyDataSetChanged();
//                                }
                            }
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.str_no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListPhotoList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_installation_photo_item_imageview_delete)
            ImageView mImageViewDelete;
            @BindView(R.id.raw_installation_photo_item_imageview_image)
            ImageView mImageViewImage;
            @BindView(R.id.raw_installation_photo_item_textview_image_type)
            TextView mTextViewImageType;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    /*Image Display Dialog*/
    public void zoomImageDialog(String mStringImagePath) {
        final Dialog myDialog = new Dialog(mActivity);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.popup_imgeview_view);
        myDialog.setCancelable(false);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorSemiTransparent)));

        ImageView mImageViewClose = (ImageView) myDialog
                .findViewById(R.id.popup_imageview_view_imageview_close);
        TouchImageView mImageViewPhoto = (TouchImageView) myDialog
                .findViewById(R.id.popup_imageview_view_imageview_image);
        System.out.println("mStringImagePath-" + mStringImagePath);
        Glide.with(FragmentInspectionPhoto.this)
                .load(mStringImagePath)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .crossFade()
                .dontAnimate()
                .placeholder(R.drawable.ic_add_photos_default)
                .into(mImageViewPhoto);
//        ImageMatrixTouchHandler imageMatrixTouchHandler = new ImageMatrixTouchHandler(mActivity);
//        mImageViewPhoto.setOnTouchListener(imageMatrixTouchHandler);
        mImageViewClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        myDialog.show();

        Window window = myDialog.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    }

    /*Compress Image*/
    class ImageCompressionAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            mStringPicturePath = compressImage(params[0]);
            return mStringPicturePath;
        }

        public String compressImage(String filePath) {
            Bitmap scaledBitmap = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            int actualHeight = 0;
            int actualWidth = 0;
            Bitmap bmp = null;
            try {
                options.inJustDecodeBounds = true;
                bmp = BitmapFactory.decodeFile(filePath, options);

                actualHeight = options.outHeight;
                actualWidth = options.outWidth;
                float maxHeight = 816.0f;
                float maxWidth = 612.0f;
                float imgRatio = actualWidth / actualHeight;
                float maxRatio = maxWidth / maxHeight;

                if (actualHeight > maxHeight || actualWidth > maxWidth) {
                    if (imgRatio < maxRatio) {
                        imgRatio = maxHeight / actualHeight;
                        actualWidth = (int) (imgRatio * actualWidth);
                        actualHeight = (int) maxHeight;
                    } else if (imgRatio > maxRatio) {
                        imgRatio = maxWidth / actualWidth;
                        actualHeight = (int) (imgRatio * actualHeight);
                        actualWidth = (int) maxWidth;
                    } else {
                        actualHeight = (int) maxHeight;
                        actualWidth = (int) maxWidth;

                    }
                }
            } catch (Exception e) {

            }
            options.inSampleSize = mActivity.mUtility.calculateInSampleSize(options, actualWidth, actualHeight);
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];
            try {
                bmp = BitmapFactory.decodeFile(filePath, options);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();

            }
            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            } catch (Exception e) {

            }
            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);
            try {
                Canvas canvas = new Canvas(scaledBitmap);
                canvas.setMatrix(scaleMatrix);
                canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));
            } catch (Exception e) {
            }


            ExifInterface exif;
            try {
                exif = new ExifInterface(filePath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                    Log.d("EXIF", "Exif: " + orientation);
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            FileOutputStream out = null;
            String filename = mStringPicturePath;
            System.out.println("CompressImage-" + filename);
            try {
                out = new FileOutputStream(filename);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return filename;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                mStringPicturePath = result;
                System.out.println("onPostExecute-" + mStringPicturePath);
                mArrayListPhotoList.get(mIntClickPosition).setInsp_photo_local_url(mStringPicturePath);
                mArrayListPhotoList.get(mIntClickPosition).setIsHasImage(true);
                mArrayListPhotoList.get(mIntClickPosition).setIsModifyImage(true);
                if (mPhotoAdapter != null) {
                    mPhotoAdapter.notifyDataSetChanged();
                }
            }
        }
    }

}
