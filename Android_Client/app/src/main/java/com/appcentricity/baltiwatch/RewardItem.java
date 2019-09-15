package com.appcentricity.baltiwatch;

public class RewardItem {
    private String Cost;
    private String rewardTitle;
    public RewardItem(String cost) {
        this.Cost = cost;
    }

    public String getRewardTitle() {
        return rewardTitle;
    }

    public void setRewardTitle(String rewardTitle) {
        this.rewardTitle = rewardTitle;
    }

    public RewardItem(){}

    public void setCost(String Cost) {
        this.Cost = Cost;
    }

    public String getCost() {
        return Cost;
    }
}
