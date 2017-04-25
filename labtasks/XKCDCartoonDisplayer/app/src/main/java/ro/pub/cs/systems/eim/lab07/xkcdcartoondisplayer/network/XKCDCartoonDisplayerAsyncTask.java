package ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.network;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.entity.BufferedHttpEntity;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.util.EntityUtils;
import ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.entities.XKCDCartoonInformation;
import ro.pub.cs.systems.eim.lab07.xkcdcartoondisplayer.general.Constants;

public class XKCDCartoonDisplayerAsyncTask extends AsyncTask<String, Void, XKCDCartoonInformation> {

    private TextView xkcdCartoonTitleTextView;
    private ImageView xkcdCartoonImageView;
    private TextView xkcdCartoonUrlTextView;
    private Button previousButton, nextButton;

    private class XKCDCartoonButtonClickListener implements Button.OnClickListener {

        private String xkcdComicUrl;

        public XKCDCartoonButtonClickListener(String xkcdComicUrl) {
            this.xkcdComicUrl = xkcdComicUrl;
        }

        @Override
        public void onClick(View view) {
            new XKCDCartoonDisplayerAsyncTask(xkcdCartoonTitleTextView, xkcdCartoonImageView, xkcdCartoonUrlTextView, previousButton, nextButton).execute(xkcdComicUrl);
        }

    }

    public XKCDCartoonDisplayerAsyncTask(TextView xkcdCartoonTitleTextView, ImageView xkcdCartoonImageView, TextView xkcdCartoonUrlTextView, Button previousButton, Button nextButton) {
        this.xkcdCartoonTitleTextView = xkcdCartoonTitleTextView;
        this.xkcdCartoonImageView = xkcdCartoonImageView;
        this.xkcdCartoonUrlTextView = xkcdCartoonUrlTextView;
        this.previousButton = previousButton;
        this.nextButton = nextButton;
    }

    @Override
    public XKCDCartoonInformation doInBackground(String... urls) {
        XKCDCartoonInformation xkcdCartoonInformation = new XKCDCartoonInformation();

        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(urls[0]);
        ResponseHandler<String> resHandler = new BasicResponseHandler();
        String content = null;

        try {
            content = httpClient.execute(httpGet, resHandler);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Cannot fetch xckd");
        }
        // TODO exercise 5a)
        // 1. obtain the content of the web page (whose Internet address is stored in urls[0])
        // - create an instance of a HttpClient object
        // - create an instance of a HttpGet object
        // - create an instance of a ResponseHandler object
        // - execute the request, thus obtaining the web page source code

        if(content == null || content.isEmpty())
            return null;

        try {
            Document doc = Jsoup.parse(content);
            Element htmlTag = doc.child(0);

            Element title = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.CTITLE_VALUE).first();
            xkcdCartoonInformation.setCartoonTitle(title.ownText());

            Element url = htmlTag.getElementsByAttributeValue(Constants.ID_ATTRIBUTE, Constants.COMIC_VALUE).first();
            String src = "http:" + url.getElementsByTag(Constants.IMG_TAG).first().attr(Constants.SRC_ATTRIBUTE);
            xkcdCartoonInformation.setCartoonUrl(src);

            // 2. parse the web page source code
            // - cartoon title: get the tag whose id equals "ctitle"
            // - cartoon url
            //   * get the first tag whose id equals "comic"
            //   * get the embedded <img> tag
            //   * get the value of the attribute "src"
            //   * prepend the protocol: "http:"
            // - cartoon bitmap (only if using Apache HTTP Components)
            //   * create the HttpGet object
            //   * execute the request and obtain the HttpResponse object
            //   * get the HttpEntity object from the response
            //   * get the bitmap from the HttpEntity stream (obtained by getContent()) using Bitmap.decodeStream() method

            httpGet = new HttpGet(src);
            HttpResponse res = null;
            try {
                res = httpClient.execute(httpGet);
            } catch (Exception e) {
                Log.e(Constants.TAG, "Cannot fetch xckd");
            }
            HttpEntity responseEntity = res.getEntity();
            BufferedHttpEntity httpEntity = null;
            try {
                httpEntity = new BufferedHttpEntity(responseEntity);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            InputStream imageStream = null;
            try {
                imageStream = httpEntity.getContent();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Bitmap bmp = BitmapFactory.decodeStream(imageStream);
            xkcdCartoonInformation.setCartoonBitmap(bmp);

            // - previous cartoon address
            //   * get the first tag whole rel attribute equals "prev"
            //   * get the href attribute of the tag
            //   * prepend the value with the base url: http://www.xkcd.com
            //   * attach the previous button a click listener with the address attached

            Element prev = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.PREVIOUS_VALUE).first();
            xkcdCartoonInformation.setPreviousCartoonUrl("http://www.xkcd.com" + prev.attr(Constants.HREF_ATTRIBUTE));
            Log.d(Constants.TAG, xkcdCartoonInformation.getPreviousCartoonUrl());
            // - next cartoon address
            //   * get the first tag whole rel attribute equals "next"
            //   * get the href attribute of the tag
            //   * prepend the value with the base url: http://www.xkcd.com
            //   * attach the next button a click listener with the address attached

            Element next = htmlTag.getElementsByAttributeValue(Constants.REL_ATTRIBUTE, Constants.NEXT_VALUE).first();
            xkcdCartoonInformation.setNextCartoonUrl("http://www.xkcd.com" + next.attr(Constants.HREF_ATTRIBUTE));
            Log.d(Constants.TAG, xkcdCartoonInformation.getNextCartoonUrl());
        } catch (NullPointerException e) {

        }

        return  xkcdCartoonInformation;
    }

    @Override
    protected void onPostExecute(final XKCDCartoonInformation xkcdCartoonInformation) {

        // TODO exercise 5b)
        // map each member of xkcdCartoonInformation object to the corresponding widget
        // cartoonTitle -> xkcdCartoonTitleTextView
        xkcdCartoonTitleTextView.setText(xkcdCartoonInformation.getCartoonTitle());
        // cartoonBitmap -> xkcdCartoonImageView (only if using Apache HTTP Components)
        xkcdCartoonImageView.setImageBitmap(xkcdCartoonInformation.getCartoonBitmap());
        // cartoonUrl -> xkcdCartoonUrlTextView
        xkcdCartoonInformation.setNextCartoonUrl(xkcdCartoonInformation.getCartoonUrl());
        // based on cartoonUrl fetch the bitmap using Volley (using an ImageRequest object added to the queue)
        // and put it into xkcdCartoonImageView
        // previousCartoonUrl, nextCartoonUrl -> set the XKCDCartoonUrlButtonClickListener for previousButton, nextButton

        nextButton.setOnClickListener(new XKCDCartoonButtonClickListener(xkcdCartoonInformation.getNextCartoonUrl()));
        previousButton.setOnClickListener(new XKCDCartoonButtonClickListener(xkcdCartoonInformation.getPreviousCartoonUrl()));
    }


}
