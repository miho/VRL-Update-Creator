/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.update;

import eu.mihosoft.vrl.annotation.ComponentInfo;
import eu.mihosoft.vrl.annotation.ParamInfo;
import eu.mihosoft.vrl.io.IOUtil;
import eu.mihosoft.vrl.system.VMessage;
import eu.mihosoft.vrl.visual.Message;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
@ComponentInfo(name = "FTPFileUploader", category = "VRL/Net")
public class FTPFileUploader implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    
    private int bufferSize = 4096*16;

    // add your code here
    public void upload(
            @ParamInfo(name = "Username", style = "default", options = "") String user,
            @ParamInfo(name = "Password", style = "default", options = "") String pass,
            @ParamInfo(name = "Server", style = "default", options = "") String server,
            @ParamInfo(name = "Remote Location", style = "default", options = "") String remoteLocation,
            @ParamInfo(name = "", style = "load-dialog", options = "") File content) {
        int port = 21;

        FTPClient ftpClient = new FTPClient();
        try {

            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            String remoteFile = remoteLocation + "/" + content.getName();
            FileInputStream inputStream = new FileInputStream(content);

            System.out.println(">> start uploading file " + content.getName() + " to " + server);
            OutputStream outputStream = ftpClient.storeFileStream(remoteFile);

            long fileSize = IOUtil.getFileSize(content);
            
            byte[] bytesIn = new byte[bufferSize];
            int bytesRead = 0;
            
            long totalRead = 0;

            while ((bytesRead = inputStream.read(bytesIn)) != -1) {
                outputStream.write(bytesIn, 0, bytesRead);
                
                totalRead+=bytesRead;
                
                String percentageMSG = " --> uploading " + content + " " + ((float)totalRead/(float)fileSize * 100.f) + " %";
                
                System.out.println(percentageMSG);
                
                Message m = VMessage.info("Uploader", percentageMSG);
                VMessage.defineMessageAsRead(m);
            }

            inputStream.close();
            outputStream.close();

            boolean completed = ftpClient.completePendingCommand();
            if (completed) {
                System.out.println(">> file is uploaded successfully.");
                VMessage.info("Uploader", ">> file is uploaded successfully.");
            }

        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            VMessage.error("Uploader", "Error: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * @return the bufferSize
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * @param bufferSize the bufferSize to set
     */
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
}
