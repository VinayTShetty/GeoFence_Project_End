package com.succorfish.geofence.customObjects;

public class ButtonBandConfiguration {
    private boolean buttonChecked;
    private long button_Ifchecked_value;
    private int button_id;
    private int button_label_value;

    public ButtonBandConfiguration(boolean buttonChecked_loc, int loc_button_label_value, int loc_button_id) {
        this.buttonChecked = buttonChecked_loc;
        this.button_label_value = loc_button_label_value;
        this.button_id = loc_button_id;
    }

    public boolean isButtonChecked() {
        return buttonChecked;
    }

    public void setButtonChecked(boolean buttonChecked) {
        this.buttonChecked = buttonChecked;
    }

    public long getButton_Ifchecked_value() {
        return button_Ifchecked_value;
    }

    public void setButton_Ifchecked_value(int button_label_value) {
        if(button_label_value!=0){
            int base = 2, exponent = button_label_value;
            button_Ifchecked_value = 1;
            while (exponent != 0) {
                button_Ifchecked_value *= base;
                --exponent;
            }
        }else {
            button_Ifchecked_value=1;
        }
    }

    public int getButton_id() {
        return button_id;
    }

    public void setButton_id(int button_id) {
        this.button_id = button_id;
    }

    public int getButton_label_value() {
        return button_label_value;
    }

    public void setButton_label_value(int button_label_value) {
        this.button_label_value = button_label_value;
    }
}
