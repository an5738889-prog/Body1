package com.ben.bodyinjury.capability;

public enum BodyPart {
    HEAD("head", "Đầu"),
    CHEST("chest", "Ngực"),
    LEFT_ARM("left_arm", "Tay trái"),
    RIGHT_ARM("right_arm", "Tay phải"),
    LEFT_LEG("left_leg", "Chân trái"),
    RIGHT_LEG("right_leg", "Chân phải");

    private final String id;
    private final String display;

    BodyPart(String id, String display) {
        this.id = id;
        this.display = display;
    }

    public String getId() { return id; }
    public String getDisplay() { return display; }
}
