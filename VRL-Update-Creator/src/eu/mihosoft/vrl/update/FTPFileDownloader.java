/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.update;

import eu.mihosoft.vrl.annotation.ComponentInfo;
import eu.mihosoft.vrl.annotation.ParamInfo;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTP;
import java.io.IOException;
import org.apache.commons.net.ftp.FTPFile;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
@ComponentInfo(name = "FTPFileDownloader", category = "VRL/Net")
public class FTPFileDownloader implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    
    private int bufferSize = 4096;

    // add your code here
    public void download(
            @ParamInfo(name = "Username", style = "default", options = "") String user,
            @ParamInfo(name = "Password", style = "default", options = "") String pass,
            @ParamInfo(name = "Server", style = "default", options = "") String server,
            @ParamInfo(name = "Remote File", style = "default", options = "") String remoteFile,
            @ParamInfo(name = "", style = "save-dialog", options = "") File content) {
        int port = 21;

        FTPClient ftpClient = new FTPClient();
        try {

            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            System.out.println(">> start downloading file " + remoteFile + " to " + server);

            OutputStream outputStream =
                    new BufferedOutputStream(new FileOutputStream(content));
            InputStream inputStream = ftpClient.retrieveFileStream(remoteFile);

            byte[] bytesArray = new byte[bufferSize];
            int bytesRead = -1;
            long totalRead = 0;
            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                outputStream.write(bytesArray, 0, bytesRead);
                totalRead += bytesRead;
                System.out.println(" --> downloading " + content.getName() + ": " + (totalRead / 1024) + " KB");
            }
            
            System.out.println(">> file is downloaded successfully.");

            // TODO why did this code wait sooo long?
//            System.out.println(" --> waiting for ftp command to complete...");
//            boolean complete = ftpClient.completePendingCommand();
//            if (complete) {
//                System.out.println(">> file is downloaded successfully.");
//            }
            outputStream.close();
            inputStream.close();

        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
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
