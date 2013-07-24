/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cobi.util.download;

import org.cobi.util.thread.Task;

/**
 *
 * @author Miaoxin Li
 */
public class OriginalJavaDownloadTask implements Task {

    private static int count = 0;
    private int num = count;
    private OriginalJavaTaskBean taskBean;

    public OriginalJavaDownloadTask(OriginalJavaTaskBean task) {
        count++;
        this.taskBean = task;
    }

    @Override
    public void execute() {
       // System.out.println("[Download " + num + "] start...");
        try {
            OriginalJavaTaskAssign ta = new OriginalJavaTaskAssign();
            ta.work(taskBean);
        } catch (Exception ie) {
        }
       // System.out.println("[Download " + num + "] done.");

    }
}