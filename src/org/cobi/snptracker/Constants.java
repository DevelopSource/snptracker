/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.snptracker;

/**
 *
 * @author mxli
 */
public interface Constants {
    public static char MISSING_ALLELE_NAME = 'X';
    public static char MISSING_STRAND_NAME = '0';
    public static char DEFAULT_MISSING_GTY_NAME = '0';
    public static final String[] CHROM_NAMES = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14",
        "15", "16", "17", "18", "19", "20", "21", "22", "X", "Y", "XY", "MT"
    };
   

    static int FILE_SEGEMENT_NUM = 5;
    static int MAX_THREAD_NUM = 1;
    static String URL_FOLDER = "http://statgenpro.psychiatry.hku.hk/limx/snptracker/download/";
    static String[] LOCAL_FILE_PATHES = {"SnpTracker.jar","RsMergeArch.bcp.gz", "b137_SNPChrPosOnRef.bcp.gz"};
    static String[] URL_FILE_PATHES = {"SnpTracker.jar","RsMergeArch.bcp.gz", "b137_SNPChrPosOnRef.bcp.gz"};
}
