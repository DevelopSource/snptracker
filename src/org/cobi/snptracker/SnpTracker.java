/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.snptracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import net.sf.samtools.util.CompressedFileReader;
import net.sf.samtools.util.LineReader;
import org.cobi.snptracker.entity.Options;
import org.cobi.util.net.NetUtils;

/**
 *
 * @author mxli
 */
public class SnpTracker implements Constants {
    // ********************* Parameters **********************//

    private String inputF;
    private String outputF;
    private String outputEF;
    private String chrPosFile;
    private String rsMergeFile;
    private Map<String, Integer> inputRsIdHash;
    private int tag;
    private int deleted;
    private int duplicated;
    private int unChr;

    public SnpTracker(Options option) {
        this.inputF = option.inputFileName;
        if (this.inputF.endsWith("gz")) {
            this.outputF = option.resultFileName + "result.txt.gz";
        } else {
            this.outputF = option.resultFileName + "result.txt";
        }
        this.outputEF = option.resultFileName + "error.txt";
        this.tag = option.idNum - 1;
    }

    public SnpTracker(String in) {
        this.inputF = in;
        if (this.inputF.endsWith("gz")) {
            this.outputF = "snptracker.result.txt.gz";
        } else {
            this.outputF = "snptracker.result.txt";
        }
        this.outputEF = "snptracker.error.txt";
        this.tag = 0;

    }

    public SnpTracker(String in, String out) {
        this.inputF = in;
        if (this.inputF.endsWith("gz")) {
            this.outputF = out + "result.txt.gz";
        } else {
            this.outputF = out + "result.txt";
        }
        this.outputEF = out + "error.txt";
        this.tag = 0;
    }

    public SnpTracker(String in, String column, String out) {
        this.inputF = in;
        if (this.inputF.endsWith("gz")) {
            this.outputF = out + "result.txt.gz";
        } else {
            this.outputF = out + "result.txt";
        }
        this.outputEF = out + "error.txt";
        this.tag = Integer.parseInt(column) - 1;
    }

    public String getInputFile() {
        return this.inputF;
    }

    public String getOutErrFile() {
        return this.outputEF;
    }

    public Map<String, Integer> getInputRsIdHash() {
        return inputRsIdHash;
    }

    public void setChrPosFile(String chrPosFile) {
        this.chrPosFile = chrPosFile;
    }

    public void setRsMergeFile(String rsMergeFile) {
        this.rsMergeFile = rsMergeFile;
    }

    public void setInputRsIdHash(Map<String, Integer> inputRsIdHash) {
        this.inputRsIdHash = inputRsIdHash;
    }

    public int getDeleted() {
        return deleted;
    }

    public int getDuplicated() {
        return duplicated;
    }

    public int getUnChr() {
        return unChr;
    }

