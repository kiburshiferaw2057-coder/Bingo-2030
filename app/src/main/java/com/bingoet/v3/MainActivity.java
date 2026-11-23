package com.bingoet.v3;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "BingoEtPrefs";
    private static final String KEY_LANG = "lang";
    private static final String KEY_BALANCE_BIRR = "balance_birr";
    private static final long ROUND_DURATION_MS = 120_000L; // 2 minutes

    private double balanceBirr = 100.00;
    private int selectedCardNumber = 1;
    private int linesToWin = 5;

    private TextView tvBalanceBirr;
    private TextView tvCountdown;
    private TextView tvGameModeStatus;
    private EditText etCardNumber;
    private CountDownTimer roundTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedLang = prefs.getString(KEY_LANG, "en");
        applyLocale(this, savedLang);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        balanceBirr = Double.longBitsToDouble(
                prefs.getLong(KEY_BALANCE_BIRR, Double.doubleToLongBits(100.00))
        );

        tvBalanceBirr = findViewById(R.id.tvBalanceBirr);
        tvCountdown = findViewById(R.id.tvCountdown);
        tvGameModeStatus = findViewById(R.id.tvGameModeStatus);
        etCardNumber = findViewById(R.id.etCardNumber);

        View alarmIcon = findViewById(R.id.ivCountdownAlarm);
        if (alarmIcon != null) {
            alarmIcon.setVisibility(View.GONE);
        }

        View payment80Metric = findViewById(R.id.tvPayment80Percent);
        if (payment80Metric != null) {
            payment80Metric.setVisibility(View.GONE);
        }

        setupLanguageSpinner(savedLang);
        setupGameModeButtons();
        setupCardSelection();
        refreshBalanceDisplay();
        startTwoMinuteRoundTimer();
    }

    private void setupLanguageSpinner(String currentLang) {
        Spinner spinnerLang = findViewById(R.id.spinnerLanguage);
        String[] labels = {
                getString(R.string.lang_en),
                getString(R.string.lang_am),
                getString(R.string.lang_om)
        };
        final String[] codes = {"en", "am", "om"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, labels
        );
        spinnerLang.setAdapter(adapter);

        int initialIndex = 0;
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(currentLang)) {
                initialIndex = i;
                break;
            }
        }
        spinnerLang.setSelection(initialIndex, false);

        spinnerLang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String chosenCode = codes[position];
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                if (!chosenCode.equals(prefs.getString(KEY_LANG, "en"))) {
                    prefs.edit().putString(KEY_LANG, chosenCode).apply();
                    recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setupGameModeButtons() {
        Button btnClassic = findViewById(R.id.btnModeClassic);
        Button btnShort3Line = findViewById(R.id.btnModeShort3Line);

        btnClassic.setOnClickListener(v -> {
            linesToWin = 5;
            tvGameModeStatus.setText(getString(R.string.mode_classic));
        });

        btnShort3Line.setOnClickListener(v -> {
            linesToWin = 3;
            tvGameModeStatus.setText(getString(R.string.mode_short_3_line));
        });

        tvGameModeStatus.setText(getString(R.string.mode_classic));
    }

    private void setupCardSelection() {
        Button btnSelectCard = findViewById(R.id.btnSelectCard);
        btnSelectCard.setOnClickListener(v -> {
            String raw = etCardNumber.getText().toString().trim();
            if (raw.isEmpty()) {
                Toast.makeText(this, "1 - 600", Toast.LENGTH_SHORT).show();
                return;
            }
            int num = Integer.parseInt(raw);
            if (num < 1 || num > 600) {
                Toast.makeText(this, "Card number must be between 1 and 600", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedCardNumber = num;
            Toast.makeText(this, "BINGO ET Card #" + selectedCardNumber + " (" + linesToWin + " lines)", Toast.LENGTH_SHORT).show();
        });
    }

    private void refreshBalanceDisplay() {
        tvBalanceBirr.setText(String.format(Locale.getDefault(), getString(R.string.balance_birr_fmt), balanceBirr));
    }

    private void startTwoMinuteRoundTimer() {
        if (roundTimer != null) {
            roundTimer.cancel();
        }
        roundTimer = new CountDownTimer(ROUND_DURATION_MS, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long totalSec = millisUntilFinished / 1000L;
                long mins = totalSec / 60L;
                long secs = totalSec % 60L;
                tvCountdown.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("00:00");
                startTwoMinuteRoundTimer();
            }
        }.start();
    }

    private void applyLocale(Context context, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roundTimer != null) {
            roundTimer.cancel();
        }
    }
}
