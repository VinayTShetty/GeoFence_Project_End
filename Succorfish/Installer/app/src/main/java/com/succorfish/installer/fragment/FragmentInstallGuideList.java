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

public class FragmentInstallGuideList extends Fragment {

    View mViewRoot;
    Unbinder mUnbinder;
    MainActivity mActivity;

    @BindView(R.id.fragment_install_guide_linearlayout_land)
    LinearLayout mLinearLayoutLand;
    @BindView(R.id.fragment_install_guide_linearlayout_marine)
    LinearLayout mLinearLayoutMarine;
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
        mViewRoot = inflater.inflate(R.layout.fragment_install_guide_list, container, false);
        mUnbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_dashboard_menu_install_guide));
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
    /*Open Land pdf*/
    @OnClick(R.id.fragment_install_guide_linearlayout_land)
    public void onLandClick(View mView) {
        if (isAdded()) {
            FragmentViewInstallGuide mFragmentViewInstallGuide = new FragmentViewInstallGuide();
            Bundle mBundle = new Bundle();
            mBundle.putString("intent_title",  getResources().getString(R.string.str_land_installation_guide));
            mBundle.putString("intent_file_url", "Succorfish_Land.pdf");
            mBundle.putBoolean("intent_is_landscape", false);
            mActivity.replacesFragment(mFragmentViewInstallGuide, true, mBundle, 1);

        }
    }
    /*Open Marin PDF*/
    @OnClick(R.id.fragment_install_guide_linearlayout_marine)
    public void onMarineClick(View mView) {
        if (isAdded()) {
            FragmentViewInstallGuide mFragmentViewInstallGuide = new FragmentViewInstallGuide();
            Bundle mBundle = new Bundle();
            mBundle.putString("intent_title", getResources().getString(R.string.str_marine_installation_guide));
            mBundle.putString("intent_file_url", "Succorfish_Marine.pdf");
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
