package pl.openpkw.openpkwmobile.qr;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pl.openpkw.openpkwmobile.models.CandidateVoteDTO;
import pl.openpkw.openpkwmobile.utils.Utils;

import static pl.openpkw.openpkwmobile.qr.QrIndex.CORRESPONDENCE_VOTING_ENVELOPESTHROWNTOBALLOTBOX;
import static pl.openpkw.openpkwmobile.qr.QrIndex.CORRESPONDENCE_VOTING_ISSUEDPACKAGES;
import static pl.openpkw.openpkwmobile.qr.QrIndex.CORRESPONDENCE_VOTING_MISSINGENVELOPEFORVOTINGCARD;
import static pl.openpkw.openpkwmobile.qr.QrIndex.CORRESPONDENCE_VOTING_MISSINGSIGNATUREONSTATEMENT;
import static pl.openpkw.openpkwmobile.qr.QrIndex.CORRESPONDENCE_VOTING_MISSINGSTATEMENT;
import static pl.openpkw.openpkwmobile.qr.QrIndex.CORRESPONDENCE_VOTING_RECEIVEDREPLYENVELOPES;
import static pl.openpkw.openpkwmobile.qr.QrIndex.CORRESPONDENCE_VOTING_UNSEALEDENVELOPE;
import static pl.openpkw.openpkwmobile.qr.QrIndex.DISTRICT_NUMBER;
import static pl.openpkw.openpkwmobile.qr.QrIndex.PERIPHERY_NUMBER;
import static pl.openpkw.openpkwmobile.qr.QrIndex.TERRITORIAL_CODE;
import static pl.openpkw.openpkwmobile.qr.QrIndex.VOTING_CARDS_CARDSFROMBALLOTBOX;
import static pl.openpkw.openpkwmobile.qr.QrIndex.VOTING_CARDS_CARDSFROMENVELOPES;
import static pl.openpkw.openpkwmobile.qr.QrIndex.VOTING_CARDS_CERTIFICATE_VOTERS;
import static pl.openpkw.openpkwmobile.qr.QrIndex.VOTING_CARDS_INVALID_CARDS;
import static pl.openpkw.openpkwmobile.qr.QrIndex.VOTING_CARDS_INVALID_VOTES;
import static pl.openpkw.openpkwmobile.qr.QrIndex.VOTING_CARDS_REGULAR_VOTERS;
import static pl.openpkw.openpkwmobile.qr.QrIndex.VOTING_CARDS_REPRESENTATIVE_VOTERS;
import static pl.openpkw.openpkwmobile.qr.QrIndex.VOTING_CARDS_TOTAL_CARDS;
import static pl.openpkw.openpkwmobile.qr.QrIndex.VOTING_CARDS_TOTAL_ENTITLED_TO_VOTE;
import static pl.openpkw.openpkwmobile.qr.QrIndex.VOTING_CARDS_UNUSED_CARDS;
import static pl.openpkw.openpkwmobile.qr.QrIndex.VOTING_CARDS_VALID_CARDS;
import static pl.openpkw.openpkwmobile.qr.QrIndex.VOTING_CARDS_VALID_VOTES;

public class QrWrapper {
    private final String[] data;

    public List<Integer> votes = new ArrayList<>();

    public QrWrapper(String codeQR) {
        this.data = codeQR.split(",");
    }

    public String getTerritorialCode() {
        return data[TERRITORIAL_CODE.getIndex()];
    }

    public String getPeripheryNumber() { return data[PERIPHERY_NUMBER.getIndex()]; }

    public String getDistrictNumber() {
        return data[DISTRICT_NUMBER.getIndex()];
    }

    public String getVotingCardsTotalEntitledToVote() { return data[VOTING_CARDS_TOTAL_ENTITLED_TO_VOTE.getIndex()]; }

    public String getVotingCardsTotalCards() { return data[VOTING_CARDS_TOTAL_CARDS.getIndex()]; }

    public String getVotingCardsUnusedCards() { return data[VOTING_CARDS_UNUSED_CARDS.getIndex()]; }
    
    public String getVotingCardsRegularVoters() { return data[VOTING_CARDS_REGULAR_VOTERS.getIndex()]; }

