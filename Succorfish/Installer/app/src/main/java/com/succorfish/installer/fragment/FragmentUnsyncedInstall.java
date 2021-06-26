package com.succorfish.installer.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoInstallation;
import com.succorfish.installer.Vo.VoInstallationPhoto;
import com.succorfish.installer.Vo.VoQuestion;
import com.succorfish.installer.Vo.VoQuestionAns;
import com.succorfish.installer.Vo.VoVessel;
import com.succorfish.installer.db.DataHolder;
import com.succorfish.installer.helper.PreferenceHelper;
import com.succorfish.installer.helper.Utility;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 27-02-2018.
 */

public class FragmentUnsyncedInstall extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.fragment_unsynced_install_recyclerView)
    RecyclerView mRecyclerViewInstallation;
    @BindView(R.id.fragment_unsynced_install_textview_no_list)
    TextView mTextViewNoListFound;
    public ArrayList<VoInstallation> mArrayListInstallations = new ArrayList<>();
    InstallationAdapter mInstallationAdapter;
    private SimpleDateFormat mSimpleDateFormatDateDisplay;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_unsynced_install, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mSimpleDateFormatDateDisplay = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm", Locale.getDefault());

        if (isAdded()) {
            mTextViewNoListFound.setText(getResources().getString(R.string.str_unsynced_install_no_item));
        }
        /*Get unsync Installation list*/
        getDBInstallationList();
        return mViewRoot;
    }

    /*Get unsync Installation list from db*/
    private void getDBInstallationList() {
        DataHolder mDataHolder;
        mArrayListInstallations = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInstall + " where " + mActivity.mDbHelper.mFieldInstallIsSync + "= 0" + " AND " + mActivity.mDbHelper.mFieldInstallUserId + "= '" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId() + "'" + " order by " + mActivity.mDbHelper.mFieldInstallLocalId + " DESC";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoInstallation mVoInstallation = new VoInstallation();
                    mVoInstallation.setInst_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLocalId));
                    mVoInstallation.setInst_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallServerId));
                    mVoInstallation.setInst_user_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallUserId));
                    mVoInstallation.setInst_device_imei_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceIMEINo));
                    mVoInstallation.setInst_device_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceServerId));
                    mVoInstallation.setInst_device_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceLocalId));
                    mVoInstallation.setInst_device_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDevicName));
                    mVoInstallation.setInst_device_warranty_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceWarranty_status));
                    mVoInstallation.setInst_device_type_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceTypeName));
                    mVoInstallation.setInst_help_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallHelpNo));
                    mVoInstallation.setInst_date_time(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDateTime));
                    mVoInstallation.setInst_latitude(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLatitude));
                    mVoInstallation.setInst_longitude(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLongitude));
                    mVoInstallation.setInst_country_code(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallCountryCode));
                    mVoInstallation.setInst_country_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallCountryName));
                    mVoInstallation.setInst_vessel_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallVesselLocalId));
                    mVoInstallation.setInst_vessel_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallVesselServerId));
                    mVoInstallation.setInst_vessel_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallVesselName));
                    mVoInstallation.setInst_vessel_regi_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallVesselRegNo));
                    mVoInstallation.setInst_power(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallPower));
                    mVoInstallation.setInst_location(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLocation));
                    mVoInstallation.setInst_owner_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerName));
                    mVoInstallation.setInst_owner_address(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerAddress));
                    mVoInstallation.setInst_owner_city(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerCity));
                    mVoInstallation.setInst_owner_state(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerState));
                    mVoInstallation.setInst_owner_zipcode(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerZipcode));
                    mVoInstallation.setInst_owner_email(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerEmail));
                    mVoInstallation.setInst_owner_mobile_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerMobileNo));
                    mVoInstallation.setInst_local_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLocalSignUrl));
                    mVoInstallation.setInst_server_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallServerSignUrl));
                    mVoInstallation.setInst_local_installer_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLocalInstallerSignUrl));
                    mVoInstallation.setInst_server_installer_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallServerInstallerSignUrl));
                    mVoInstallation.setInst_pdf_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallPdfUrl));
                    mVoInstallation.setInst_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallCreatedDate));
                    mVoInstallation.setInst_updated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallUpdatedDate));
                    mVoInstallation.setInst_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallIsSync));
                    mVoInstallation.setIs_install(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallStatus));
                    mVoInstallation.setInst_date_timestamp(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDateTimeStamp));
                    mArrayListInstallations.add(mVoInstallation);
                }
            }
