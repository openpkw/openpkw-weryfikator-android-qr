package pl.openpkw.openpkwmobile.loaders;

import android.app.Activity;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import pl.openpkw.openpkwmobile.models.ElectionCommitteeDTO;
import pl.openpkw.openpkwmobile.utils.Utils;

public class ElectionCommitteesDataLoader {

    final private static int ELECTION_COMMITTEE_NAME_INDEX = 0;
    final private static int ELECTION_COMMITTEE_SHORT_NAME_INDEX = 1;
    final private static int ELECTION_COMMITTEE_ADDRESS_INDEX = 2;
    final private static int ELECTION_COMMITTEE_WWW_ADDRESS_INDEX = 3;

    private Activity activity;

    public ElectionCommitteesDataLoader(Activity activity) {
        this.activity = activity;
    }

    public  HashMap<String,ElectionCommitteeDTO> getElectionCommitteesData(){
        HashMap<String,ElectionCommitteeDTO> electionCommitteeMap = new HashMap<>();
        Scanner scanner = null;
        String [] electionCommitteesData;
        String nameElectionCommittee;
        String shortNameElectionCommittee;
        String addressElectionCommittee;
        String wwwAddressElectionCommittee;
        try {
            scanner = new Scanner(activity.getResources().getAssets().open("komitety.csv"));
            while (scanner.hasNext()){
                electionCommitteesData = scanner.nextLine().split(";");
                nameElectionCommittee = electionCommitteesData[ELECTION_COMMITTEE_NAME_INDEX].replace("\"","");
                shortNameElectionCommittee = (electionCommitteesData[ELECTION_COMMITTEE_SHORT_NAME_INDEX].replace("\"",""));
                addressElectionCommittee = (electionCommitteesData[ELECTION_COMMITTEE_ADDRESS_INDEX].replace("\"",""));
                wwwAddressElectionCommittee = (electionCommitteesData[ELECTION_COMMITTEE_WWW_ADDRESS_INDEX].replace("\"",""));
                electionCommitteeMap.put(nameElectionCommittee,new ElectionCommitteeDTO(nameElectionCommittee,
                        shortNameElectionCommittee,addressElectionCommittee,wwwAddressElectionCommittee));
            }
        } catch (FileNotFoundException e) {
            Log.e(Utils.TAG,"ELECTION COMMITTEES FILE NOT FOUND: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(scanner!=null)
                scanner.close();
        }
        return electionCommitteeMap;
    }

}
