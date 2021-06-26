package com.succorfish.installer.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.succorfish.installer.MainActivity;
import com.succorfish.installer.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 10-04-2018.
 */

public class FragmentLegalDocList extends Fragment {

    View mViewRoot;
    Unbinder mUnbinder;
    MainActivity mActivity;

    //    @BindView(R.id.fragment_install_guide_list_recyclerView)
//    RecyclerView mRecyclerView;
//    ArrayList<String> mStringsArrayListTitle = new ArrayList<>();
//    InstallGuideAdapter mInstallGuideAdapter;
//    LinearLayoutManager mLayoutManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_legal_doc_list, container, false);
        mUnbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_dashboard_menu_legal_docs));
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);


//        mStringsArrayListTitle.add("Land installation Guide");
//        mStringsArrayListTitle.add("Marine installation Guide");

//        mInstallGuideAdapter = new InstallGuideAdapter();
//        mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
//        mRecyclerView.setLayoutManager(mLayoutManager);
//        mRecyclerView.setAdapter(mInstallGuideAdapter);

        return mViewRoot;
    }

    /*Open Terms and Condition pdf*/
    @OnClick(R.id.fragment_legal_doc_linearlayout_tnc)
    public void onTnCClick(View mView) {
        if (isAdded()) {
            FragmentViewInstallGuide mFragmentViewInstallGuide = new FragmentViewInstallGuide();
            Bundle mBundle = new Bundle();
            mBundle.putString("intent_title", getResources().getString(R.string.str_terms_n_condition));
            mBundle.putString("intent_file_url", "Succorfish_Terms.pdf");
            mBundle.putBoolean("intent_is_landscape", false);
            mActivity.replacesFragment(mFragmentViewInstallGuide, true, mBundle, 1);

        }
    }

    /*Open Warranty PDF*/
    @OnClick(R.id.fragment_legal_doc_linearlayout_warranty)
    public void onWarrantyClick(View mView) {
        if (isAdded()) {
            FragmentViewInstallGuide mFragmentViewInstallGuide = new FragmentViewInstallGuide();
            Bundle mBundle = new Bundle();
            mBundle.putString("intent_title", getResources().getString(R.string.str_warranty));
            mBundle.putString("intent_file_url", "Succorfish_Warranty.pdf");
            mBundle.putBoolean("intent_is_landscape", false);
            mActivity.replacesFragment(mFragmentViewInstallGuide, true, mBundle, 1);
        }
    }

    /*Open Health And Safety PDF*/
    @OnClick(R.id.fragment_legal_doc_ll_health_n_safety_info)
    public void onHealthNSafetyClick(View mView) {
        if (isAdded()) {
            FragmentViewInstallGuide mFragmentViewInstallGuide = new FragmentViewInstallGuide();
            Bundle mBundle = new Bundle();
            mBundle.putString("intent_title", getResources().getString(R.string.str_info_privacy_policy));
            mBundle.putString("intent_file_url", "Info_Security_Privacy_Policy_v1.4.pdf");
            mBundle.putBoolean("intent_is_landscape", false);
            mActivity.replacesFragment(mFragmentViewInstallGuide, true, mBundle, 1);
        }
    }
//    public class InstallGuideAdapter extends RecyclerView.Adapter<InstallGuideAdapter.ViewHolder> {
//
//        @Override
//        public InstallGuideAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_install_guide_list_item, parent, false);
//            return new InstallGuideAdapter.ViewHolder(itemView);
//        }
//
//        @Override
//        public void onBindViewHolder(InstallGuideAdapter.ViewHolder mViewHolder, final int position) {
//            if (isAdded()) {
//                if (mStringsArrayListTitle.get(position) != null && !mStringsArrayListTitle.get(position).equalsIgnoreCase("")) {
//                    mViewHolder.mTextViewName.setText(mStringsArrayListTitle.get(position));
//                }
//                mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (mStringsArrayListTitle != null) {
//                            if (position < mStringsArrayListTitle.size()) {
//                            }
//                        }
//                    }
//                });
//            }
//        }
//
//        @Override
//        public int getItemCount() {
//            return mStringsArrayListTitle.size();
//        }
//
//        public class ViewHolder extends RecyclerView.ViewHolder {
//
//            @BindView(R.id.raw_install_guide_list_item_textview_name)
//            TextView mTextViewName;
//
//            public ViewHolder(View itemView) {
//                super(itemView);
//                ButterKnife.bind(this, itemView);
//            }
//        }
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
        mActivity.mImageViewBack.setVisibility(View.GONE);
    }
}
