package pl.openpkw.openpkwmobile.models;


public class ElectionCommitteeDTO {

    private String name;
    private String shortName;
    private Integer listNumber;
    private String address;
    private String wwwAddress;
    private Integer totalNumberOfVotes;

    public ElectionCommitteeDTO(String name, String shortName, String address, String wwwAddress) {
        this.name = name;
        this.shortName = shortName;
        this.address = address;
        this.wwwAddress = wwwAddress;
    }

    public Integer getTotalNumberOfVotes() {
        return totalNumberOfVotes;
    }

    public void setTotalNumberOfVotes(Integer totalNumberOfVotes) {
        this.totalNumberOfVotes = totalNumberOfVotes;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWwwAddress() {
        return wwwAddress;
    }

    public void setWwwAddress(String wwwAddress) {
        this.wwwAddress = wwwAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getListNumber() {
        return listNumber;
    }

    public void setListNumber(Integer listNumber) {
        this.listNumber = listNumber;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public boolean equals(Object e){
        if (!(e instanceof ElectionCommitteeDTO )) {
            return false;
        }
        ElectionCommitteeDTO electionCommittee = (ElectionCommitteeDTO) e;
        return this.name.equals(electionCommittee.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

}
