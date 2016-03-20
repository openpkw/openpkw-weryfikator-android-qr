package pl.openpkw.openpkwmobile.network;

import android.util.Log;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.json.JSONException;
import org.json.JSONObject;

import pl.openpkw.openpkwmobile.utils.StringUtils;

public class SendUserRegisterData {

    public JSONObject sendUserData(String register_url,String body){

        //init json object
        JSONObject jsonObject = null;

        //make user register request
        OAuthClientRequest oAuthRequest = null;
        try {
            oAuthRequest = OAuthClientRequest
                    .authorizationLocation(register_url)
                    .buildQueryMessage();

        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }

        //set request body JSON {"first_name":"user_first_name","last_name":"user_last_name","password":"user_password","email":"user_email"."public_key":"user_public_key_ECDSA"}
        Log.e(StringUtils.TAG,"USER REGISTER JSON: "+body);
        if (oAuthRequest != null) {
            oAuthRequest.setHeader(OAuth.HeaderType.CONTENT_TYPE, OAuth.ContentType.JSON);
            oAuthRequest.setBody(body);
        }

        //create OAuth client that uses custom http client under the hood
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        //get server resource response
        OAuthResourceResponse oAuthResourceResponse = null;
        try {
            oAuthResourceResponse = oAuthClient.resource(oAuthRequest, OAuth.HttpMethod.POST, OAuthResourceResponse.class);
            Log.e(StringUtils.TAG,"SEND USER DATA RESPONSE BODY: "+oAuthResourceResponse.getBody());
        } catch (OAuthSystemException e) {
            e.printStackTrace();
            Log.e(StringUtils.TAG, "SEND QR SYSTEM EXCEPTION: " + e.getMessage());
        } catch (OAuthProblemException e) {
            e.printStackTrace();
            Log.e(StringUtils.TAG, "SEND QR PROBLEM EXCEPTION: " + e.getMessage());
        }

        //build response JSON
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

