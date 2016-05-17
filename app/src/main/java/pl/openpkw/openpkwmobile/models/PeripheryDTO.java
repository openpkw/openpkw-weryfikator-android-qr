package pl.openpkw.openpkwmobile.models;


public class PeripheryDTO {

    private String peripheryNumber;
    private String territorialCode;
    private String peripheryAddress;
    private String peripheryName;


    public PeripheryDTO(String peripheryNumber, String territorialCode) {
        this.peripheryNumber = peripheryNumber;
        this.territorialCode = territorialCode;
    }

    public String getPeripheryNumber() {
        return peripheryNumber;
    }

    public String getPeripheryAddress() {
        return peripheryAddress;
    }

    public void setPeripheryAddress(String peripheryAddress) {
        this.peripheryAddress = peripheryAddress;
    }

    public String getPeripheryName() {
        return peripheryName;
    }

    public void setPeripheryName(String peripheryName) {
        this.peripheryName = peripheryName;
    }

    public String getTerritorialCode() {
        return territorialCode;
    }

}
