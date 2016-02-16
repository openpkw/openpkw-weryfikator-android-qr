package pl.openpkw.openpkwmobile.network;

import android.util.Log;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.json.JSONException;
import org.json.JSONObject;

import pl.openpkw.openpkwmobile.utils.StringUtils;

public class SendQrData {

    public SendQrData() {
    }

    public JSONObject sendQR(String resource_url,String access_token, String body){

        //init json object
        JSONObject jsonObject = null;
        //make resource request to bearer
        OAuthClientRequest bearerClientRequest = null;

        try {
            bearerClientRequest = new OAuthBearerClientRequest(resource_url)
                .setAccessToken(access_token)
                .buildQueryMessage();
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }

        //set request body JSON {"qr":"scanned_qr","token":"access_token"}
        if (bearerClientRequest != null) {
            bearerClientRequest.setHeader(OAuth.HeaderType.CONTENT_TYPE, OAuth.ContentType.JSON);
            bearerClientRequest.setBody(body);
        }

        //create OAuth client that uses custom http client under the hood
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        //get server resource response
        OAuthResourceResponse oAuthResourceResponse = null;
        try {
            oAuthResourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.POST, OAuthResourceResponse.class);
            Log.e(StringUtils.TAG,"SEND QR RESPONSE BODY: "+oAuthResourceResponse.getBody());
        } catch (OAuthSystemException e) {
            e.printStackTrace();
            Log.e(StringUtils.TAG, "SEND QR SYSTEM EXCEPTION: " + e.getMessage());
        } catch (OAuthProblemException e) {
            e.printStackTrace();
            Log.e(StringUtils.TAG, "SEND QR PROBLEM EXCEPTION: " + e.getMessage());
        }

        //build JSON
        if(oAuthResourceResponse!=null) {
            try {
                jsonObject = new JSONObject(oAuthResourceResponse.getBody());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonObject;
    }

}
