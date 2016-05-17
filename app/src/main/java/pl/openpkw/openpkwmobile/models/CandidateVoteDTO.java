package pl.openpkw.openpkwmobile.models;


public class CandidateVoteDTO {

    private Integer positionOnList;

    private Integer votesNumber;

    private Integer listNumber;

    private String name;

    public CandidateVoteDTO(Integer positionOnList, Integer votesNumber, Integer listNumber) {
        this.positionOnList = positionOnList;
        this.votesNumber = votesNumber;
        this.listNumber = listNumber;
    }

    public CandidateVoteDTO(Integer listNumber, Integer positionOnList, String name, Integer votesNumber) {
        this.positionOnList = positionOnList;
        this.votesNumber = votesNumber;
        this.listNumber = listNumber;
        this.name = name;
    }

    public Integer getPositionOnList() {
        return positionOnList;
    }

    public Integer getVotesNumber() {
        return votesNumber;
    }

    public Integer getListNumber() {
        return listNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVotesNumber(Integer votesNumber) {
        this.votesNumber = votesNumber;
    }
}
