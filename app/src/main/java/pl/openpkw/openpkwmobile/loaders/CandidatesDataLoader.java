package pl.openpkw.openpkwmobile.loaders;

import android.app.Activity;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;

import pl.openpkw.openpkwmobile.models.CandidateVoteDTO;
import pl.openpkw.openpkwmobile.utils.Utils;


public class CandidatesDataLoader {

    final private static int LIST_NUMBER_INDEX = 2;
    final private static int POSITION_ON_LIST_INDEX = 5;
    final private static int FIRST_NAME_INDEX = 7;
    final private static int LAST_NAME_INDEX = 6;
    final private static int ELECTION_COMMITTEE_NAME = 3;

    private Activity activity;

    private String districtNumber;

    public HashSet<String> electionCommitteeList = null;

    public CandidatesDataLoader(String districtNumber, Activity activity) {
        this.districtNumber = districtNumber;
        this.activity = activity;
    }

    public HashMap<String, CandidateVoteDTO> getCandidatesData()
    {
        HashMap<String, CandidateVoteDTO> candidatesMap;
        String textLine;
        Pattern pattern = Pattern.compile("(?m)\\Q" + districtNumber + "\\E.*$");
        Scanner scanner = null;
        String[] candidatesData;
        candidatesMap = new HashMap<>();
        electionCommitteeList = new HashSet<>();
        String listNumber;
        String positionOnList;
        String candidateName;
        try {
            scanner = new Scanner(activity.getResources().getAssets().open("kandydaci_sejm.csv"));

            while ((textLine = scanner.findWithinHorizon(pattern,0))!=null)
            {
                candidatesData = textLine.split(";");
                listNumber = candidatesData[LIST_NUMBER_INDEX].replace("\"","");
                positionOnList = candidatesData[POSITION_ON_LIST_INDEX].replace("\"","");
                //add unique election committee names and list number
                electionCommitteeList.add(candidatesData[ELECTION_COMMITTEE_NAME].replace("\"","")+";"+listNumber);
                //fill candidates hash map
                candidateName = candidatesData[LAST_NAME_INDEX].replace("\"","")+" "+candidatesData[FIRST_NAME_INDEX].replace("\"","");
                candidatesMap.put(listNumber+","+positionOnList,new CandidateVoteDTO(Integer.valueOf(listNumber),Integer.valueOf(positionOnList),candidateName,0));
            }

        } catch (FileNotFoundException e) {
            Log.e(Utils.TAG,"CANDIDATES FILE NOT FOUND: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(scanner!=null)
                scanner.close();
        }
        return candidatesMap;
    }

    /*
    public File getCandidatesStorageDir(String dir) {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), dir);
        if (!file.mkdirs()) {
            Log.e(Utils.TAG, "DIRECTORY NOT CREATED");
        }
        return file;
    }
    */
}
