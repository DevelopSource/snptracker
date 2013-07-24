/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cobi.util.download;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

 

/**
 *
 * @author Miaoxin Li
 */
public class OriginalJavaTaskBean {
 
    private String downURL;
    private String saveFile;
    private int bufferSize = 64 * 1024;
    private int workerCount;
    private int sectionCount;
    private long contentLength;
    private long[] sectionsOffset;
    public static final int HEAD_SIZE = 4096;

    public OriginalJavaTaskBean() {
    }

    

    //read data from the description file
    public synchronized void read(RandomAccessFile file) throws IOException {
        byte[] temp = new byte[HEAD_SIZE];
        file.seek(0);
        int readed = file.read(temp);
        if (readed != temp.length) {
            throw new RuntimeException();
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(temp);
        DataInputStream dis = new DataInputStream(bais);

        downURL = dis.readUTF();
        saveFile = dis.readUTF();
        sectionCount = dis.readInt();
        contentLength = dis.readLong();

        sectionsOffset = new long[sectionCount];
        for (int i = 0; i < sectionCount; i++) {
            sectionsOffset[i] = file.readLong();
        }
        bais.close();
        dis.close();
    }

//create description file
    public synchronized void create(RandomAccessFile file) throws IOException {
        if (sectionCount != sectionsOffset.length) {
            throw new RuntimeException();
        }

        long len = HEAD_SIZE + 8 * sectionCount;
        file.setLength(len);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(downURL);
        dos.writeUTF(saveFile);
        dos.writeInt(sectionCount);
        dos.writeLong(contentLength);
        byte[] src = baos.toByteArray();
        byte[] temp = new byte[HEAD_SIZE];
        System.arraycopy(src, 0, temp, 0, src.length);
        file.seek(0);
        file.write(temp);
        writeOffset(file);
        baos.close();
        dos.close();
    }
//update the downloading process

    public synchronized void writeOffset(RandomAccessFile file) throws IOException {
        if (sectionCount != sectionsOffset.length) {
            throw new RuntimeException();
        }

        file.seek(HEAD_SIZE);
        for (int i = 0; i < sectionsOffset.length; i++) {
            file.writeLong(sectionsOffset[i]);
        }
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getDownURL() {
        return downURL;
    }

    public void setDownURL(String downURL) {
        this.downURL = downURL;
    }

    public String getSaveFile() {
        return saveFile;
    }

    public void setSaveFile(String saveFile) throws Exception {
        this.saveFile = saveFile;
    }

    public int getSectionCount() {
        return sectionCount;
    }

    public void setSectionCount(int sectionCount) {
        this.sectionCount = sectionCount;
    }

    public long[] getSectionsOffset() {
        return sectionsOffset;
    }

    public void setSectionsOffset(long[] sectionsOffset) {
        this.sectionsOffset = sectionsOffset;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }
}
