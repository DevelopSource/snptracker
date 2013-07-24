/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.net;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import org.cobi.snptracker.Constants;
import static org.cobi.snptracker.Constants.LOCAL_FILE_PATHES;
import static org.cobi.snptracker.Constants.URL_FILE_PATHES;
import static org.cobi.snptracker.Constants.URL_FOLDER;
import org.cobi.snptracker.GlobalManager;
import org.cobi.util.download.OriginalJavaDownloadTask;
import org.cobi.util.download.OriginalJavaTaskBean;
import org.cobi.util.thread.TaskQueue;
import org.cobi.util.thread.ThreadPool;

/**
 *
 * @author mxli
 */
public class NetUtils implements Constants {

    public static void updateLocal() throws Exception {
        for (int i = 0; i < LOCAL_FILE_PATHES.length; i++) {
            File copiedFile = new File(GlobalManager.LOCAL_COPY_FOLDER + File.separator + LOCAL_FILE_PATHES[i]);
            File targetFile = new File(GlobalManager.LOCAL_FOLDER + File.separator + LOCAL_FILE_PATHES[i]);
            //a file with size less than 1k is not normal
            if (copiedFile.length() > 1024 && copiedFile.length() != targetFile.length() && LOCAL_FILE_PATHES[i].indexOf("jar") >= 0) {
                copyFile(targetFile, copiedFile);
            }
        }
    }

    public static void copyFile(File targetFile, File sourceFile) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            if (!sourceFile.exists()) {
                return;
            }

            if (targetFile.exists()) {
                if (!targetFile.delete()) {
                    targetFile.deleteOnExit();
                    //System.err.println("Cannot delete " + targetFile.getCanonicalPath());
                }
            }
            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            in = new FileInputStream(sourceFile);
            out = new FileOutputStream(targetFile);

