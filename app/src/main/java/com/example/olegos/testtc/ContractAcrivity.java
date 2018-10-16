package com.example.olegos.testtc;


public interface ContractAcrivity {
    void startRecordClick();

    void startPlayClick();

    void setEnableButton(Boolean b);

    void progressChanged(int i);

    void setDefaultProgressBar();

    void setMessage(String message);
}
