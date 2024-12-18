package com.proove.ble.data;

public class BatteryInfo {
    private int leftBattery;
    private int rightBattery;
    private int caseBattery;
    private boolean isLeftCharging;
    private boolean isRightCharging;
    private boolean isCaseCharging;

    public int getLeftBattery() {
        return leftBattery;
    }

    public void setLeftBattery(int leftBattery) {
        this.leftBattery = leftBattery;
    }

    public int getRightBattery() {
        return rightBattery;
    }

    public void setRightBattery(int rightBattery) {
        this.rightBattery = rightBattery;
    }

    public boolean isLeftCharging() {
        return isLeftCharging;
    }

    public void setLeftCharging(boolean leftCharging) {
        isLeftCharging = leftCharging;
    }

    public boolean isRightCharging() {
        return isRightCharging;
    }

    public void setRightCharging(boolean rightCharging) {
        isRightCharging = rightCharging;
    }

    public int getCaseBattery() {
        return caseBattery;
    }

    public void setCaseBattery(int caseBattery) {
        this.caseBattery = caseBattery;
    }

    public boolean isCaseCharging() {
        return isCaseCharging;
    }

    public void setCaseCharging(boolean caseCharging) {
        isCaseCharging = caseCharging;
    }

    @Override
    public String toString() {
        return "BatteryInfo{" +
                "leftBattery=" + leftBattery +
                ", rightBattery=" + rightBattery +
                ", caseBattery=" + caseBattery +
                ", isLeftCharging=" + isLeftCharging +
                ", isRightCharging=" + isRightCharging +
                ", isCaseCharging=" + isCaseCharging +
                '}';
    }
}
