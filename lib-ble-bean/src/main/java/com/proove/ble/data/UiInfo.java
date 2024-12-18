package com.proove.ble.data;


import com.proove.ble.constant.protocol.UiAction;
import com.proove.ble.constant.protocol.UiFunction;

import java.util.List;

public class UiInfo {
    private UiFunction leftOneClickFunction;
    private UiFunction leftTwoClickFunction;
    private UiFunction leftThreeClickFunction;
    private UiFunction leftLongClickFunction;
    private UiFunction rightOneClickFunction;
    private UiFunction rightTwoClickFunction;
    private UiFunction rightThreeClickFunction;
    private UiFunction rightLongClickFunction;
    private List<UiFunction> uiFunctionList;
    private List<UiAction> uiActionList;

    public UiInfo() {
    }

    public UiFunction getLeftOneClickFunction() {
        return leftOneClickFunction;
    }

    public UiFunction getLeftTwoClickFunction() {
        return leftTwoClickFunction;
    }

    public UiFunction getLeftThreeClickFunction() {
        return leftThreeClickFunction;
    }

    public UiFunction getLeftLongClickFunction() {
        return leftLongClickFunction;
    }

    public UiFunction getRightOneClickFunction() {
        return rightOneClickFunction;
    }

    public UiFunction getRightTwoClickFunction() {
        return rightTwoClickFunction;
    }

    public UiFunction getRightThreeClickFunction() {
        return rightThreeClickFunction;
    }

    public UiFunction getRightLongClickFunction() {
        return rightLongClickFunction;
    }

    public void setLeftOneClickFunction(UiFunction leftOneClickFunction) {
        this.leftOneClickFunction = leftOneClickFunction;
    }

    public void setLeftTwoClickFunction(UiFunction leftTwoClickFunction) {
        this.leftTwoClickFunction = leftTwoClickFunction;
    }

    public void setLeftThreeClickFunction(UiFunction leftThreeClickFunction) {
        this.leftThreeClickFunction = leftThreeClickFunction;
    }

    public void setLeftLongClickFunction(UiFunction leftLongClickFunction) {
        this.leftLongClickFunction = leftLongClickFunction;
    }

    public void setRightOneClickFunction(UiFunction rightOneClickFunction) {
        this.rightOneClickFunction = rightOneClickFunction;
    }

    public void setRightTwoClickFunction(UiFunction rightTwoClickFunction) {
        this.rightTwoClickFunction = rightTwoClickFunction;
    }

    public void setRightThreeClickFunction(UiFunction rightThreeClickFunction) {
        this.rightThreeClickFunction = rightThreeClickFunction;
    }

    public void setRightLongClickFunction(UiFunction rightLongClickFunction) {
        this.rightLongClickFunction = rightLongClickFunction;
    }

    public List<UiFunction> getUiFunctionList() {
        return uiFunctionList;
    }

    public void setUiFunctionList(List<UiFunction> uiFunctionList) {
        this.uiFunctionList = uiFunctionList;
    }

    public List<UiAction> getUiActionList() {
        return uiActionList;
    }

    public void setUiActionList(List<UiAction> uiActionList) {
        this.uiActionList = uiActionList;
    }

    @Override
    public String toString() {
        return "UiInfo{" +
                "leftOneClickAction=" + leftOneClickFunction +
                ", leftTwoClickAction=" + leftTwoClickFunction +
                ", leftThreeClickAction=" + leftThreeClickFunction +
                ", leftLongClickAction=" + leftLongClickFunction +
                ", rightOneClickAction=" + rightOneClickFunction +
                ", rightTwoClickAction=" + rightTwoClickFunction +
                ", rightThreeClickAction=" + rightThreeClickFunction +
                ", rightLongClickAction=" + rightLongClickFunction +
                '}';
    }
}
