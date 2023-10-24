package com.game.gameserver;

import com.game.util.FileReaderWriter;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// this is a class that has services, in our case, we are using this to generate unique game IDs
@WebServlet(name = "gameServlet", value = "/game-servlet")
public class GameServlet extends HttpServlet {
    //static so this set is unique
    public static Set<String> games = new HashSet<>();

    // method generates unique game codes
    public String generatingRandomUpperAlphanumericString(int length) {
        String generatedString = RandomStringUtils.randomAlphanumeric(length).toUpperCase();
        // generating unique game code
        while (games.contains(generatedString)){
            generatedString = RandomStringUtils.randomAlphanumeric(length).toUpperCase();
        }
        games.add(generatedString);

        return generatedString;
    }

    // http get method to generate and return unique 5 character game id
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");

        // send the random code as the response's content
        PrintWriter out = response.getWriter();
        out.println(generatingRandomUpperAlphanumericString(5));
    }

    // method invoked when server is shutting down or application is redeployed
    // in this method the high scores will be saved to the file
    public void destroy() {
        try {
            HighScore hs = HighScore.getInstance(); // get instance
            Map<String, Long> scores = hs.getAllScores(); // get all user scores

            // this is to generate the json string from high score
            String data = "[";
            boolean blnDelimiter = false;
            for (Map.Entry<String, Long> record:scores.entrySet()) {
                String name = record.getKey();
                Long score = record.getValue();
                if (blnDelimiter) {
                    data += ",";
                } else {
                    blnDelimiter = true;
                }
                data += "{";
                data += "\"name\": \"" + name + "\",";
                data += "\"score\": \"" + score.longValue() + "\"";
                data += "}";
            }
            data += "]";

            // save the json string to a json file on the server
            URL url = this.getClass().getClassLoader().getResource(Constants.HIGH_SCORE_DIR);
            File mainDir = new File(url.toURI());
            FileReaderWriter.saveNewFile(mainDir,Constants.HIGH_SCORE_FILE,data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}