    public void conversionId(String ifile,
            Map<String, String> cphash, Map<String, String> rsmhash) throws IOException, Exception {

        Map<String, Integer> iprshash = this.inputRsIdHash;
        BufferedWriter bw;
        if (this.outputF.endsWith("gz")) {
            bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(this.outputF))));
        } else {
            bw = new BufferedWriter(new FileWriter(new File(this.outputF)));
        }
        BufferedWriter bwe = new BufferedWriter(new FileWriter(new File(this.outputEF)));

        LineReader bf = new CompressedFileReader(new File(ifile));
        bw.write(bf.readLine() + "rsCurrent_Chr\trsCurrent_Pos\trsCurrent\n");

        String tmLine;
        while ((tmLine = bf.readLine()) != null) {

            String currentId;
            String addContent;
            String id;
            int tag = this.tag;

            id = tmLine.split("\t")[tag];
            if (rsmhash.containsKey(id)) {
                currentId = rsmhash.get(id);
            } else {
                currentId = id;
            }

            // judge snp have deleted or not
            if (!cphash.containsKey(currentId)) {
                this.deleted++;
                bwe.write("#Deleted\t" + tmLine + "\n");
                continue;
            }

            // judge duplicated snp and unChromosome snp
            if (cphash.get(currentId).equals("Un	1")) {
                this.unChr++;
                bwe.write("#UnChromosome\t" + tmLine + "\n");
                continue;
            }
            if (iprshash.get(currentId) > 1) {
                this.duplicated++;
                bwe.write("#DuplicatedRs\t" + tmLine + "\n");
                continue;
            }

            addContent = cphash.get(currentId) + "\t" + currentId;
            bw.write(tmLine + "\t" + addContent + "\n");
        }
        bw.close();
    }

    public Map<String, String> chrPosBackground()
            throws IOException, Exception {

        Map<String, Integer> rsIdata = this.inputRsIdHash;
        setChrPosFile(getPath("SNPChrPosOnRef"));
        String cF = this.chrPosFile;
        String tmLine;
        Map<String, String> chrPosHash = new HashMap<String, String>();
        int pos;
        LineReader bf = new CompressedFileReader(new File(cF));
        while ((tmLine = bf.readLine()) != null) {
            if (tmLine.indexOf("AltOnly") >= 0 || tmLine.indexOf("MT") >= 0 || tmLine.indexOf("Multi") >= 0 || tmLine.indexOf("NotOn") >= 0 || tmLine.indexOf("PAR") >= 0) {
                continue;
            }
            //System.out.println(tmLine);
            String[] array = tmLine.split("\t");
            if (array[2] == null) {
                continue;
            }
            String id = array[0];
            String chr = array[1];

            if (rsIdata.containsKey("rs" + id)) {
                if (chr.equals("Un")) {
                    pos = 0;
                } else {
                    pos = Integer.parseInt(array[2]);
                }
                pos++;
                chrPosHash.put("rs" + id, chr + "\t" + pos);
            } else {
                continue;
            }
        }
        bf.close();


        return chrPosHash;
    }

    public Map<String, String> rsMergeBackground() throws IOException, Exception {
        setRsMergeFile(getPath("RsMergeArch"));
        String mF = this.rsMergeFile;
        String tmLine;
        Map<String, String> mergeArchHash = new HashMap<String, String>();
        this.readInputRsId();
        Map<String, Integer> rsIdata = this.getInputRsIdHash();

        LineReader bf = new CompressedFileReader(new File(mF));
        while ((tmLine = bf.readLine()) != null) {
            String[] array = tmLine.split("\t");
            String oldId = "rs" + array[0];
            String newId = "rs" + array[6];
            if (rsIdata.containsKey(oldId)) {
                mergeArchHash.put(oldId, newId);
                if (rsIdata.containsKey(newId)) {
                    rsIdata.put(newId, 2);
                } else {
                    rsIdata.put(newId, 1);
                }
            } else {
                continue;
            }
        }
        bf.close();

        this.setInputRsIdHash(rsIdata);
        return mergeArchHash;
    }

    private String getPath(String patternString) {
        String path = null;
        for (int i = 0; i < LOCAL_FILE_PATHES.length; i++) {
            if (LOCAL_FILE_PATHES[i].indexOf(patternString) >= 0) {
                path = GlobalManager.RESOURCE_PATH + LOCAL_FILE_PATHES[i];
            }
        }
        return path;
    }

    /**
     * getInputRsId
     *
     * @return Map <String, Integer> list
     * @throws IOException
     */
    private void readInputRsId() throws IOException, Exception {

        String iF = this.inputF;
        int tag = this.tag;
        Map<String, Integer> rsId = new HashMap<String, Integer>();
        String tmLine;
        LineReader bf = new CompressedFileReader(new File(iF));
        bf.readLine();
        while ((tmLine = bf.readLine()) != null) {
            String id = tmLine.split("\t")[tag];
            rsId.put(id, 1);
        }

        this.setInputRsIdHash(rsId);
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) {
        String PVERSION = "0.01";        // 3 chars
        String PREL = " ";               // space or p (full, or prelease)
        String PDATE = "25/July/2013"; // 11 chars
        // TODO code application logic here

        long startTime = System.currentTimeMillis();
        String headInfor = "@----------------------------------------------------------@\n" + "|     snpTracker!     |     v" + PVERSION + PREL + "     |   " + PDATE + "    |\n" + "|----------------------------------------------------------|\n" + "|  (C) 2013 Jiaen Deng,  dengje@yahoo.com                  |\n" + "|  (C) 2013 Miaoxin Li,  limx54@yahoo.com                  |\n" + "|----------------------------------------------------------|\n" + "|  For documentation, citation & bug-report instructions:  |\n"
                + "|      http://statgenpro.psychiatry.hku.hk/snpTracker      |\n"
                + "@----------------------------------------------------------@";
        System.out.println(headInfor);
        Options option = new Options();
        SnpTracker rs;
        try {
            if (args.length == 1) {
                rs = new SnpTracker(args[0]);
            } else if (args.length == 2) {
                rs = new SnpTracker(args[0], args[1]);
            } else if (args.length == 3) {
                rs = new SnpTracker(args[0], args[1], args[2]);
            } else if (args.length > 1) {
                option.readOptions(args);
                option.parseOptions();
                rs = new SnpTracker(option);
            } else {
                System.err.println("Usage: java -Xmx1g -jar snptracker.jar input [column] output\n Or:    java -Xmx1g -jar snptracker.jar [options] ...");
                return;
            }

            if (!NetUtils.isConnected()) {
                String msg = "Sorry, I cannot connect to website to check the latest version!\n Please check your local network configurations!";
                System.err.println(msg);
            } else {
                if (NetUtils.needUpdate()) {
                    NetUtils.checkLibFileVersion();
                    return;
                }
            }

            String ifile = rs.getInputFile();
            String oefile = rs.getOutErrFile();

            // read Background data
            System.out.println("Reading Background....");
            Map<String, String> rsMerge = rs.rsMergeBackground();
            Map<String, String> chrPos = rs.chrPosBackground();
            System.out.println("Background has been readed!");

            // rsId conversion
            System.out.println("Trackering SNPs....");
            rs.conversionId(ifile, chrPos, rsMerge);

            // print error information
            int deleted = rs.getDeleted();
            int duplicated = rs.getDuplicated();
            int unchr = rs.getUnChr();
            BufferedWriter bwe = new BufferedWriter(new FileWriter(new File(oefile), true));
            bwe.write("There are " + deleted + " SNPs deleted\n");
            bwe.write("There are " + duplicated + " SNPs duplicated\n");
            bwe.write("There are " + unchr + " SNPs in Un chromosome\n");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("The time used is " + (System.currentTimeMillis() - startTime) / 1000 + " seconds.");
    }
}
