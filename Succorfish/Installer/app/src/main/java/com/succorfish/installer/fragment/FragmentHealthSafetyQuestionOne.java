package com.succorfish.installer.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.succorfish.installer.MainActivity;
import com.succorfish.installer.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Jaydeep on 22-02-2018.
 */

public class FragmentHealthSafetyQuestionOne extends Fragment implements View.OnClickListener {

    Button mButtonNext;
    View mViewRoot;
    MainActivity mActivity;
    RecyclerView mRecyclerView;
    private OnStepOneListener mListener;

    QuestionAdapter mQuestionAdapter;

    public FragmentHealthSafetyQuestionOne() {
        // Required empty public constructor
    }

    public static FragmentHealthSafetyQuestionOne newInstance() {
        return new FragmentHealthSafetyQuestionOne();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_health_safety_ques_one, container, false);
        mRecyclerView = (RecyclerView) mViewRoot.findViewById(R.id.frg_health_safety_ques_one_recyclerView);
        mButtonNext = (Button) mViewRoot.findViewById(R.id.nextBT);
        mQuestionAdapter = new QuestionAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mQuestionAdapter);

        return mViewRoot;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.nextBT:
                /*Check all question ans, validate and move to next screen*/
                for (int i = 0; i < 3; i++) {
                    if (mActivity.mListQuestion.get(i).getChooseAns() == 2) {
                        if (mActivity.mListQuestion.get(i).getAnsComment().equalsIgnoreCase("")) {
                            mActivity.mUtility.errorDialog("Please explain the reason for Question " + mActivity.mListQuestion.get(i).getQuestionNo());
                            return;
                        }
                    }
                }
                if (mListener != null) {
                    mListener.onNextPressed(this);
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mButtonNext.setOnClickListener(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        mButtonNext.setOnClickListener(null);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (context instanceof OnStepOneListener) {
                System.out.println("mListener Attached");
                mListener = (OnStepOneListener) context;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnStepOneListener {
        //void onFragmentInteraction(Uri uri);
        void onNextPressed(Fragment fragment);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        System.out.println("mListener onDetach");
        mListener = null;
        mButtonNext = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mUtility.hideKeyboard(mActivity);
    }

    /*Question List adapter*/
    public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {

        @Override
        public QuestionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_health_safety_questions, parent, false);
            return new QuestionAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final QuestionAdapter.ViewHolder mViewHolder, final int position) {

            mViewHolder.mTextViewQuestionNo.setText("Q" + mActivity.mListQuestion.get(position).getQuestionNo() + ".");
            mViewHolder.mAppCompatEditTextComment.setText(mActivity.mListQuestion.get(position).getAnsComment());
            if (mActivity.mListQuestion.get(position).getQuestionName() != null && !mActivity.mListQuestion.get(position).getQuestionName().equalsIgnoreCase("")) {
                mViewHolder.mTextViewQuestionName.setText(mActivity.mListQuestion.get(position).getQuestionName());
            } else {
                mViewHolder.mTextViewQuestionName.setText("NA");
            }
            if (mActivity.mListQuestion.get(position).getQuestionType() == 1) {
                if (mActivity.mListQuestion.get(position).getAnsDisplayOption() == 3) {
                    ((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(2)).setVisibility(View.VISIBLE);
                } else {
                    ((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(2)).setVisibility(View.GONE);
                }
                if (mActivity.mListQuestion.get(position).getChooseAns() == 2) {
                    ((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(1)).setChecked(true);
                    mViewHolder.mAppCompatEditTextComment.setVisibility(View.VISIBLE);
                    mViewHolder.mAppCompatEditTextComment.setText(mActivity.mListQuestion.get(position).getAnsComment());
                    mActivity.mListQuestion.get(position).setAnsText(((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(1)).getText().toString());
                } else if (mActivity.mListQuestion.get(position).getChooseAns() == 3) {
                    ((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(2)).setChecked(true);
                    mActivity.mListQuestion.get(position).setAnsText(((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(2)).getText().toString());
                } else {
                    ((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(0)).setChecked(true);
                    mActivity.mListQuestion.get(position).setAnsText(((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(0)).getText().toString());
                }
            }
            mViewHolder.mRadioGroupAns.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    mActivity.mUtility.hideKeyboard(mActivity);
                    mViewHolder.mAppCompatEditTextComment.setVisibility(View.GONE);
                    if (checkedId == R.id.raw_health_safety_rb_ans_no) {
                        mActivity.mListQuestion.get(position).setChooseAns((byte) 2);
                        mViewHolder.mAppCompatEditTextComment.setVisibility(View.VISIBLE);
                        mViewHolder.mAppCompatEditTextComment.setText(mActivity.mListQuestion.get(position).getAnsComment());
                        mActivity.mListQuestion.get(position).setAnsText(((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(1)).getText().toString());
                    } else if (checkedId == R.id.raw_health_safety_rb_ans_na) {
                        mActivity.mListQuestion.get(position).setChooseAns((byte) 3);
                        mActivity.mListQuestion.get(position).setAnsText(((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(2)).getText().toString());
                    } else {
                        mActivity.mListQuestion.get(position).setChooseAns((byte) 1);
                        mActivity.mListQuestion.get(position).setAnsText(((RadioButton) mViewHolder.mRadioGroupAns.getChildAt(0)).getText().toString());
                    }
                }
            });
            mViewHolder.mAppCompatEditTextComment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mActivity.mListQuestion.get(position).setAnsComment(mViewHolder.mAppCompatEditTextComment.getText().toString().trim());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mActivity.mListQuestion == null ? 0 : 3;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_health_safety_tv_qus_no)
            TextView mTextViewQuestionNo;
            @BindView(R.id.raw_health_safety_tv_qus)
            TextView mTextViewQuestionName;
            @BindView(R.id.raw_health_safety_rg_ans)
            RadioGroup mRadioGroupAns;
            @BindView(R.id.raw_health_safety_et_ans_comment)
            AppCompatEditText mAppCompatEditTextComment;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
