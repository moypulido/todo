package com.example.to_do.data.model;

import android.content.Context;

import com.example.to_do.R;

public enum Category {
    WORK,
    PERSONAL,
    SHOPPING,
    HEALTH,
    OTHER;

    public String getDisplayName(Context context) {
        switch (this) {
            case WORK:
                return context.getString(R.string.category_work);
            case PERSONAL:
                return context.getString(R.string.category_personal);
            case SHOPPING:
                return context.getString(R.string.category_shopping);
            case HEALTH:
                return context.getString(R.string.category_health);
            case OTHER:
                return context.getString(R.string.category_other);
            default:
                return "";
        }
    }
}