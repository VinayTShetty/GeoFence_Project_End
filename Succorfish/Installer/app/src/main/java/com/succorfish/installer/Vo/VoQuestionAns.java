package com.succorfish.installer.Vo;

import java.io.Serializable;
import java.util.ArrayList;

public class VoQuestionAns implements Serializable {

    byte questionNo = 1;
    byte questionType = 1; // 1 single choice
    String questionName = "";
    byte ansDisplayOption = 2; // 2 ans - 3 ans
    byte chooseAns = 1;
    String ansComment = "";
    String ansText = "";

    public byte getQuestionNo() {
        return questionNo;
    }

    public void setQuestionNo(byte question_no) {
        this.questionNo = question_no;
    }

    public byte getQuestionType() {
        return questionType;
    }

    public void setQuestionType(byte questionType) {
        this.questionType = questionType;
    }

    public String getQuestionName() {
        return questionName;
    }

    public void setQuestionName(String questionName) {
        this.questionName = questionName;
    }

    public byte getAnsDisplayOption() {
        return ansDisplayOption;
    }

    public void setAnsDisplayOption(byte ansDisplayOption) {
        this.ansDisplayOption = ansDisplayOption;
    }

    public byte getChooseAns() {
        return chooseAns;
    }

    public void setChooseAns(byte chooseAns) {
        this.chooseAns = chooseAns;
    }

    public String getAnsComment() {
        return ansComment;
    }

    public void setAnsComment(String ansComment) {
        this.ansComment = ansComment;
    }

    public String getAnsText() {
        return ansText;
    }

    public void setAnsText(String ansText) {
        this.ansText = ansText;
    }
}
