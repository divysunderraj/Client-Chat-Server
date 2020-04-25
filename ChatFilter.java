import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class will filter out any bad words the users say
 *
 * @author Adhi Ramkumar & Divy Sunderraj
 * @version April 25 2020
 */
public class ChatFilter {

    private String badWordsFileName;

    public ChatFilter(String badWordsFileName) {
        if (badWordsFileName == null || badWordsFileName.equals("")) {
            this.badWordsFileName = "badwords.txt";
        } else {
            this.badWordsFileName = badWordsFileName;
        }
    }

    public String filter(String msg) {
        String censored;
        String[] split = msg.split(" ");
        ArrayList<String> filteredWords = new ArrayList<>(Arrays.asList(split));
        ArrayList<String> listOfBadWords = new ArrayList<>();
        try {
            FileReader fr = new FileReader(badWordsFileName);
            BufferedReader reader = new BufferedReader(fr);
            //each bad word is stored in array
            while (true) {
                String line = reader.readLine();
                if (line == null || line.equals("")) {
                    break;
                }
                listOfBadWords.add(line);
            } //end while
            for (int i = 0; i < filteredWords.size(); i++) {
                for (int j = 0; j < listOfBadWords.size(); j++) {
                    if (filteredWords.get(i).contains(listOfBadWords.get(j))) {
                        int length = listOfBadWords.get(j).length();
                        String stars = "";
                        for (int k = 0; k < length; k++) {
                            stars += "*";
                        }
                        censored = filteredWords.get(i).replace(listOfBadWords.get(j), stars);
                        filteredWords.set(i, censored);
                    } //bad word found - must be censored
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < filteredWords.size(); i++) {
            str.append(filteredWords.get(i));
            if (i < filteredWords.size() - 1) {
                str.append(" ");
            }
        }

        msg = str.toString();

        return msg;
    }
    
}
