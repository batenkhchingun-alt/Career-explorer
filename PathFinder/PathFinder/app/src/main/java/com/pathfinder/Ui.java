package com.pathfinder;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.*;

/** Кодоор UI хийх туслах функцууд */
class Ui {
    static LinearLayout page(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        int p = (int) (20 * c.getResources().getDisplayMetrics().density);
        l.setPadding(p, p, p, p);
        return l;
    }
    static TextView title(Context c, String t) { TextView v = new TextView(c); v.setText(t); v.setTextSize(24); v.setPadding(0, 0, 0, 24); return v; }
    static TextView text(Context c, String t) { TextView v = new TextView(c); v.setText(t); v.setTextSize(15); v.setPadding(0, 8, 0, 8); return v; }
    static EditText input(Context c, String hint, boolean pass, boolean num) {
        EditText e = new EditText(c); e.setHint(hint);
        e.setInputType(pass ? (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                : num ? InputType.TYPE_CLASS_PHONE : InputType.TYPE_CLASS_TEXT);
        return e;
    }
    static Button btn(Context c, String t, View.OnClickListener l) { Button b = new Button(c); b.setText(t); b.setOnClickListener(l); return b; }
    static void toast(Context c, String m) { Toast.makeText(c, m, Toast.LENGTH_SHORT).show(); }
    static String s(EditText e) { return e.getText().toString().trim(); }
}