//                Collections.sort(mArrayListTreatmentLists, new Comparator<TreatmentList>() {
//                    @Override
//                    public int compare(TreatmentList s1, TreatmentList s2) {
//                        return s1.getTreatment_title().compareToIgnoreCase(s1.getTreatment_title());
//                    }
//                });
        } catch (Exception e) {
            e.printStackTrace();
        }
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mInstallationAdapter = new InstallationAdapter();
        mRecyclerViewInstallation.setLayoutManager(mLayoutManager);
        mRecyclerViewInstallation.setAdapter(mInstallationAdapter);
        mInstallationAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });
        checkAdapterIsEmpty();
    }

    private void checkAdapterIsEmpty() {
        if (mInstallationAdapter.getItemCount() == 0) {
            mTextViewNoListFound.setVisibility(View.VISIBLE);
            mRecyclerViewInstallation.setVisibility(View.GONE);
        } else {
            mTextViewNoListFound.setVisibility(View.GONE);
            mRecyclerViewInstallation.setVisibility(View.VISIBLE);
        }
    }

    /*Install list adapter*/
    public class InstallationAdapter extends RecyclerView.Adapter<InstallationAdapter.ViewHolder> {

        @Override
        public InstallationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_unsync_install_list, parent, false);
            return new InstallationAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final InstallationAdapter.ViewHolder mViewHolder, final int position) {
            if (mArrayListInstallations.get(position).getInst_device_imei_no() != null && !mArrayListInstallations.get(position).getInst_device_imei_no().equalsIgnoreCase("")) {
                mViewHolder.mTextViewIMEINo.setText(mArrayListInstallations.get(position).getInst_device_imei_no());
            } else {
                mViewHolder.mTextViewIMEINo.setText("-NA-");
            }
            if (mArrayListInstallations.get(position).getIs_install() != null && !mArrayListInstallations.get(position).getIs_install().equalsIgnoreCase("")) {
                if (mArrayListInstallations.get(position).getIs_install().equalsIgnoreCase("0")) {
                    mViewHolder.mTextViewStatus.setVisibility(View.VISIBLE);
                } else {
                    mViewHolder.mTextViewStatus.setVisibility(View.GONE);
                }
            } else {
                mViewHolder.mTextViewStatus.setVisibility(View.GONE);
            }
            if (mArrayListInstallations.get(position).getInst_vessel_name() != null && !mArrayListInstallations.get(position).getInst_vessel_name().equalsIgnoreCase("")) {
                mViewHolder.mTextViewVesselName.setText(mArrayListInstallations.get(position).getInst_vessel_name());
            } else {
                mViewHolder.mTextViewVesselName.setText("-NA-");
            }
            if (mArrayListInstallations.get(position).getInst_date_time() != null && !mArrayListInstallations.get(position).getInst_date_time().equalsIgnoreCase("") && !mArrayListInstallations.get(position).getInst_date_time().equalsIgnoreCase("null")) {
                try {
                    Calendar mCalendar = Calendar.getInstance();
                    mCalendar.setTimeInMillis(Long.parseLong(mArrayListInstallations.get(position).getInst_date_time()));
                    mViewHolder.mTextViewInstallationDate.setText(mSimpleDateFormatDateDisplay.format(mCalendar.getTime()));
                } catch (Exception e) {
                    mViewHolder.mTextViewInstallationDate.setText("-NA-");
                    e.printStackTrace();
                }
            } else {
                mViewHolder.mTextViewInstallationDate.setText("-NA-");
            }
            if (mArrayListInstallations.get(position).getInst_vessel_regi_no() != null && !mArrayListInstallations.get(position).getInst_vessel_regi_no().equalsIgnoreCase("")) {
                mViewHolder.mTextViewVesselRegNo.setText(mArrayListInstallations.get(position).getInst_vessel_regi_no());
            } else {
                mViewHolder.mTextViewVesselRegNo.setText("-NA-");
            }
            if (mArrayListInstallations.get(position).getInst_device_type_name() != null && !mArrayListInstallations.get(position).getInst_device_type_name().equalsIgnoreCase("")) {
                mViewHolder.mTextViewType.setText(mArrayListInstallations.get(position).getInst_device_type_name());
            } else {
                mViewHolder.mTextViewType.setText("-NA-");
            }
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListInstallations != null) {
                        if (position < mArrayListInstallations.size()) {
                            mActivity.mInstallUnInstallInspectStatus = 0;
                            mActivity.mIntInstallationId = Integer.parseInt(mArrayListInstallations.get(position).getInst_local_id());
                            Gson gson = new Gson();
                            VoQuestion mVoQuestion = gson.fromJson(Utility.fetchQuestionnaireJson(mActivity, "questions_list.json"), VoQuestion.class);
                            mActivity.mListQuestion = mVoQuestion.getQuestions();

                            List<VoQuestionAns> mListQuestionTemp = new ArrayList<>();
                            DataHolder mDataHolder;
                            try {
                                String url = "select * from " + mActivity.mDbHelper.mTableQuestionAnswer + " where " + mActivity.mDbHelper.mFieldQuesAnsInsUninsInspType + "= 0" + " AND " + mActivity.mDbHelper.mFieldQuesAnsInsUninsInspLocalID + "= '" + mActivity.mIntInstallationId + "'";
                                System.out.println("Local url " + url);
                                mDataHolder = mActivity.mDbHelper.read(url);
                                TypeToken<List<VoQuestionAns>> token = new TypeToken<List<VoQuestionAns>>() {
                                };
                                if (mDataHolder != null) {
                                    System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                                    for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                                        mListQuestionTemp = gson.fromJson(mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldQuesAnsText), token.getType());
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            for (int i = 0; i < mListQuestionTemp.size(); i++) {
                                if (mActivity.mListQuestion.get(i).getQuestionNo() == mListQuestionTemp.get(i).getQuestionNo()) {
                                    mActivity.mListQuestion.get(i).setChooseAns(mListQuestionTemp.get(i).getChooseAns());
                                    mActivity.mListQuestion.get(i).setAnsComment(mListQuestionTemp.get(i).getAnsComment());
                                }
                            }
                            Bundle mBundle = new Bundle();
                            mBundle.putBoolean("mIntent_is_from_un_sync", true);
                            mActivity.replacesFragment(new FragmentHealthNsafety(), true, "Finish_Back", mBundle, 1);
//                            mActivity.replacesFragment(new FragmentNewInstallation(), true, null, 1);
                        }
                    }
                }
            });

            mViewHolder.mImageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListInstallations != null) {
                        if (position < mArrayListInstallations.size()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                            builder.setTitle(getResources().getString(R.string.str_remove));
                            builder.setCancelable(false);
                            builder.setMessage(getResources().getString(R.string.str_unsynced_install_remove_confirmation));
                            builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    try {
                                        if (mArrayListInstallations.get(position).getInst_local_sign_url() != null && !mArrayListInstallations.get(position).getInst_local_sign_url().equals("") && !mArrayListInstallations.get(position).getInst_local_sign_url().equals("null")) {
                                            File mFileDelete = new File(mArrayListInstallations.get(position).getInst_local_sign_url());
                                            if (mFileDelete != null && mFileDelete.exists()) {
                                                mFileDelete.delete();
                                            }
                                        }
                                        if (mArrayListInstallations.get(position).getInst_local_installer_sign_url() != null && !mArrayListInstallations.get(position).getInst_local_installer_sign_url().equals("") && !mArrayListInstallations.get(position).getInst_local_installer_sign_url().equals("null")) {
                                            File mFileDelete = new File(mArrayListInstallations.get(position).getInst_local_installer_sign_url());
                                            if (mFileDelete != null && mFileDelete.exists()) {
                                                mFileDelete.delete();
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    String mStringQuery = "delete from " + mActivity.mDbHelper.mTableInstall + " where " + mActivity.mDbHelper.mFieldInstallLocalId + "= '" + mArrayListInstallations.get(position).getInst_local_id() + "'";
                                    mActivity.mDbHelper.exeQuery(mStringQuery);
                                    System.out.println("Installation deleted In Local Db");

                                    deleteDBInstallationPhotoList(mArrayListInstallations.get(position).getInst_local_id());

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
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListInstallations.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_unsync_install_item_textview_vessel_name)
            TextView mTextViewVesselName;
            @BindView(R.id.raw_unsync_install_item_textview_reg_no)
            TextView mTextViewVesselRegNo;
            @BindView(R.id.raw_unsync_install_item_textview_type)
            TextView mTextViewType;
            @BindView(R.id.raw_unsync_install_item_textview_imei_no)
            TextView mTextViewIMEINo;
            @BindView(R.id.raw_unsync_install_item_textview_date)
            TextView mTextViewInstallationDate;
            @BindView(R.id.raw_unsync_install_item_textview_status)
            TextView mTextViewStatus;
            @BindView(R.id.raw_unsync_install_item_imageview_delete)
            ImageView mImageViewDelete;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

    }

    /*delete installation photo */
    private void deleteDBInstallationPhotoList(String installationLocalId) {
        System.out.println("CALLLL");
        DataHolder mDataHolder;
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInstallerPhoto + " where " + mActivity.mDbHelper.mFieldInstLocalId + "= '" + installationLocalId + "'";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Photo List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoInstallationPhoto mVoInstallationPhoto = new VoInstallationPhoto();
                    mVoInstallationPhoto.setInst_photo_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoLocalID));
                    mVoInstallationPhoto.setInst_photo_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoServerID));
                    mVoInstallationPhoto.setInst_photo_local_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoLocalURL));
                    mVoInstallationPhoto.setInst_photo_server_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoServerURL));
                    if (mVoInstallationPhoto.getInst_photo_local_url() != null && !mVoInstallationPhoto.getInst_photo_local_url().equals("") && !mVoInstallationPhoto.getInst_photo_local_url().equals("null")) {
                        File mFileDelete = new File(mVoInstallationPhoto.getInst_photo_local_url());
                        if (mFileDelete != null && mFileDelete.exists()) {
                            mFileDelete.delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String mStringQueryPhoto = "delete from " + mActivity.mDbHelper.mTableInstallerPhoto + " where " + mActivity.mDbHelper.mFieldInstLocalId + "= '" + installationLocalId + "'";
        mActivity.mDbHelper.exeQuery(mStringQueryPhoto);
        System.out.println("Installation deleted In Local Db");
        String mStringQueryQues = "delete from " + mActivity.mDbHelper.mTableQuestionAnswer + " where " + mActivity.mDbHelper.mFieldQuesAnsInsUninsInspType + "= 0" + " AND " + mActivity.mDbHelper.mFieldQuesAnsInsUninsInspLocalID + "= '" + installationLocalId + "'";
        mActivity.mDbHelper.exeQuery(mStringQueryQues);

        mActivity.getUnSyncedCount();
        getDBInstallationList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
