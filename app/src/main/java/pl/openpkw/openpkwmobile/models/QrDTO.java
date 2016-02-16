package pl.openpkw.openpkwmobile.models;

/**
 * Created by Admin on 08.12.15.
 */
public class QrDTO {
    private String qr;
    private String token;
    private String sign;

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }
}
