package com.game.util;

import java.io.*;

public class FileReaderWriter {
    // method to help save the file to server
    static public void saveNewFile(File dir, String name, String content) throws FileNotFoundException {
        File myFile = null; // declare file handle
        try {
            myFile = new File(dir, name); // get the file
            if (myFile.isFile()) { // check this is a file
                myFile.delete(); // delete the file
                myFile.createNewFile(); // create a new file
            }
        } catch (IOException e) {
            throw new RuntimeException(e); // throw exception if any error
        }

        if (myFile!=null){ // check for null
            PrintWriter output = new PrintWriter(myFile); // create the print write object to write the content to the file
            output.print(content); // send the content to the file on the server
            output.close(); // close the print writer
        }
    }

    // method reads the content of the file and returns it as string
    static public String readHighScoresFile(File dir, String name) throws FileNotFoundException {
        File myFile = null; // declare file handle
        try {
            myFile = new File(dir, name); // get the file
            if (myFile.createNewFile()) { // if a new file, return empty string
                return "";
            } else {
                FileReader fileInput = new FileReader(myFile); // create file reader
                BufferedReader input = new BufferedReader(fileInput); // create buffered reader to read the file content

                // read the content line by line from the file
                StringBuffer buffer = new StringBuffer(); // create buffered string
                String line;
                while ((line = input.readLine()) != null) { // continue reading until end of file is reached
                    buffer.append(line); // store the line in buffer
                }
                return buffer.toString(); // output the contents of the buffer as string
            }
        } catch (IOException e) {
            throw new RuntimeException(e); // throw exception if any error
        }
    }
}
