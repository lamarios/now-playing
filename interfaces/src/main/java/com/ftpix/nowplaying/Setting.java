package com.ftpix.nowplaying;

import javafx.util.Pair;

import java.util.List;

public class Setting {
    private String label, name, value, description;

    /**
     * The type of the setting (relates to html componenets
     */
    private SettingType type;
    /**
     * Only for SELECT type
     */
    private List<Pair<String, String>> values;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SettingType getType() {
        return type;
    }

    public void setType(SettingType type) {
        this.type = type;
    }

    public List<Pair<String, String>> getValues() {
        return values;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setValues(List<Pair<String, String>> values) {
        this.values = values;
    }
}
