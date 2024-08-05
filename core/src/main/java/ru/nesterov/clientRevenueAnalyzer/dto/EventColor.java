package ru.nesterov.clientRevenueAnalyzer.dto;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum EventColor {
    DEFAULT(null, "Default calendar color"),
    LAVENDER("1", "Lavender"),
    SAGE("2", "Sage"),
    GRAPE("3", "Grape"),
    FLAMINGO("4", "Flamingo"),
    BANANA("5", "Banana"),
    TANGERINE("6", "Tangerine"),
    PEACOCK("7", "Peacock"),
    GRAPHITE("8", "Graphite"),
    BLUEBERRY("9", "Blueberry"),
    BASIL("10", "Basil"),
    TOMATO("11", "Tomato"),
    ORANGE("12", "Orange"),
    MANGO("13", "Mango"),
    PINE("14", "Pine"),
    CINNAMON("15", "Cinnamon"),
    OLIVE("16", "Olive"),
    CHERRY("17", "Cherry"),
    COCOA("18", "Cocoa"),
    PLUM("19", "Plum"),
    BASIL2("20", "Basil"); // "Basil" повторяется, поэтому для уникальности можно использовать "Basil2"

    private final String colorId;
    private final String colorName;

    EventColor(String colorId, String colorName) {
        this.colorId = colorId;
        this.colorName = colorName;
    }

    public static EventColor fromColorId(String colorId) {
        for (EventColor color : EventColor.values()) {
            if (Objects.equals(color.getColorId(), colorId)) {
                return color;
            }
        }
        throw new IllegalArgumentException("Unknown colorId: " + colorId);
    }

    @Override
    public String toString() {
        return colorName;
    }
}
