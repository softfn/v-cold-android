package systems.v.coldwallet.Activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Locale;

import systems.v.coldwallet.R;
import systems.v.coldwallet.Util.UIUtil;
import systems.v.coldwallet.Wallet.Account;
import systems.v.coldwallet.Wallet.Transaction;
import systems.v.coldwallet.Wallet.Wallet;

public class ConfirmTxActivity extends AppCompatActivity {
    private static final String TAG = "Winston";
    private ActionBar actionBar;
    private ConfirmTxActivity activity;

    private Account sender;
    private String recipient,assetId, feeAssetId, txId, attachment, walletStr;
    private long timestamp, amount, fee;
    private short feeScale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_tx);

        activity = this;

        Intent intent = getIntent();
        String action = intent.getStringExtra("ACTION");

        Gson gson = new Gson();
        String senderStr;
        walletStr = intent.getStringExtra("WALLET");

        String protocol = intent.getStringExtra("PROTOCOL");
        if (!Wallet.PROTOCOL.equals(protocol)) {
            Toast.makeText(activity, "Wrong QRCode is used. Invalid protocol: " + protocol, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        int api_version = intent.getIntExtra("API", 0);
        if (Wallet.API_VERSION != api_version) {
            String msg = String.format(
                    Locale.ENGLISH,
                    "Wrong API version: %d. Expected version: %d",
                    api_version,
                    Wallet.API_VERSION);
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        String op_code = intent.getStringExtra("OPC");
        if (!Transaction.OP_CODE.equals(op_code)) {
            Toast.makeText(activity, "Wrong QRCode is used. This is not transaction", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        switch (action) {
            case "PAYMENT":
                senderStr = intent.getStringExtra("SENDER");

                sender = gson.fromJson(senderStr, Account.class);
                recipient = intent.getStringExtra("RECIPIENT");
                amount= intent.getLongExtra("AMOUNT", 0);
                fee = intent.getLongExtra("FEE", 0);
                feeScale = intent.getShortExtra("FEESCALE", Short.valueOf("100"));
                attachment = intent.getStringExtra("ATTACHMENT");
                timestamp =intent.getLongExtra("TIMESTAMP", 0);

                UIUtil.setPaymentTx(activity, sender, recipient, amount,
                       fee, feeScale, attachment, timestamp);
                break;

            case "TRANSFER":
                senderStr = intent.getStringExtra("SENDER");

                sender = gson.fromJson(senderStr, Account.class);
                recipient = intent.getStringExtra("RECIPIENT");
                assetId = intent.getStringExtra("ASSET_ID");
                feeAssetId = intent.getStringExtra("FEE_ASSET_ID");
                attachment = intent.getStringExtra("ATTACHMENT");
                amount= intent.getLongExtra("AMOUNT", 0);
                fee = intent.getLongExtra("FEE", 0);
                timestamp =intent.getLongExtra("TIMESTAMP", 0);

                UIUtil.setTransferTx(activity, sender, recipient, amount,
                        assetId, fee, feeAssetId, attachment, timestamp);
                break;

            case "LEASE":
                senderStr = intent.getStringExtra("SENDER");

                sender = gson.fromJson(senderStr, Account.class);
                recipient = intent.getStringExtra("RECIPIENT");
                amount= intent.getLongExtra("AMOUNT", 0);
                fee = intent.getLongExtra("FEE", 0);
                feeScale = intent.getShortExtra("FEESCALE", Short.valueOf("100"));
                timestamp =intent.getLongExtra("TIMESTAMP", 0);

                UIUtil.setLeaseTx(activity, sender, recipient, amount, fee, feeScale, timestamp);
                break;

            case "CANCEL_LEASE":
                senderStr = intent.getStringExtra("SENDER");

                sender = gson.fromJson(senderStr, Account.class);
                txId = intent.getStringExtra("TX_ID");
                fee = intent.getLongExtra("FEE", 0);
                feeScale = intent.getShortExtra("FEESCALE", Short.valueOf("100"));
                timestamp =intent.getLongExtra("TIMESTAMP", 0);

                UIUtil.setCancelLeaseTx(activity, sender, txId, fee, feeScale, timestamp);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.custom_toolbar);
        setSupportActionBar(toolbar);

        Drawable icon = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_qr_code);
        icon.mutate();
        icon.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);

        actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setLogo(icon);
        actionBar.setTitle(R.string.title_confirm_tx);
    }

    public String getWalletStr() {
        return walletStr;
    }
}
