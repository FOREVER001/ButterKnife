package com.zxh.library;

import android.view.View;

public abstract class DebouncingOnClickListener implements View.OnClickListener{
    @Override
    public void onClick(View v) {
        doClick(v);
    }
    public abstract void doClick(View v);
}
