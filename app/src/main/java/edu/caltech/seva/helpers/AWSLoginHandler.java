package edu.caltech.seva.helpers;

public interface AWSLoginHandler {
    void onSignInSuccess();
    void onFailure(Exception exception);
}
