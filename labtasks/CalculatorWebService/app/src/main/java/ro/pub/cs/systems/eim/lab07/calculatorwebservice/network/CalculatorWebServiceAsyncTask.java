package ro.pub.cs.systems.eim.lab07.calculatorwebservice.network;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;
import ro.pub.cs.systems.eim.lab07.calculatorwebservice.general.Constants;

public class CalculatorWebServiceAsyncTask extends AsyncTask<String, Void, String> {

    private TextView resultTextView;

    public CalculatorWebServiceAsyncTask(TextView resultTextView) {
        this.resultTextView = resultTextView;
    }

    @Override
    protected String doInBackground(String... params) {
        String operator1 = params[0];
        String operator2 = params[1];
        String operation = params[2];
        int method = Integer.parseInt(params[3]);

        // TODO exercise 4
        // signal missing values through error messages
        if (operator1 == null || operator2 == null || operation == null)
            return null;

        // create an instance of a HttpClient object

        // get method used for sending request from methodsSpinner

        // 1. GET
        // a) build the URL into a HttpGet object (append the operators / operations to the Internet address)
        // b) create an instance of a ResultHandler object
        // c) execute the request, thus generating the result
        if (method == Constants.GET_OPERATION) {
            HttpGet httpGet = new HttpGet(Constants.GET_WEB_SERVICE_ADDRESS +
                    "?" + Constants.OPERATION_ATTRIBUTE + "=" + operation +
                    "&" + Constants.OPERATOR1_ATTRIBUTE + "=" + operator1 +
                    "&" + Constants.OPERATOR2_ATTRIBUTE + "=" + operator2);

            HttpClient client = new DefaultHttpClient();
            try {
                HttpResponse res = client.execute(httpGet);
                HttpEntity ent = res.getEntity();
                if (ent != null)
                    return EntityUtils.toString(ent);

            } catch (Exception e) {
                Log.e(Constants.TAG, "GET failed");
            }
        } else if(method == Constants.POST_OPERATION) {
            // 2. POST
            HttpPost httpPost = new HttpPost(Constants.POST_WEB_SERVICE_ADDRESS);
            List<NameValuePair> pparam = new ArrayList<>();
            pparam.add(new BasicNameValuePair(Constants.OPERATION_ATTRIBUTE, operation));
            pparam.add(new BasicNameValuePair(Constants.OPERATOR1_ATTRIBUTE, operator1));
            pparam.add(new BasicNameValuePair(Constants.OPERATOR2_ATTRIBUTE, operator2));
            HttpClient client = new DefaultHttpClient();

            try {
                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(pparam, HTTP.UTF_8);
                httpPost.setEntity(urlEncodedFormEntity);

                HttpResponse res = client.execute(httpPost);
                HttpEntity ent = res.getEntity();
                if (ent != null)
                    return EntityUtils.toString(ent);
            } catch (Exception unsupportedEncodingException) {
                Log.e(Constants.TAG, unsupportedEncodingException.getMessage());
                if (Constants.DEBUG) {
                    unsupportedEncodingException.printStackTrace();
                }
            }
        }

        // a) build the URL into a HttpPost object
        // b) create a list of NameValuePair objects containing the attributes and their values (operators, operation)
        // c) create an instance of a UrlEncodedFormEntity object using the list and UTF-8 encoding and attach it to the post request
        // d) create an instance of a ResultHandler object
        // e) execute the request, thus generating the result

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        // display the result in resultTextView
        resultTextView.setText(result);
    }

}