            byte[] buffer = new byte[1024 * 5];
            int size;
            while ((size = in.read(buffer)) != -1) {
                out.write(buffer, 0, size);
                out.flush();
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
        }
    }

    public static boolean isConnected() {
        String url = URL_FOLDER + URL_FILE_PATHES[0];

        URLConnection urlconn = null;
        try {
            URL u = new URL(url);
            urlconn = u.openConnection();
            urlconn.setConnectTimeout(1000);
            urlconn.connect();
            /*
             TimedUrlConnection timeoutconn = new TimedUrlConnection(urlconn, 5000);//time   out:   100seconds
             boolean bconnectok = timeoutconn.connect();
             if (bconnectok == false) {
             //urlconn   fails   to   connect   in   100seconds
             return false;
             } else {
             //connect   ok
             return true;
             }
             *
             */
            return true;
        } catch (SocketTimeoutException ex) {
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (ConnectException ex) {
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (MalformedURLException ex) {
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            // Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
        }

    }

    public static long getContentLength(String url) throws IOException {
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        try {
            return conn.getContentLength();
        } finally {
            conn.disconnect();
        }
    }

    public static boolean needUpdate() throws Exception {
        for (int i = 0; i < LOCAL_FILE_PATHES.length; i++) {
            File newLibFile;
            if (LOCAL_FILE_PATHES[i].indexOf("jar") >= 0) {
                newLibFile = new File(GlobalManager.LOCAL_COPY_FOLDER + File.separator + LOCAL_FILE_PATHES[i]);
            } else {
                newLibFile = new File(GlobalManager.RESOURCE_PATH + File.separator + LOCAL_FILE_PATHES[i]);
            }
            if (newLibFile.exists()) {
                long fileSize = newLibFile.length();
                String url = URL_FOLDER + URL_FILE_PATHES[i];

                long netFileLen = getContentLength(url);
                if (netFileLen <= 1024) {
                    return false;
                }
                if (fileSize != netFileLen) {
                    return true;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    /*
     //check previously downloaded resources
     public boolean updateShellVersion() throws Exception {
     File localFile = new File(GlobalManager.LOCAL_COPY_FOLDER + File.separator + SHELL_NAME);
     String urlFile = URL_FOLDER + SHELL_NAME;
     if (localFile.exists()) {
     long fileSize = localFile.length();
     OriginalJavaTaskAssign taskAssign = new OriginalJavaTaskAssign();
     long netFileLen = taskAssign.getContentLength(urlFile);
     if (netFileLen <= 0) {
     return false;
     }
     if (fileSize == netFileLen) {
     return false;
     }
     }
    
     OriginalJavaTaskAssign downloadTask = new OriginalJavaTaskAssign();
     System.out.println("Updating Shell ...");
     OriginalJavaTaskBean taskBean = new OriginalJavaTaskBean();
    
     taskBean.setDownURL(urlFile);
     taskBean.setSectionCount(FILE_SEGEMENT_NUM);
     taskBean.setWorkerCount(FILE_SEGEMENT_NUM);
     taskBean.setBufferSize(128 * 1024);
     taskBean.setSaveFile(localFile.getCanonicalPath());
     downloadTask.work(taskBean);
    
     String infor = "The shell has been updated.\n1. Please exit now and remove " + localFile.getCanonicalPath()
     + "\n2. Rename \'" + localFile.getCanonicalPath() + ".r_save\' as \'" + localFile.getCanonicalPath() + "\'"
     + "\'.";
     System.out.println(infor);
     return true;
     }
     */
    public static void checkLibFileVersion() throws Exception {

        List<String> updatedLocalFiles = new ArrayList<String>();

        for (int i = 0; i < LOCAL_FILE_PATHES.length; i++) {
            File newLibFile;
            if (LOCAL_FILE_PATHES[i].indexOf("jar") >= 0) {
                newLibFile = new File(GlobalManager.LOCAL_COPY_FOLDER + File.separator + LOCAL_FILE_PATHES[i]);
            } else {
                newLibFile = new File(GlobalManager.RESOURCE_PATH + File.separator + LOCAL_FILE_PATHES[i]);
            }

            if (!newLibFile.exists()) {
                updatedLocalFiles.add(LOCAL_FILE_PATHES[i]);
            } else {
                long fileSize = newLibFile.length();
                String url = URL_FOLDER + URL_FILE_PATHES[i];

                long netFileLen = getContentLength(url);
                if (netFileLen <= 0) {
                    updatedLocalFiles.add(LOCAL_FILE_PATHES[i]);
                }
                if (fileSize != netFileLen) {
                    updatedLocalFiles.add(LOCAL_FILE_PATHES[i]);
                }
            }
        }

        if (!updatedLocalFiles.isEmpty()) {
            System.out.println("Updating Resource...");
            int filesNum = updatedLocalFiles.size();
            for (int i = 0; i < filesNum; i++) {
                File newLibFile = new File(GlobalManager.RESOURCE_PATH + File.separator + updatedLocalFiles.get(i));
                File newJar = new File(GlobalManager.LOCAL_COPY_FOLDER + File.separator + updatedLocalFiles.get(i));
                File libFolder = newLibFile.getParentFile();
                if (!libFolder.exists()) {
                    libFolder.mkdirs();
                }

                String url = URL_FOLDER + updatedLocalFiles.get(i);
                if (updatedLocalFiles.get(i).indexOf("jar") > 0) {
                    downloadResource(url, newJar);
                } else {
                    downloadResource(url, newLibFile);
                }
            }
            System.out.println("The resource of has been updated! Please re-initiate this application!");
            updateLocal();
        }
    }

    public static void downloadResource(String url, File localDest) throws Exception {
        System.out.println("Downloading " + localDest.getName() + "...");

        TaskQueue queue = new TaskQueue();
        OriginalJavaTaskBean task = new OriginalJavaTaskBean();
        task.setDownURL(url);
        task.setSectionCount(20);
        task.setWorkerCount(20);
        task.setBufferSize(128 * 1024);

        File libFolder = localDest.getParentFile();
        if (!libFolder.exists()) {
            libFolder.mkdirs();
        }
        task.setSaveFile(localDest.getCanonicalPath());
        queue.putTask(new OriginalJavaDownloadTask(task));

        ThreadPool pool = new ThreadPool(queue);
        //launtch two thread to do these taskBean
        for (int i = 0; i < 1; i++) {
            pool.addWorkerThread();
        }
        pool.jointAllWorkerThread();
    }
}
