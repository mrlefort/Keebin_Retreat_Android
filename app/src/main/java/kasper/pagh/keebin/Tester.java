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
import rest.houseKeeping.GetCertificate;

public class Tester extends AppCompatActivity implements AsyncResponse
{

    Context ctx = this;


    int MOBILEPAY_PAYMENT_REQUEST_CODE = 1337;

    TextView resp;
    TextView link;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tester);
        resp = (TextView) findViewById(R.id.mbTester);
        link = (TextView) findViewById(R.id.mbTesterLink);
        resp.setText("ikke startet");
        link.setVisibility(View.INVISIBLE);
        initMP();


    }

    public void john(View view)
    {
        boolean isMobilePayInstalled = MobilePay.getInstance().isMobilePayInstalled(getApplicationContext());

        if (isMobilePayInstalled)
        {
            Payment payment = new Payment();

            payment.setProductPrice(new BigDecimal(10.0));
            payment.setOrderId("tissueZone");

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
        if (requestCode == MOBILEPAY_PAYMENT_REQUEST_CODE)
        {
            // The request code matches our MobilePay Intent
            MobilePay.getInstance().handleResult(resultCode, data, new ResultCallback()
            {
                @Override
                public void onSuccess(SuccessResult result)
                {
                    // The payment succeeded - you can deliver the product.
                    resp.setText(result + "");
                }

                @Override
                public void onFailure(FailureResult result)
                {
                    link.setVisibility(View.INVISIBLE);
                    switch (result.getErrorCode())
                    {
                        case 1:
                            resp.setText("Something went wrong. Please try updating your app.");
                            break;
                        case 2:
                            resp.setText("Could not connect, please check ur internet connection.");
                            break;
                        case 3:
                            resp.setText("Your MobilePay is out of date, plz use the link to update it.");
                            link.setVisibility(View.VISIBLE);
                            link.setText(R.string.mobilepayGoogle);
                            break;
                        case 4:
                            resp.setText("Something went wrong. Please try updating your app using the link below.");
                            link.setVisibility(View.VISIBLE);
                            link.setText(R.string.keebinGooglePlay);
                            break;
                        case 5:
                            resp.setText("Something went wrong. Please try updating your app using the link below.");
                            link.setVisibility(View.VISIBLE);
                            link.setText(R.string.keebinGooglePlay);
                            break;
                        case 6:
                            resp.setText("Your payment has times out, please try again.");
                            break;
                        case 7:
                            resp.setText("You have exeeded your pay limit, you can view your limits under 'beløbsgrænser' on MobilePay app");
                            break;
                        case 8:
                            resp.setText("Your payment has timed out, please try again.");
                            break;
                        case 9:
                            resp.setText("Something went wrong try again later.");
                            break;
                        case 10:
                            resp.setText("Your app is out of date, please update using link below");
                            link.setVisibility(View.VISIBLE);
                            link.setText(R.string.keebinGooglePlay);
                            break;
                        case 11:
                            resp.setText("Order has already been sent, check for confirmation or try again later.");
                            break;
                        case 12:
                            resp.setText("Your payment have been rejectred due to suspicious behaviour. Please contact MobilePay to resolve the issue.");
                            break;
                        default:
                            resp.setText("something went wrong. Please try again later.");
                            break;
                    }

                }

                @Override
                public void onCancel()
                {
                    resp.setText("Your payment has been canceled, but you can keep shopping.");
                    // The payment was cancelled.
                }
            });
        }
    }


    public void initMP()
    {
        MobilePay.getInstance().init("APPDK0000000000", Country.DENMARK);
        MobilePay.getInstance().setCaptureType(CaptureType.RESERVE);
        MobilePay.getInstance().setTimeoutSeconds(4);
    }

    @Override
    public void processFinished(String output)
    {

    }
}
