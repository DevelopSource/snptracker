/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author ������ѩ�� or ���? */
public class OriginalJavaTaskAssign {

    long totalSize = 1;

    public OriginalJavaTaskAssign() {
    }

    public void work(OriginalJavaTaskBean task) throws IOException {
        File trueSavedFile = new File(task.getSaveFile());
        totalSize = getContentLength(task.getDownURL());
        if (trueSavedFile.exists()) {
            if (totalSize == trueSavedFile.length()) {
                System.out.println("The file " + trueSavedFile.getName() + " has the same size as the one to be downloaded. No need to download it!");
                return;
            } else {
                trueSavedFile.delete();
                System.out.println("The file " + trueSavedFile.getName() + " will be overwritten");
            }
        }

//whether the download is successful or not
        final AtomicBoolean success = new AtomicBoolean(true);

//task trueSavedFile
        File taskFile = new File(task.getSaveFile() + ".r_task");
//trueSavedFile being download
        File saveFile = new File(task.getSaveFile() + ".r_save");
        boolean taskFileExist = taskFile.exists();
        RandomAccessFile taskRandomFile = null;
        RandomAccessFile downRandomFile = null;
        try {
            taskRandomFile = new RandomAccessFile(taskFile, "rw");
            downRandomFile = new RandomAccessFile(saveFile, "rw");


            if (!taskFileExist) {
//if the task trueSavedFile does not exist, create a new task trueSavedFile
                task.setContentLength(totalSize);
                initTaskFile(taskRandomFile, task);
                downRandomFile.setLength(totalSize);
            } else {
// if the task trueSavedFile exists, read the download task
                task.read(taskRandomFile);
                if (task.getContentLength() != totalSize) {
                    throw new RuntimeException();
                }
            }
            int secCount = task.getSectionCount();

//create download threads, here the thread pool is used
            ExecutorService es = Executors.newFixedThreadPool(task.getWorkerCount());
            for (int i = 0; i < secCount; i++) {
                final int j = i;
                final OriginalJavaTaskBean t = task;
                final RandomAccessFile f1 = taskRandomFile;
                final RandomAccessFile f2 = downRandomFile;
                es.execute(new Runnable() {

                    public void run() {
                        try {
                            down(f1, f2, t, j);
                        } catch (IOException e) {
                            success.set(false);
                            e.printStackTrace(System.out);
                        }
                    }
                });
            }

            es.shutdown();

            try {
                es.awaitTermination(24 * 3600, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            taskRandomFile.close();
            taskRandomFile = null;
            downRandomFile.close();
            downRandomFile = null;

//if the download succeeds, delete description trueSavedFile and rename downloaded trueSavedFile
            if (success.get()) {
                taskFile.delete();
                saveFile.renameTo(trueSavedFile);
            }

        } finally {
            if (taskRandomFile != null) {
                taskRandomFile.close();
                taskRandomFile = null;
            }

            if (downRandomFile != null) {
                downRandomFile.close();
                downRandomFile = null;
            }
        }
    }

    public void down(RandomAccessFile taskRandomFile, RandomAccessFile downRandomFile, OriginalJavaTaskBean task, int sectionNo) throws IOException {
//here HttpURLConnection is employed to download the data, you can also use HttpClient or other Http protocal. but it seems not necessary
        URL u = new URL(task.getDownURL());
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        long start = task.getSectionsOffset()[sectionNo];
        long end = -1;
//note: the following code is to calculate length of current section
        if (sectionNo < task.getSectionCount() - 1) {
            long per = task.getContentLength() / task.getSectionCount();
            end = per * (sectionNo + 1);
        } else {
            end = task.getContentLength();
        }

        if (start >= end) {
            String msg = "Section has finished before. " + sectionNo;
            System.out.println(msg);
            return;
        }

        String range = "bytes=" + start + "-" + (end - 1);
        conn.setRequestProperty("Range", range);
        conn.setRequestProperty("User-Agent", "Ray-Downer");
        try {
            conn.connect();
            if (conn.getResponseCode() != 206) {
                throw new RuntimeException();
            }

            if (conn.getContentLength() != (end - start)) {
                throw new RuntimeException();
            }

            InputStream is = conn.getInputStream();

            byte[] temp = new byte[task.getBufferSize()];
            BufferedInputStream bis = new BufferedInputStream(is, temp.length);

            int readed = 0;
            while ((readed = bis.read(temp)) > 0) {
                long offset = task.getSectionsOffset()[sectionNo];
                synchronized (task) {
//once a download is finished, the descritpion will be updated. but it is not efficeint because
                    // one thread access twice i/o operations. you are suggested using buffer to increase the efficiency
                    downRandomFile.seek(offset);
                    downRandomFile.write(temp, 0, readed);
                    offset += readed;
                    task.getSectionsOffset()[sectionNo] = offset;
                    task.writeOffset(taskRandomFile);
                }
            }
            is.close();
            bis.close();
        } finally {
            conn.disconnect();
        }
        String msg = "Section finished. " + sectionNo;
        System.out.println(msg);
    }

    public void initTaskFile(RandomAccessFile taskRandomFile, OriginalJavaTaskBean task) throws IOException {
        int secCount = task.getSectionCount();
        long per = task.getContentLength() / secCount;
        long[] sectionsOffset = new long[secCount];
        for (int i = 0; i < secCount; i++) {
            sectionsOffset[i] = per * i;
        }
        task.setSectionsOffset(sectionsOffset);
        task.create(taskRandomFile);
    }

    public long getContentLength(String url) throws IOException {
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        try {
            return conn.getContentLength();
        } finally {
            conn.disconnect();
        }
    }

    public static void main(String[] args) throws IOException {





        long startTime, endTime;
        startTime = System.currentTimeMillis();
        OriginalJavaTaskBean task = new OriginalJavaTaskBean();
        task.setDownURL("http://dlc2.pconline.com.cn/filedown.jsp?dlid=53944&linkid=6444611");
        try {
            task.setSaveFile("D:/xx1.exe");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        task.setSectionCount(10);
        task.setWorkerCount(10);
        task.setBufferSize(128 * 1024);

    /*
    OriginalJavaTaskAssign ta = new OriginalJavaTaskAssign();
    ta.work(task);
    StringBuffer inforString = new StringBuffer();
    inforString.append("The Overall Lapsed Time: ");
    endTime = System.currentTimeMillis();
    inforString.append((endTime - startTime) / 1000.0);
    inforString.append(" Seconds.\n");
    System.out.println("\n" + inforString.toString());
     */
    }
}
