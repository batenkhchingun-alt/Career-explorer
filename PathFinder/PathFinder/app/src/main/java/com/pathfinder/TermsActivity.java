package com.pathfinder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

/** Аппын журам унших, зөвшөөрөх хэсэг */
public class TermsActivity extends AppCompatActivity {
    static final String TERMS =
        "1. PathFinder нь 1 өдрийн туршилтаар ажил хийж үзэх боломжийг холбогч платформ юм.\n\n"
      + "2. Хэрэглэгч үнэн зөв мэдээлэл оруулна. Хуурамч мэдээлэл оруулбал бүртгэлийг хаана.\n\n"
      + "3. Нэр, утас, регистрийн дугаар, e-Mongolia зургийг зөвхөн хоёр талын найдвартай байдлыг хангах зорилгоор ашиглана.\n\n"
      + "4. Туршилтын ажлыг зөвхөн хоёр тал урьдчилан тохирсон байршил, хугацаанд хийнэ.\n\n"
      + "5. Хүн дагалдан явах үед тухайн хүний мэдээллийг (нэр, утас) байгууллагад харуулна.\n\n"
      + "6. Хэрэглэгч бусдын эрх, аюулгүй байдлыг хүндэтгэнэ. Дарамт, хууран мэхлэлтийг хатуу хориглоно.\n\n"
      + "7. Хувийн мэдээллийг гуравдагч этгээдэд зөвшөөрөлгүй дамжуулахгүй.";

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = Ui.page(this);
        l.addView(Ui.title(this, "Аппын журам"));
        l.addView(Ui.text(this, TERMS));
        CheckBox cb = new CheckBox(this); cb.setText("Журмыг уншиж, зөвшөөрч байна");
        l.addView(cb);
        l.addView(Ui.btn(this, "Үргэлжлүүлэх", v -> {
            if (!cb.isChecked()) { Ui.toast(this, "Журмыг зөвшөөрнө үү"); return; }
            Db.acceptTerms(this);
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }));
        setContentView(new ScrollView(this) {{ addView(l); }});
    }
}
