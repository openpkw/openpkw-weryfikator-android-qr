package pl.openpkw.openpkwmobile.qr;

import java.util.concurrent.atomic.AtomicReference;

import static pl.openpkw.openpkwmobile.qr.QrIndex.*;

public class QrValidator {

    public static boolean isCorrectQR(String qr){
        String [] splitQr = qr.split(",");

        if(TERRITORIAL_CODE.getIndex()>splitQr.length)
            return false;
        else
        {
            if(!isInteger(splitQr[TERRITORIAL_CODE.getIndex()]))
                return false;
        }

        if(PERIPHERY_NUMBER.getIndex()>splitQr.length)
            return false;
        else
        {
            if(!isInteger(splitQr[PERIPHERY_NUMBER.getIndex()]))
                return false;
        }

        if(DISTRICT_NUMBER.getIndex()>splitQr.length)
            return false;
        else
        {
            if(!isInteger(splitQr[DISTRICT_NUMBER.getIndex()]))
                return false;
        }

        if(VOTING_CARDS_TOTAL_ENTITLED_TO_VOTE.getIndex()>splitQr.length)
            return false;
        else
        {
            if(!isInteger(splitQr[VOTING_CARDS_TOTAL_ENTITLED_TO_VOTE.getIndex()]))
                return false;
        }

        if(VOTING_CARDS_TOTAL_CARDS.getIndex()>splitQr.length)
            return false;
        else
        {
            if(!isInteger(splitQr[VOTING_CARDS_TOTAL_CARDS.getIndex()]))
                return false;
        }

        if(VOTING_CARDS_VALID_CARDS.getIndex()>splitQr.length)
            return false;
        else
        {
            if(!isInteger(splitQr[VOTING_CARDS_VALID_CARDS.getIndex()]))
                return false;
        }

        if(VOTING_CARDS_INVALID_VOTES.getIndex()>splitQr.length)
            return false;
        else
        {
            if(!isInteger(splitQr[VOTING_CARDS_INVALID_VOTES.getIndex()]))
                return false;
        }

        if(VOTING_CARDS_VALID_VOTES.getIndex()>splitQr.length)
            return false;
        else
        {
            if(!isInteger(splitQr[VOTING_CARDS_VALID_VOTES.getIndex()]))
                return false;
        }

        return true;
    }

    public static boolean isInteger(String str)
    {
        try
        {
            AtomicReference<Integer> integer = new AtomicReference<>();
            integer.set(Integer.parseInt(str));
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }
}
