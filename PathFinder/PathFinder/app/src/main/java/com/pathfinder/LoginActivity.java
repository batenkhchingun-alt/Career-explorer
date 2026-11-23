package com.pathfinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (Db.uid(this) != -1) { next(); return; }   // нэвтэрсэн бол шууд үргэлжлүүлнэ

        LinearLayout l = Ui.page(this);
        l.addView(Ui.title(this, "PathFinder"));
        EditText phone = Ui.input(this, "Утасны дугаар", false, true);
        EditText pass = Ui.input(this, "Нууц үг", true, false);
        l.addView(phone); l.addView(pass);
        l.addView(Ui.btn(this, "Нэвтрэх", v -> {
            long id = new Db(this).login(Ui.s(phone), Ui.s(pass));
            if (id == -1) { Ui.toast(this, "Утас эсвэл нууц үг буруу"); return; }
            Db.setUid(this, id); next();
        }));
        l.addView(Ui.btn(this, "Бүртгүүлэх", v -> startActivity(new Intent(this, SignUpActivity.class))));
        setContentView(new ScrollView(this) {{ addView(l); }});
    }
    private void next() {
        startActivity(new Intent(this, Db.termsOk(this) ? MainActivity.class : TermsActivity.class));
        finish();
    }
}
