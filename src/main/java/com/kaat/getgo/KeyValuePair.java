package com.kaat.getgo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class KeyValuePair implements RequestController.TableRow {
    private final BooleanProperty selected;
    private final StringProperty key;
    private final StringProperty value;

    public KeyValuePair(boolean selected, String key, String value) {
        this.selected = new SimpleBooleanProperty(selected);
        this.key = new SimpleStringProperty(key);
        this.value = new SimpleStringProperty(value);
    }

    public BooleanProperty selectedProperty() {
        return selected;
    }

    public StringProperty keyProperty() {
        return key;
    }

    public StringProperty valueProperty() {
        return value;
    }
}
