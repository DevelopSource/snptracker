/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.snptracker.entity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.cobi.snptracker.Constants;

/**
 *
 * @author mxli
 */
public class Options implements Constants {

    String[] options;
    int optionNum;
    public int idNum;
    public String inputFileName = null;
    public String resultFileName = null;
    public boolean noWeb = false;

    private int find(String opp) {
        for (int i = 0; i < optionNum; i++) {
            if (options[i].equals(opp)) {
                return i;
            }
        }
        return -1;
    }

    public boolean parseOptions() throws Exception {
        int id = -1;
        StringBuilder param = new StringBuilder();

        id = find("--no-web");
        if (id >= 0) {
            noWeb = true;
        } else {
            String info = "To disable web checking, use --no-web";
            System.out.println(info);
        }

        /*
         * Commands
         */

        id = find("--rsid-column");
        if (id >= 0) {
            idNum = Integer.parseInt(options[id + 1]);
            param.append("--rsid-column");
            param.append(' ');
            param.append(idNum);
            param.append('\n');
        } else {
            String infor = "No --rsid-column option to identify id column in data! Default --rsid-column 1";
            idNum = 1;
            //return false;
        }

        id = find("--in");
        if (id >= 0) {
            inputFileName = options[id + 1];
            param.append("--in");
            param.append(' ');
            param.append(inputFileName);
            param.append('\n');

        } else {
            String infor = "No --in option to read input data!";
            throw new Exception(infor);
            //return false;
            //resultFileName = "snpTracker";
        }

        id = find("--out");
        if (id >= 0) {
            resultFileName = options[id + 1];
            param.append("--out");
            param.append(' ');
            param.append(resultFileName);
            param.append('\n');

        } else {
            String infor = "No --out option to save result data!";
            //throw new Exception(infor);
            //return false;
            resultFileName = "snpTracker";
        }


        System.out.println("Effective settings :");
        System.out.println(param);
        return true;
    }

    public void readOptions(String fileName) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line = "";
        int lineNumber = 0;
        StringBuilder tmpStr = new StringBuilder();
        List<String> optionList = new ArrayList<String>();
        //assume every parameter has a line
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                continue;
            }
            if (line.startsWith("//")) {
                continue;
            }
            int index = line.indexOf("//");
            if (index >= 0) {
                line = line.substring(0, index);
            }
            //  System.out.println(line);
            StringTokenizer tokenizer = new StringTokenizer(line);
            //sometimes tokenizer.nextToken() can not release memory

            while (tokenizer.hasMoreTokens()) {
                //parameter Name value
                optionList.add(tmpStr.append(tokenizer.nextToken().trim()).toString());
                tmpStr.delete(0, tmpStr.length());
            }
            lineNumber++;
        }
        br.close();
        optionNum = optionList.size();
        options = new String[optionNum];
        for (int i = 0; i < optionNum; i++) {
            options[i] = optionList.get(i);
        }
    }

    public void readOptions(String[] args) throws Exception {
        optionNum = args.length;
        options = new String[optionNum];
        System.arraycopy(args, 0, options, 0, optionNum);
    }
}
