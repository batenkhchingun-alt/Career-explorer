package com.pathfinder;

import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.io.*;

public class SignUpActivity extends AppCompatActivity {
    String photoPath = "";
    TextView photoLbl;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout l = Ui.page(this);
        l.addView(Ui.title(this, "Бүртгүүлэх"));

        RadioGroup rg = new RadioGroup(this);
        RadioButton seeker = new RadioButton(this); seeker.setText("Ажил хайгч (туршиж үзэх)"); seeker.setId(1);
        RadioButton org = new RadioButton(this); org.setText("Байгууллага"); org.setId(2);
        rg.addView(seeker); rg.addView(org); seeker.setChecked(true);
        l.addView(rg);

        // Ажил хайгчийн талбарууд
        LinearLayout sBox = new LinearLayout(this); sBox.setOrientation(LinearLayout.VERTICAL);
        EditText name = Ui.input(this, "Нэр", false, false);
        EditText reg = Ui.input(this, "Регистрийн дугаар", false, false);
        photoLbl = Ui.text(this, "e-Mongolia зураг: сонгоогүй");
        sBox.addView(name); sBox.addView(reg); sBox.addView(photoLbl);
        sBox.addView(Ui.btn(this, "e-Mongolia зураг сонгох", v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.setType("image/*");
            startActivityForResult(i, 1);
        }));

        // Байгууллагын талбарууд
        LinearLayout oBox = new LinearLayout(this); oBox.setOrientation(LinearLayout.VERTICAL);
        EditText orgName = Ui.input(this, "Байгууллагын нэр", false, false);
        EditText contact = Ui.input(this, "Холбоо барих хүн", false, false);
        EditText loc = Ui.input(this, "Байршил (хаяг)", false, false);
        oBox.addView(orgName); oBox.addView(contact); oBox.addView(loc);
        oBox.setVisibility(android.view.View.GONE);

        rg.setOnCheckedChangeListener((g, id) -> {
            sBox.setVisibility(id == 1 ? android.view.View.VISIBLE : android.view.View.GONE);
            oBox.setVisibility(id == 2 ? android.view.View.VISIBLE : android.view.View.GONE);
        });
        l.addView(sBox); l.addView(oBox);

        EditText phone = Ui.input(this, "Утасны дугаар", false, true);
        EditText pass = Ui.input(this, "Нууц үг (6+ тэмдэгт)", true, false);
        l.addView(phone); l.addView(pass);

        l.addView(Ui.btn(this, "Бүртгүүлэх", v -> {
            boolean isSeeker = rg.getCheckedRadioButtonId() == 1;
            if (Ui.s(phone).length() < 8 || Ui.s(pass).length() < 6) { Ui.toast(this, "Утас/нууц үгээ шалгана уу"); return; }
            ContentValues cv = new ContentValues();
            cv.put("phone", Ui.s(phone));
            if (isSeeker) {
                if (Ui.s(name).isEmpty() || Ui.s(reg).length() != 10 || photoPath.isEmpty()) {
                    Ui.toast(this, "Нэр, регистр (10 тэмдэгт), зургаа бөглөнө үү"); return; }
                cv.put("role", "seeker"); cv.put("name", Ui.s(name));
                cv.put("regno", Ui.s(reg).toUpperCase()); cv.put("photo", photoPath);
            } else {
                if (Ui.s(orgName).isEmpty() || Ui.s(contact).isEmpty() || Ui.s(loc).isEmpty()) {
                    Ui.toast(this, "Бүх талбарыг бөглөнө үү"); return; }
                cv.put("role", "org"); cv.put("name", Ui.s(orgName));
                cv.put("contact", Ui.s(contact)); cv.put("location", Ui.s(loc));
            }
            long id = new Db(this).register(cv, Ui.s(pass));
            if (id == -1) { Ui.toast(this, "Энэ утасны дугаар бүртгэлтэй байна"); return; }
            Db.setUid(this, id);
            startActivity(new Intent(this, TermsActivity.class));
            finishAffinity();
        }));
        setContentView(new ScrollView(this) {{ addView(l); }});
    }

    /** Зургийг апп-ийн хувийн санд хуулж хадгална */
    @Override protected void onActivityResult(int req, int res, Intent d) {
        super.onActivityResult(req, res, d);
        if (req != 1 || res != RESULT_OK || d == null) return;
        try (InputStream in = getContentResolver().openInputStream(d.getData());
             OutputStream out = new FileOutputStream(new File(getFilesDir(), "emn_" + System.currentTimeMillis() + ".jpg"))) {
            byte[] buf = new byte[8192]; int n;
            File f = null;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            File[] fs = getFilesDir().listFiles();
            long best = 0;
            for (File x : fs) if (x.getName().startsWith("emn_") && x.lastModified() >= best) { best = x.lastModified(); f = x; }
            photoPath = f.getAbsolutePath();
            photoLbl.setText("e-Mongolia зураг: сонгосон ✓");
        } catch (Exception e) { Ui.toast(this, "Зураг уншиж чадсангүй"); }
    }
}
