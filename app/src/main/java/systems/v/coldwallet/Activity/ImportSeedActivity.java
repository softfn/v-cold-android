package systems.v.coldwallet.Activity;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import systems.v.coldwallet.R;
import systems.v.coldwallet.Util.QRCodeUtil;
import systems.v.coldwallet.Util.UIUtil;
import systems.v.coldwallet.Wallet.Wallet;

public class ImportSeedActivity extends AppCompatActivity {
    private static final String TAG = "Winston";

    private ImportSeedActivity activity;
    private ActionBar actionBar;

    private TextView paste;
    private EditText input;
    private ImageView scan;
    private Button confirm;

    private String qrContents;
    private String pasteContents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_seed);

        activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);

        Drawable icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_import);
        icon.mutate();
        icon.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

        actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setLogo(icon);
        actionBar.setTitle(R.string.title_import_seed);

        paste = findViewById(R.id.import_seed_paste);
        input = findViewById(R.id.import_seed_input);
        scan = findViewById(R.id.import_seed_scan);
        confirm = findViewById(R.id.import_seed_confirm);

        paste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pasteContents != null) { input.setText(pasteContents); }
            }
        });

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRCodeUtil.scan(activity);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String seed = input.getText().toString();
                if (Wallet.validateSeedPhrase(activity, seed)) {
                    Intent intent = new Intent(activity, SetPasswordActivity.class);
                    intent.putExtra("SEED", seed);
                    startActivity(intent);
                }
                else {
                    UIUtil.createForeignSeedDialog(activity, seed);
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        qrContents = result.getContents();

        if(result != null) {
            switch (QRCodeUtil.processQrContents(qrContents)) {
                case 0:
                    Toast.makeText(activity, "Cancelled", Toast.LENGTH_LONG).show();
                    break;

                case 2:
                    String seed = QRCodeUtil.parseSeed(qrContents);
                    if (Wallet.validateSeedPhrase(activity, seed)) {
                        Intent intent = new Intent(activity, SetPasswordActivity.class);
                        intent.putExtra("SEED", seed);
                        startActivity(intent);
                    }
                    else {
                        UIUtil.createForeignSeedDialog(activity, seed);
                    }
                    break;

                case 3:
                    UIUtil.createForeignSeedDialog(activity, qrContents);
                    //Toast.makeText(activity, "Incorrect QR code format", Toast.LENGTH_LONG).show();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Retrieve paste content from clipboard if possible
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        if (!(clipboard.hasPrimaryClip())) {
            paste.setEnabled(false);
            paste.setTextColor(getResources().getColor(R.color.textLight));
        }
        else if (!(clipboard.getPrimaryClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))) {
            paste.setEnabled(false);
            paste.setTextColor(getResources().getColor(R.color.textLight));
        } else {
            paste.setEnabled(true);
            paste.setTextColor(getResources().getColor(R.color.orange));
            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
            pasteContents = item.getText().toString();
        }
    }
}
