package fr.lepetitpingouin.android.gks;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class DonateActivity extends Activity {

    TextView amount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.donate);

        amount = (TextView)findViewById(R.id.donate_amount);
    }

    public void onIncreaseClick(View v) {
        int amnt = Integer.valueOf(amount.getText().toString());
        amount.setText(String.valueOf(++amnt));
    }

    public void onDecreaseClick(View v) {
        int amnt = Integer.valueOf(amount.getText().toString());
        if(amnt > 1)
            amount.setText(String.valueOf(--amnt));
    }

    public void onDonateClick(View v) {

        String amountValue = amount.getText().toString()+"%2e00";
        Intent i = new Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=meyergre%40gmail%2ecom&lc=FR&item_name=t411%2dcompanion&amount="
                        + amountValue
                        + "&currency_code=EUR&currency_code=EUR&bn=PP%2dDonationsBF%3abtn_donate_LG%2egif%3aNonHosted"));
        startActivity(i);
    }

    public void onCancelClick(View v) {
        finish();
    }
}