    public String getVotingCardsRepresentativeVoters() { return data[VOTING_CARDS_REPRESENTATIVE_VOTERS.getIndex()]; }

    public String getVotingCardsCertificateVoters() { return data[VOTING_CARDS_CERTIFICATE_VOTERS.getIndex()]; }
    
    public String getVotingCardsFromBallotBox() { return data[VOTING_CARDS_CARDSFROMBALLOTBOX.getIndex()]; }

    public String getVotingCardsFromEnvelopes() { return data[VOTING_CARDS_CARDSFROMENVELOPES.getIndex()]; }

    public String getVotingCardsInvalidCards() { return data[VOTING_CARDS_INVALID_CARDS.getIndex()]; }

    public String getVotingCardsValidCards() { return data[VOTING_CARDS_VALID_CARDS.getIndex()]; }

    public String getVotingCardsInvalidVotes() { return data[VOTING_CARDS_INVALID_VOTES.getIndex()]; }

    public String getVotingCardsValidVotes() { return data[VOTING_CARDS_VALID_VOTES.getIndex()]; }
    
    public String getCorrespondenceVotingIssuedPackages() { return data[CORRESPONDENCE_VOTING_ISSUEDPACKAGES.getIndex()]; }

    public String getCorrespondenceVotingReceivedReplyEnvelopes() { return data[CORRESPONDENCE_VOTING_RECEIVEDREPLYENVELOPES.getIndex()]; }
    
    public String getCorrespondenceVotingMissingStatement() { return data[CORRESPONDENCE_VOTING_MISSINGSTATEMENT.getIndex()]; }
    
    public String getCorrespondenceVotingMissingSignatureOnStatement() { return data[CORRESPONDENCE_VOTING_MISSINGSIGNATUREONSTATEMENT.getIndex()]; }
    
    public String getCorrespondenceVotingMissingEnvelopeForVotingCard() { return data[CORRESPONDENCE_VOTING_MISSINGENVELOPEFORVOTINGCARD.getIndex()]; }
    
    public String getCorrespondenceVotingUnsealedEnvelope() { return data[CORRESPONDENCE_VOTING_UNSEALEDENVELOPE.getIndex()]; }

    public String getCorrespondenceVotingEnvelopesThrownToBallotBox() { return data[CORRESPONDENCE_VOTING_ENVELOPESTHROWNTOBALLOTBOX.getIndex()]; }

    public List<CandidateVoteDTO> getCandidates() {
        List<CandidateVoteDTO> candidates = new ArrayList<>();
        for (int i = QrIndex.CANDIDATES_START.getIndex(); i < data.length; i++) {
            String[] candidateData = data[i].split(";");
            Integer listNumber = Integer.parseInt(candidateData[0].substring(0, 2));
            Integer positionOnList = Integer.parseInt(candidateData[0].substring(2, 4));
            candidates.add(new CandidateVoteDTO(listNumber, positionOnList, Integer.valueOf(candidateData[1])));
        }
        return candidates;
    }

    public HashMap<String,CandidateVoteDTO> getCandidatesVotes(HashMap<String,CandidateVoteDTO> candidatesMap) {

        CandidateVoteDTO candidate;
        String[] candidateData;
        String listNumber;

        for (int i = QrIndex.CANDIDATES_START.getIndex(); i < data.length; i++) {
            candidateData = data[i].split(";");
            listNumber = (candidateData[0].substring(0, 2));
            if(listNumber.indexOf("0")==0)
            {
                listNumber = listNumber.replace("0","");
            }
            String positionOnList = (candidateData[0].substring(2, 4));
            if(positionOnList.indexOf("0")==0)
            {
                positionOnList = positionOnList.replace("0","");
            }
            Integer numberOfVotes = Integer.valueOf(candidateData[1]);
            candidate = candidatesMap.get(listNumber+","+positionOnList);

            if(candidate!=null) {
                candidate.setVotesNumber(numberOfVotes);
                candidatesMap.put(listNumber + "," + positionOnList, candidate);
            }else
                Log.e(Utils.TAG,"CANDIDATE NOT FOUND -> LIST: "+listNumber+ "  POSITION: "+positionOnList);
        }

        return candidatesMap;
    }
}
