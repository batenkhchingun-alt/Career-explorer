package com.pathfinder;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import java.security.*;
import java.util.*;

/** SQLite + session. Нууц үгийг давстай SHA-256-аар хадгална (энгийн текстээр хадгалахгүй). */
public class Db extends SQLiteOpenHelper {
    public Db(Context c) { super(c, "pathfinder.db", null, 1); }

    @Override public void onCreate(SQLiteDatabase d) {
        d.execSQL("CREATE TABLE users(id INTEGER PRIMARY KEY AUTOINCREMENT, role TEXT, name TEXT, phone TEXT UNIQUE,"
                + "regno TEXT, photo TEXT, contact TEXT, location TEXT, salt TEXT, hash TEXT)");
    }
    @Override public void onUpgrade(SQLiteDatabase d, int o, int n) {}

    static String hash(String pass, String salt) {
        try {
            byte[] b = MessageDigest.getInstance("SHA-256").digest((salt + pass).getBytes("UTF-8"));
            StringBuilder s = new StringBuilder();
            for (byte x : b) s.append(String.format("%02x", x));
            return s.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    /** @return false бол утасны дугаар бүртгэлтэй байна */
    public long register(ContentValues v, String pass) {
        String salt = UUID.randomUUID().toString();
        v.put("salt", salt); v.put("hash", hash(pass, salt));
        return getWritableDatabase().insert("users", null, v); // -1 бол давхардсан
    }

    /** @return хэрэглэгчийн id, эсвэл -1 */
    public long login(String phone, String pass) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id,salt,hash FROM users WHERE phone=?", new String[]{phone})) {
            if (c.moveToFirst() && hash(pass, c.getString(1)).equals(c.getString(2))) return c.getLong(0);
        }
        return -1;
    }

    public ContentValues get(long id) {
        ContentValues v = new ContentValues();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT role,name,phone,regno,photo,contact,location FROM users WHERE id=?", new String[]{"" + id})) {
            if (c.moveToFirst()) {
                String[] k = {"role","name","phone","regno","photo","contact","location"};
                for (int i = 0; i < k.length; i++) v.put(k[i], c.getString(i));
            }
        }
        return v;
    }

    public List<ContentValues> listByRole(String role) {
        List<ContentValues> out = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id FROM users WHERE role=?", new String[]{role})) {
            while (c.moveToNext()) out.add(get(c.getLong(0)));
        }
        return out;
    }

    // ---- session ----
    static SharedPreferences sp(Context c) { return c.getSharedPreferences("session", MODE_PRIVATE_); }
    static final int MODE_PRIVATE_ = Context.MODE_PRIVATE;
    static long uid(Context c) { return sp(c).getLong("uid", -1); }
    static void setUid(Context c, long id) { sp(c).edit().putLong("uid", id).apply(); }
    static boolean termsOk(Context c) { return sp(c).getBoolean("terms_" + uid(c), false); }
    static void acceptTerms(Context c) { sp(c).edit().putBoolean("terms_" + uid(c), true).apply(); }
}
