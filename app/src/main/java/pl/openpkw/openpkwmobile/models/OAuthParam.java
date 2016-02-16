package pl.openpkw.openpkwmobile.models;

/**
 * Created by Admin on 25.01.16.
 */
public class OAuthParam {
    private String loginURL;
    private String sendQrURL;
    private String id;
    private String secret;
    private String refreshToken;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getLoginURL() {
        return loginURL;
    }

    public void setLoginURL(String loginURL) {
        this.loginURL = loginURL;
    }

    public String getSendQrURL() {
        return sendQrURL;
    }

    public void setSendQrURL(String sendQrURL) {
        this.sendQrURL = sendQrURL;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
