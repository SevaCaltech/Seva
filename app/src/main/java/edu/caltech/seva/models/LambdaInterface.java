package edu.caltech.seva.models;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;
import com.google.gson.JsonObject;

public interface LambdaInterface {

    @LambdaFunction
    JsonObject testButtonTriggered(LambdaTriggerInfo info);
}
