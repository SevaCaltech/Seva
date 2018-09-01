package edu.caltech.seva.helpers;

public class DbContract {

    //Table for holding incoming error information
    public static final String NOTIFY_TABLE = "notificationInfo";
    public static final String ERROR_CODE = "errorCode";
    public static final String NOTIFY_DATE = "date";
    public static final String TOILET_ID = "toiletId";
    public static final String UPDATE_UI_FILTER = "josh.caltech.seva.seva_josh.UPDATE_UI";

    //Table for accessing toilet info, also uses TOILET_ID from above
    public static final String TOILET_INFO = "toiletInfo";
    public static final String TOILET_LAT = "latitude";
    public static final String TOILET_LNG = "longitude";
    public static final String TOILET_DESC = "description";

    //Table for accessing repair info, also uses ERROR_CODE from above
    public static final String INFO_TABLE = "repairInfo";
    public static final String REPAIR_TITLE = "repairTitle";
    public static final String TOTAL_STEPS = "totalSteps";
    public static final String TOTAL_TIME = "totalTime";
    public static final String TOOL_INFO = "toolInfo";

    //Table for accessing repair steps
    public static final String REPAIR_TABLE = "repairStep-";
    public static final String STEP_NUM = "stepNum";
    public static final String STEP_PIC = "stepPic";
    public static final String STEP_TEXT = "stepText";
    public static final String STEP_INFO = "stepInfo";
    public static final String STEP_SYMBOL = "stepSymbol";

}
