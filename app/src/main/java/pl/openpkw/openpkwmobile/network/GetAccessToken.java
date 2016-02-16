package pl.openpkw.openpkwmobile.network;

import android.util.Log;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.json.JSONException;
import org.json.JSONObject;

import pl.openpkw.openpkwmobile.utils.StringUtils;

/**
 * Created by Admin on 14.01.16.
 */
public class GetAccessToken {

    public GetAccessToken() {
    }

    public JSONObject getToken(String token_url,String client_id,
                               String client_secret,String refresh_token){

        //init json object
        JSONObject jsonObject = null;

        //make access token request
        OAuthClientRequest oAuthRequest = null;
        try {
            oAuthRequest = OAuthClientRequest
                    .tokenLocation(token_url)
                    .setGrantType(GrantType.REFRESH_TOKEN)
                    .setRefreshToken(refresh_token)
                    .setClientId(client_id)
                    .buildBodyMessage();

        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }

        //add basic authentication
        if (oAuthRequest != null) {
            oAuthRequest.addHeader(OAuth.HeaderType.AUTHORIZATION,
                    NetworkUtils.base64EncodedBasicAuthentication(client_id, client_secret));
        }
        //create OAuth client that uses custom http client under the hood
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        //get access token
        OAuthAccessTokenResponse oauthTokenResponse = null;
        try {
            oauthTokenResponse = oAuthClient.accessToken(oAuthRequest);

        } catch (OAuthSystemException e) {
            e.printStackTrace();
            Log.e(StringUtils.TAG, "SYSTEM EXCEPTION ACCESS TOKEN: " + e.getMessage());
        } catch (OAuthProblemException e) {
            e.printStackTrace();
            Log.e(StringUtils.TAG,"PROBLEM EXCEPTION ACCESS TOKEN: "+e.getMessage());
            try {
                jsonObject = new JSONObject();
                jsonObject.put("error", e.getMessage());
                return jsonObject;
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

        //build JSON
        if(oauthTokenResponse!=null) {
            try {
                jsonObject = new JSONObject(oauthTokenResponse.getBody());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return jsonObject;
    }
}
