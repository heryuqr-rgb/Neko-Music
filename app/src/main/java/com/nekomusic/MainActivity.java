package com.nekomusic;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private final int bg = Color.rgb(13, 14, 20);
    private final int panel = Color.rgb(28, 30, 40);
    private final int accent = Color.rgb(185, 145, 255);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private LinearLayout results;
    private EditText search;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(12));
        root.setBackgroundColor(bg);
        setContentView(root);

        TextView brand = new TextView(this);
        brand.setText("neko music");
        brand.setTextColor(Color.WHITE);
        brand.setTextSize(30);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(brand);

        TextView subtitle = new TextView(this);
        subtitle.setText("Discover your next favorite song");
        subtitle.setTextColor(0xFF9A9CAB);
        subtitle.setTextSize(14);
        LinearLayout.LayoutParams subp = new LinearLayout.LayoutParams(-1, -2);
        subp.bottomMargin = dp(22);
        root.addView(subtitle, subp);

        LinearLayout searchRow = new LinearLayout(this);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);
        searchRow.setPadding(dp(14), 0, dp(8), 0);
        searchRow.setBackground(round(panel, 18));
        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextColor(Color.WHITE);
        search.setHintTextColor(0xFF858796);
        search.setHint("Search songs, artists...");
        search.setTextSize(16);
        search.setBackgroundColor(Color.TRANSPARENT);
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchRow.addView(search, new LinearLayout.LayoutParams(0, dp(52), 1));
        Button go = new Button(this);
        go.setText("GO");
        go.setTextColor(accent);
        go.setBackgroundColor(Color.TRANSPARENT);
        searchRow.addView(go);
        root.addView(searchRow);
        go.setOnClickListener(v -> doSearch());
        search.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_SEARCH) { doSearch(); return true; }
            return false;
        });

        status = new TextView(this);
        status.setText("Search the YouTube Music catalog");
        status.setTextColor(0xFF9A9CAB);
        status.setTextSize(13);
        LinearLayout.LayoutParams stp = new LinearLayout.LayoutParams(-1, -2);
        stp.topMargin = dp(20); stp.bottomMargin = dp(10);
        root.addView(status, stp);

        ScrollView scroll = new ScrollView(this);
        results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(results);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView footer = new TextView(this);
        footer.setText("Playback opens YouTube Music • Online catalog");
        footer.setTextColor(0xFF777987);
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, dp(34));
        root.addView(footer, fp);
    }

    private void doSearch() {
        String q = search.getText().toString().trim();
        if (q.isEmpty()) return;
        status.setText("Searching...");
        results.removeAllViews();
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                String base = BuildConfig.API_BASE_URL;
                URL url = new URL(base + "/search?q=" + URLEncoder.encode(q, "UTF-8") + "&limit=20");
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(12000);
                conn.setReadTimeout(20000);
                conn.setRequestMethod("GET");
                int code = conn.getResponseCode();
                if (code != 200) throw new Exception("API HTTP " + code);
                java.io.InputStream in = conn.getInputStream();
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                byte[] buf = new byte[4096]; int n;
                while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
                JSONObject json = new JSONObject(out.toString("UTF-8"));
                JSONArray tracks = json.getJSONArray("results");
                runOnUiThread(() -> renderTracks(tracks));
            } catch (Exception e) {
                runOnUiThread(() -> status.setText("Can't reach catalog API. Deploy backend first."));
            } finally { if (conn != null) conn.disconnect(); }
        });
    }

    private void renderTracks(JSONArray tracks) {
        results.removeAllViews();
        status.setText(tracks.length() + " songs found");
        if (tracks.length() == 0) {
            status.setText("No songs found. Try another search.");
            return;
        }
        for (int i = 0; i < tracks.length(); i++) {
            JSONObject item = tracks.optJSONObject(i);
            if (item == null) continue;
            String id = item.optString("videoId");
            String title = item.optString("title", "Unknown title");
            String artist = item.optString("artist", "Unknown artist");
            String album = item.optString("album", "");
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14), dp(10), dp(14), dp(10));
            row.setBackground(round(panel, 14));
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(-1, -2);
            rp.bottomMargin = dp(9);
            TextView disc = new TextView(this);
            disc.setText("♫");
            disc.setTextSize(26);
            disc.setTextColor(accent);
            disc.setGravity(Gravity.CENTER);
            row.addView(disc, new LinearLayout.LayoutParams(dp(48), dp(48)));
            LinearLayout labels = new LinearLayout(this);
            labels.setOrientation(LinearLayout.VERTICAL);
            labels.setPadding(dp(10), 0, dp(4), 0);
            TextView t = new TextView(this);
            t.setText(title); t.setTextColor(Color.WHITE); t.setTextSize(15);
            t.setTypeface(null, Typeface.BOLD); t.setMaxLines(1);
            TextView a = new TextView(this);
            a.setText(artist + (album.isEmpty() ? "" : " • " + album));
            a.setTextColor(0xFF9A9CAB); a.setTextSize(12); a.setMaxLines(1);
            labels.addView(t); labels.addView(a);
            row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
            TextView play = new TextView(this);
            play.setText("▶"); play.setTextColor(accent); play.setTextSize(19);
            row.addView(play);
            row.setOnClickListener(v -> openTrack(id));
            results.addView(row, rp);
        }
    }

    private void openTrack(String id) {
        if (id == null || id.isEmpty()) return;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://music.youtube.com/watch?v=" + Uri.encode(id)));
        try { startActivity(intent); }
        catch (Exception e) { Toast.makeText(this, "YouTube Music is not available", Toast.LENGTH_SHORT).show(); }
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color); d.setCornerRadius(dp(radius)); return d;
    }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }
    @Override protected void onDestroy() { super.onDestroy(); executor.shutdownNow(); }
}
