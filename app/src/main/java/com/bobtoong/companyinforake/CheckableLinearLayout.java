package com.bobtoong.companyinforake;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Checkable;

import com.bobtoong.companyinforake.R;

/**
 * Created by Owner on 2018-04-30.
 */

public class CheckableLinearLayout extends LinearLayout implements Checkable {

    public CheckableLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setChecked(boolean b) {
        CheckBox cb = (CheckBox) findViewById(R.id.customListViewcheckBox);
        if (cb.isChecked() != b) {
            cb.setChecked(b);
        }
    }

    @Override
    public boolean isChecked() {
        CheckBox cb = (CheckBox) findViewById(R.id.customListViewcheckBox);

        return cb.isChecked();
    }

    @Override
    public void toggle() {
        CheckBox cb = (CheckBox) findViewById(R.id.customListViewcheckBox);

        setChecked(cb.isChecked() ? false : true);
    }
}
