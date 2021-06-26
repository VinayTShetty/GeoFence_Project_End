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
import com.succorfish.installer.Vo.VoInspection;
import com.succorfish.installer.Vo.VoInspectionPhoto;
import com.succorfish.installer.Vo.VoQuestion;
import com.succorfish.installer.Vo.VoQuestionAns;
import com.succorfish.installer.Vo.VoUnInstall;
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

public class FragmentUnsyncedInspection extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.fragment_unsynced_install_recyclerView)
    RecyclerView mRecyclerViewUnInstallation;
    @BindView(R.id.fragment_unsynced_install_textview_no_list)
    TextView mTextViewNoListFound;
    public ArrayList<VoInspection> mArrayListInspectionList = new ArrayList<>();
    UnInstallationAdapter mUnInstallationAdapter;
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
            mTextViewNoListFound.setText(getResources().getString(R.string.str_unsynced_inspection_no_item));
        }
        /*get all un sync inspection list*/
        getDBInspectionList();
        return mViewRoot;
    }

    /*get all un sync inspection list*/
    private void getDBInspectionList() {
        DataHolder mDataHolder;
        mArrayListInspectionList = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInspection + " where " + mActivity.mDbHelper.mFieldInspectionIsSync + "= 0" + " AND " + mActivity.mDbHelper.mFieldInspectionUserId + "= '" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId() + "'" + " order by " + mActivity.mDbHelper.mFieldInspectionLocalId + " DESC";
            ;
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoInspection mVoInspection = new VoInspection();
                    mVoInspection.setInsp_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionLocalId));
                    mVoInspection.setInsp_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionServerId));
                    mVoInspection.setInsp_user_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionUserId));
                    mVoInspection.setInsp_device_imei_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceIMEINo));
                    mVoInspection.setInsp_device_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceServerId));
                    mVoInspection.setInsp_device_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceLocalId));
                    mVoInspection.setInsp_device_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDevicName));
                    mVoInspection.setInsp_device_warranty_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceWarranty_status));
                    mVoInspection.setInsp_device_type_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceTypeName));
                    mVoInspection.setInsp_vessel_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionVesselLocalId));
                    mVoInspection.setInsp_vessel_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionVesselServerId));
                    mVoInspection.setInsp_vessel_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionVesselName));
                    mVoInspection.setInsp_vessel_regi_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionVesselRegNo));
                    mVoInspection.setInsp_owner_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerName));
                    mVoInspection.setInsp_owner_address(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerAddress));
                    mVoInspection.setInsp_owner_city(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerCity));
                    mVoInspection.setInsp_owner_state(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerState));
                    mVoInspection.setInsp_owner_zipcode(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerZipcode));
                    mVoInspection.setInsp_owner_email(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerEmail));
                    mVoInspection.setInsp_owner_mobile_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerMobileNo));
                    mVoInspection.setInsp_result(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionResult));
                    mVoInspection.setInsp_action_taken(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionActionTaken));
                    mVoInspection.setInsp_warranty_return(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionWarrentyReturn));
                    mVoInspection.setInsp_local_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionLocalSignUrl));
                    mVoInspection.setInsp_server_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionServerSignUrl));
                    mVoInspection.setInsp_local_inspector_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionLocalInspectorSignUrl));
                    mVoInspection.setInsp_server_inspector_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionServerInspectorSignUrl));
                    mVoInspection.setInsp_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionCreatedDate));
                    mVoInspection.setInsp_updated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionUpdatedDate));
                    mVoInspection.setInsp_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionIsSync));
                    mVoInspection.setInsp_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionStatus));

                    mArrayListInspectionList.add(mVoInspection);
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
        mUnInstallationAdapter = new UnInstallationAdapter();
        mRecyclerViewUnInstallation.setLayoutManager(mLayoutManager);
        mRecyclerViewUnInstallation.setAdapter(mUnInstallationAdapter);
        mUnInstallationAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });
        checkAdapterIsEmpty();
    }

    private void checkAdapterIsEmpty() {
        if (mUnInstallationAdapter.getItemCount() == 0) {
            mTextViewNoListFound.setVisibility(View.VISIBLE);
            mRecyclerViewUnInstallation.setVisibility(View.GONE);
        } else {
            mTextViewNoListFound.setVisibility(View.GONE);
            mRecyclerViewUnInstallation.setVisibility(View.VISIBLE);
        }
    }

    /*Unsync inspection list Adapter*/
    public class UnInstallationAdapter extends RecyclerView.Adapter<UnInstallationAdapter.ViewHolder> {

        @Override
        public UnInstallationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_unsync_install_list, parent, false);
            return new UnInstallationAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final UnInstallationAdapter.ViewHolder mViewHolder, final int position) {
            if (mArrayListInspectionList.get(position).getInsp_device_imei_no() != null && !mArrayListInspectionList.get(position).getInsp_device_imei_no().equalsIgnoreCase("")) {
                mViewHolder.mTextViewIMEINo.setText(mArrayListInspectionList.get(position).getInsp_device_imei_no());
            } else {
                mViewHolder.mTextViewIMEINo.setText("-NA-");
            }
            if (mArrayListInspectionList.get(position).getInsp_status() != null && !mArrayListInspectionList.get(position).getInsp_status().equalsIgnoreCase("")) {
                if (mArrayListInspectionList.get(position).getInsp_status().equalsIgnoreCase("0")) {
                    mViewHolder.mTextViewStatus.setVisibility(View.VISIBLE);
                } else {
                    mViewHolder.mTextViewStatus.setVisibility(View.GONE);
                }
            } else {
                mViewHolder.mTextViewStatus.setVisibility(View.GONE);
            }
            if (mArrayListInspectionList.get(position).getInsp_vessel_name() != null && !mArrayListInspectionList.get(position).getInsp_vessel_name().equalsIgnoreCase("")) {
                mViewHolder.mTextViewVesselName.setText(mArrayListInspectionList.get(position).getInsp_vessel_name());
            } else {
                mViewHolder.mTextViewVesselName.setText("-NA-");
            }
            if (mArrayListInspectionList.get(position).getInsp_date_time() != null && !mArrayListInspectionList.get(position).getInsp_date_time().equalsIgnoreCase("") && !mArrayListInspectionList.get(position).getInsp_date_time().equalsIgnoreCase("null")) {
                try {
                    Calendar mCalendar = Calendar.getInstance();
                    mCalendar.setTimeInMillis(Long.parseLong(mArrayListInspectionList.get(position).getInsp_date_time()));
                    mViewHolder.mTextViewInstallationDate.setText(mSimpleDateFormatDateDisplay.format(mCalendar.getTime()));
                } catch (Exception e) {
                    mViewHolder.mTextViewInstallationDate.setText("-NA-");
                    e.printStackTrace();
                }
            } else {
                mViewHolder.mTextViewInstallationDate.setText("-NA-");
            }
            if (mArrayListInspectionList.get(position).getInsp_vessel_regi_no() != null && !mArrayListInspectionList.get(position).getInsp_vessel_regi_no().equalsIgnoreCase("")) {
                mViewHolder.mTextViewVesselRegNo.setText(mArrayListInspectionList.get(position).getInsp_vessel_regi_no());
            } else {
                mViewHolder.mTextViewVesselRegNo.setText("-NA-");
            }
            if (mArrayListInspectionList.get(position).getInsp_device_type_name() != null && !mArrayListInspectionList.get(position).getInsp_device_type_name().equalsIgnoreCase("")) {
                mViewHolder.mTextViewType.setText(mArrayListInspectionList.get(position).getInsp_device_type_name());
            } else {
                mViewHolder.mTextViewType.setText("-NA-");
            }

            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListInspectionList != null) {
                        if (position < mArrayListInspectionList.size()) {
                            mActivity.mInstallUnInstallInspectStatus = 2;
                            mActivity.mIntInspectionId = Integer.parseInt(mArrayListInspectionList.get(position).getInsp_local_id());
                            Gson gson = new Gson();
                            VoQuestion mVoQuestion = gson.fromJson(Utility.fetchQuestionnaireJson(mActivity, "questions_list.json"), VoQuestion.class);
                            mActivity.mListQuestion = mVoQuestion.getQuestions();

                            List<VoQuestionAns> mListQuestionTemp = new ArrayList<>();
                            DataHolder mDataHolder;
                            try {
                                String url = "select * from " + mActivity.mDbHelper.mTableQuestionAnswer + " where " + mActivity.mDbHelper.mFieldQuesAnsInsUninsInspType + "= 2" + " AND " + mActivity.mDbHelper.mFieldQuesAnsInsUninsInspLocalID + "= '" + mActivity.mIntInspectionId + "'";
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
//                            mActivity.replacesFragment(new FragmentInspections(), true, null, 1);
                        }
                    }
                }
            });

            mViewHolder.mImageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListInspectionList != null) {
                        if (position < mArrayListInspectionList.size()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                            builder.setTitle(getResources().getString(R.string.str_remove));
                            builder.setCancelable(false);
                            builder.setMessage(getResources().getString(R.string.str_unsynced_inspection_remove_confirmation));
                            builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    try {
                                        if (mArrayListInspectionList.get(position).getInsp_local_inspector_sign_url() != null && !mArrayListInspectionList.get(position).getInsp_local_inspector_sign_url().equals("") && !mArrayListInspectionList.get(position).getInsp_local_inspector_sign_url().equals("null")) {
                                            File mFileDelete = new File(mArrayListInspectionList.get(position).getInsp_local_inspector_sign_url());
                                            if (mFileDelete != null && mFileDelete.exists()) {
                                                mFileDelete.delete();
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    String mStringQuery = "delete from " + mActivity.mDbHelper.mTableInspection + " where " + mActivity.mDbHelper.mFieldInspectionLocalId + "= '" + mArrayListInspectionList.get(position).getInsp_local_id() + "'";
                                    mActivity.mDbHelper.exeQuery(mStringQuery);
                                    System.out.println("Inspection deleted In Local Db");
                                    deleteDBInspectionPhotoList(mArrayListInspectionList.get(position).getInsp_local_id());
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
            return mArrayListInspectionList.size();
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
    /*Delete Inspection photo*/
    private void deleteDBInspectionPhotoList(String inspectionLocalId) {
        System.out.println("CALLLL");
        DataHolder mDataHolder;
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInspectionPhoto + " where " + mActivity.mDbHelper.mFieldInspcLocalId + "= '" + inspectionLocalId + "'";
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
                    if (mVoInspectionPhoto.getInsp_photo_local_url() != null && !mVoInspectionPhoto.getInsp_photo_local_url().equals("") && !mVoInspectionPhoto.getInsp_photo_local_url().equals("null")) {
                        File mFileDelete = new File(mVoInspectionPhoto.getInsp_photo_local_url());
                        if (mFileDelete != null && mFileDelete.exists()) {
                            mFileDelete.delete();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String mStringQueryPhoto = "delete from " + mActivity.mDbHelper.mTableInspectionPhoto + " where " + mActivity.mDbHelper.mFieldInspcLocalId + "= '" + inspectionLocalId + "'";
        mActivity.mDbHelper.exeQuery(mStringQueryPhoto);
        System.out.println("Inspection deleted In Local Db");
        String mStringQueryQues = "delete from " + mActivity.mDbHelper.mTableQuestionAnswer + " where " + mActivity.mDbHelper.mFieldQuesAnsInsUninsInspType + "= 2" + " AND " + mActivity.mDbHelper.mFieldQuesAnsInsUninsInspLocalID + "= '" + inspectionLocalId + "'";
        mActivity.mDbHelper.exeQuery(mStringQueryQues);
        mActivity.getUnSyncedCount();
        getDBInspectionList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
