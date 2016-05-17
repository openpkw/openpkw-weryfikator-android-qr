package pl.openpkw.openpkwmobile.loaders;

import android.app.Activity;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

import pl.openpkw.openpkwmobile.models.PeripheryDTO;
import pl.openpkw.openpkwmobile.utils.Utils;


public class PeripheryDataLoader {

    private PeripheryDTO peripheryDTO;
    private Activity activity;

    final private static int PERIPHERY_NUMBER_INDEX = 5;
    final private static int PERIPHERY_NAME_AND_ADDRESS_INDEX = 6;

    public PeripheryDataLoader(PeripheryDTO periphery, Activity activity) {
        this.peripheryDTO = periphery;
        this.activity = activity;
    }

    public PeripheryDTO getPeripheryData()
    {
        String textLine;
        Pattern pattern = Pattern.compile("(?m)\\Q" + peripheryDTO.getTerritorialCode() + "\\E.*$");
        Scanner scanner = null;
        String[] peripheryData;
        String peripheryNumberStr;
        try {
            scanner = new Scanner(activity.getResources().getAssets().open("140000-obwody.csv"));
            while ((textLine = scanner.findWithinHorizon(pattern,0)) != null)   {
                peripheryData = textLine.split(";");
                peripheryNumberStr = peripheryData[PERIPHERY_NUMBER_INDEX].replace("\"","");
                if (peripheryNumberStr.equalsIgnoreCase(peripheryDTO.getPeripheryNumber())) {
                    int peripheryNameIndex = peripheryData[PERIPHERY_NAME_AND_ADDRESS_INDEX].indexOf(",",0);
                    peripheryDTO.setPeripheryName(peripheryData[PERIPHERY_NAME_AND_ADDRESS_INDEX].substring(1,peripheryNameIndex));
                    peripheryDTO.setPeripheryAddress(peripheryData[PERIPHERY_NAME_AND_ADDRESS_INDEX].substring(peripheryNameIndex+1,peripheryData[6].length()-1));
                    Log.e(Utils.TAG,"PERIPHERY ADDRESS: " + peripheryDTO);
                    Log.e(Utils.TAG,"PERIPHERY NAME: " + peripheryDTO.getPeripheryName());
                    break;
                }
            }

        } catch (FileNotFoundException e) {
            Log.e(Utils.TAG,"PERIPHERY FILE NOT FOUND: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(scanner!=null)
                scanner.close();
        }

        return this.peripheryDTO;
    }

}
