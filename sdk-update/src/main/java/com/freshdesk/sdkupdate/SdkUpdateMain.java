package com.freshdesk.sdkupdate;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.wiztools.appupdate.Version;

public class SdkUpdateMain {

    public static void main(String[] args) {
        List<Rollbackable> rollbackables = new ArrayList<>();
        
        try {
            System.out.println("New version availability check...");
            WsUtil wsu = new WsUtil(Constants.VER_WS_ENDPT);

            Version current = SdkUtil.getCurrentVersion(Constants.SDK_DIR);
            Version latest = wsu.getLatest();

            if(current.isLessThan(latest)) {
                System.out.println("New version available: " + latest + ". Downloading...");
                DownloadUtil dlu = new DownloadUtil(wsu.getDlUrl());
                rollbackables.add(dlu);

                File downloaded = dlu.download();
                System.out.println("File downloaded. Installing...");
                InstallUtil inu = new InstallUtil(downloaded, Constants.SDK_DIR);
                rollbackables.add(inu);
                inu.install();
            }
            else {
                System.out.println("Already at latest version. Quitting...");
            }
        }
        catch(SdkUpdateException ex) {
            // Rollbacks should happen in reverse order:
            Collections.reverse(rollbackables);
            
            // Now rollback:
            rollbackables.stream().forEach((r) -> {
                try {
                    r.rollback();
                }
                catch(SdkUpdateException ex1) {
                    System.err.println("Error while rollbacking...");
                    ex1.printStackTrace(System.err);
                }
            });
        }
    }
}
