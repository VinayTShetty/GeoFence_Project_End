package com.vithamastech.smartlight;

import androidx.annotation.Nullable;

import java.io.Serializable;

public class IconSelector implements Serializable {
    public int source;
    public boolean isSelected;

    public IconSelector(int source, boolean isSelected) {
        this.source = source;
        this.isSelected = false;
    }

    public IconSelector(int source) {
        this.source = source;
        this.isSelected = false;
    }

    public IconSelector() {
        this.isSelected = false;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return obj instanceof IconSelector && (this.source == (((IconSelector) obj).source));
    }
}
