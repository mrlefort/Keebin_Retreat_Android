package kasper.pagh.keebin;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;

import dk.danskebank.mobilepay.sdk.CaptureType;
import dk.danskebank.mobilepay.sdk.Country;
import dk.danskebank.mobilepay.sdk.MobilePay;
import dk.danskebank.mobilepay.sdk.ResultCallback;
import dk.danskebank.mobilepay.sdk.model.FailureResult;
import dk.danskebank.mobilepay.sdk.model.Payment;
import dk.danskebank.mobilepay.sdk.model.SuccessResult;
import java.util.UUID;

public class Tester extends AppCompatActivity implements AsyncResponse
{

    Context ctx = this;


    int MOBILEPAY_PAYMENT_REQUEST_CODE = 1337;

    TextView resp;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tester);
        resp = (TextView) findViewById(R.id.mbTester);
        resp.setText("ikke startet");
        MobilePay.getInstance().init("APPDK0000000000", Country.DENMARK);

        initMP();
        Log.d("TESTER", MobilePay.getInstance().getCaptureType().toString() + " and has payment active? " + MobilePay.getInstance().hasActivePayment());


    }

    public void john(View view)
    {
        boolean isMobilePayInstalled = MobilePay.getInstance().isMobilePayInstalled(getApplicationContext());

        if (isMobilePayInstalled)
        {
            Payment payment = new Payment();
            payment.setProductPrice(new BigDecimal(10.0));
            payment.setOrderId(UUID.randomUUID().toString());
            payment.setServerCallbackUrl("https://keebin-keebin.rhcloud.com/appswitchstatus");
            Intent paymentIntent = MobilePay.getInstance().createPaymentIntent(payment);
            startActivityForResult(paymentIntent, MOBILEPAY_PAYMENT_REQUEST_CODE);
        }
        else
        {
            // MobilePay is not installed. Use the SDK to create an Intent to take the user to Google Play and download MobilePay.
            Intent intent = MobilePay.getInstance().createDownloadMobilePayIntent(getApplicationContext());
            startActivity(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, final int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        if (bundle != null)
        {
            for (String key : bundle.keySet())
            {
                Object value = bundle.get(key);
                Log.d("TESTER INTENT", String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }
        }

        if (requestCode == MOBILEPAY_PAYMENT_REQUEST_CODE)
        {
            // The request code matches our MobilePay Intent
            MobilePay.getInstance().handleResult(resultCode, data, new ResultCallback()
            {
                final

                @Override
                public void onSuccess(SuccessResult result)
                {
                    // The payment succeeded - you can deliver the product.
                    resp.setText("Amount withdrawn: " + result.getAmountWithdrawnFromCard() + "\n" +
                            "Order ID: " + result.getOrderId() + "\n" +
                            "Signature: " + result.getSignature().substring(0,10)+ "..." + "\n" +
                            "Transaction ID: " + result.getTransactionId());
                }

                @Override
                public void onFailure(FailureResult result)
                {
                    // The payment failed - show an appropriate error message to the user. Consult the MobilePay class documentation for possible error codes.
                    resp.setText("FAIL");
                }

                @Override
                public void onCancel()
                {
                    Toast.makeText(ctx, "CANCEL", Toast.LENGTH_LONG);
                    // The payment was cancelled.
                }
            });
        }
    }


    public void initMP()
    {

        MobilePay.getInstance().setCaptureType(CaptureType.RESERVE);


    }

    @Override
    public void processFinished(String output)
    {

    }
}
