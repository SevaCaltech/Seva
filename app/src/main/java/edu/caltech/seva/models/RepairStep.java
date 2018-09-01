package edu.caltech.seva.models;

public class RepairStep {
    private int stepNum;
    private String stepPic, stepText, stepInfo, stepSymbol;

    public RepairStep (int stepNum, String stepPic, String stepText, String stepInfo, String stepSymbol) {
        this.stepInfo = stepInfo;
        this.stepNum = stepNum;
        this.stepPic = stepPic;
        this.stepText = stepText;
        this.stepSymbol = stepSymbol;
    }

    public int getStepNum() {
        return stepNum;
    }

    public void setStepNum(int stepNum) {
        this.stepNum = stepNum;
    }

    public String getStepPic() {
        return stepPic;
    }

    public void setStepPic(String stepPic) {
        this.stepPic = stepPic;
    }

    public String getStepText() {
        return stepText;
    }

    public void setStepText(String stepText) {
        this.stepText = stepText;
    }

    public String getStepInfo() {
        return stepInfo;
    }

    public void setStepInfo(String stepInfo) {
        this.stepInfo = stepInfo;
    }

    public String getStepSymbol() {
        return stepSymbol;
    }

    public void setStepSymbol(String stepSymbol) {
        this.stepSymbol = stepSymbol;
    }
}
