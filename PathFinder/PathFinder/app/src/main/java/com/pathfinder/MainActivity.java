package com.pathfinder;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

/** Доод цэс: Нүүр | Профайл | Тохиргоо | Гарах */
public class MainActivity extends AppCompatActivity {
    FrameLayout content; Db db; ContentValues me;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        db = new Db(this); me = db.get(Db.uid(this));
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        content = new FrameLayout(this);
        root.addView(content, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout nav = new LinearLayout(this);
        String[] names = {"Нүүр", "Профайл", "Тохиргоо", "Гарах"};
        for (int i = 0; i < 4; i++) {
            final int k = i;
            Button x = Ui.btn(this, names[i], v -> show(k));
            nav.addView(x, new LinearLayout.LayoutParams(0, -2, 1));
        }
        root.addView(nav);
        setContentView(root);
        show(0);
    }

    void show(int tab) {
        content.removeAllViews();
        if (tab == 3) { confirmLogout(); return; }
        View v = tab == 0 ? home() : tab == 1 ? profile() : settings();
        content.addView(new ScrollView(this) {{ addView(v); }});
    }

    /** Ажил хайгчид байгууллагуудыг, байгууллагад ажил хайгчдыг харуулна */
    View home() {
        boolean seeker = "seeker".equals(me.getAsString("role"));
        LinearLayout l = Ui.page(this);
        l.addView(Ui.title(this, seeker ? "Ажлын санал (байгууллагууд)" : "Туршилтад хамрагдах хүмүүс"));
        List<ContentValues> list = db.listByRole(seeker ? "org" : "seeker");
        if (list.isEmpty()) l.addView(Ui.text(this, "Одоогоор бүртгэл алга."));
        for (ContentValues c : list) {
            String sub = seeker ? "📍 " + c.getAsString("location") : "📞 " + c.getAsString("phone");
            l.addView(Ui.btn(this, c.getAsString("name") + "\n" + sub, v -> details(c, seeker)));
        }
        return l;
    }

    void details(ContentValues c, boolean org) {
        String msg = org
            ? "Байгууллага: " + c.getAsString("name") + "\nХолбоо барих: " + c.getAsString("contact")
              + "\nБайршил: " + c.getAsString("location") + "\nУтас: " + c.getAsString("phone")
            : "Нэр: " + c.getAsString("name") + "\nУтас: " + c.getAsString("phone");
        new AlertDialog.Builder(this).setTitle("Дэлгэрэнгүй").setMessage(msg)
            .setPositiveButton("Залгах", (d, w) -> startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + c.getAsString("phone")))))
            .setNegativeButton("Хаах", null).show();
    }

    View profile() {
        LinearLayout l = Ui.page(this);
        l.addView(Ui.title(this, "Профайл"));
        if ("seeker".equals(me.getAsString("role"))) {
            ImageView iv = new ImageView(this);
            iv.setImageBitmap(BitmapFactory.decodeFile(me.getAsString("photo")));
            iv.setAdjustViewBounds(true); iv.setMaxHeight(500);
            l.addView(iv);
            l.addView(Ui.text(this, "Нэр: " + me.getAsString("name")));
            l.addView(Ui.text(this, "Утас: " + me.getAsString("phone")));
            String r = me.getAsString("regno");   // регистрийг хагас нууцалж харуулна
            l.addView(Ui.text(this, "Регистр: " + r.substring(0, 2) + "******" + r.substring(8)));
        } else {
            l.addView(Ui.text(this, "Байгууллага: " + me.getAsString("name")));
            l.addView(Ui.text(this, "Холбоо барих: " + me.getAsString("contact")));
            l.addView(Ui.text(this, "Байршил: " + me.getAsString("location")));
            l.addView(Ui.text(this, "Утас: " + me.getAsString("phone")));
        }
        return l;
    }

    View settings() {
        LinearLayout l = Ui.page(this);
        l.addView(Ui.title(this, "Тохиргоо"));
        l.addView(Ui.text(this, "Аппын журам:"));
        l.addView(Ui.text(this, TermsActivity.TERMS));
        l.addView(Ui.text(this, "PathFinder v1.0"));
        return l;
    }

    void confirmLogout() {
        new AlertDialog.Builder(this).setMessage("Та гарахдаа итгэлтэй байна уу?")
            .setPositiveButton("Тийм", (d, w) -> {
                Db.setUid(this, -1);
                startActivity(new Intent(this, LoginActivity.class));
                finishAffinity();
            })
            .setNegativeButton("Үгүй", (d, w) -> show(0)).show();
    }
}
