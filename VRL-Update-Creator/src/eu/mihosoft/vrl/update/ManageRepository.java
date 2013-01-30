/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.update;

import eu.mihosoft.vrl.annotation.ComponentInfo;
import eu.mihosoft.vrl.annotation.OutputInfo;
import eu.mihosoft.vrl.annotation.ParamInfo;
import eu.mihosoft.vrl.io.IOUtil;
import eu.mihosoft.vrl.reflection.VisualCanvas;
import eu.mihosoft.vrl.security.PGPUtil;
import eu.mihosoft.vrl.system.Repository;
import eu.mihosoft.vrl.system.RepositoryEntry;
import eu.mihosoft.vrl.system.VMessage;
import eu.mihosoft.vrl.system.VRL;
import eu.mihosoft.vrl.visual.VDialog;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JPasswordField;

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
@ComponentInfo(name = "Manage Repository", category = "VRL/Development")
public class ManageRepository implements java.io.Serializable {

    private static final long serialVersionUID = 1L;
    private Repository repository;

    public ManageRepository() {
    }

    public void printEntries(
            @ParamInfo(name = "as message") boolean asMessage) {

        String msg = "-- REPOSITORY --";
        String separator = "\n-------------------------------\n";
        msg += separator;

        for (RepositoryEntry re : repository.getEntries()) {
            msg += re;
            msg += separator;
        }

        System.out.println(msg);

        if (asMessage) {
            VMessage.info("Repository Content:", msg.replace("\n", "<br>"));
        }
    }

    public void loadRepository(
            @ParamInfo(name = "Server") String server,
            @ParamInfo(name = "User") String user,
            @ParamInfo(name = "Password") String pass,
            @ParamInfo(name = "Location (e.g. vrl-studio.mihosoft/updates)") String location,
            @ParamInfo(name = "OS", style = "selection",
            options = "value=[\"linux\", \"windows\", \"osx\"]") final String osFolderName) throws IOException {

        FTPFileDownloader downloader = new FTPFileDownloader();
        File repositoryFile = new File(IOUtil.createTempDir(), "repository.xml");

        downloader.download(user, pass, server, location + "/" + osFolderName + "/repository.xml", repositoryFile);

        XMLDecoder decoder = null;

        try {
            decoder = new XMLDecoder(
                    new BufferedInputStream(new FileInputStream(repositoryFile)));

            setRepository((Repository) decoder.readObject());
        } finally {
            if (decoder != null) {
                decoder.close();
            }
        }
    }

    public boolean removeEntry(
            @ParamInfo(name = "Name",
            style = "plugin-name") String name,
            @ParamInfo(name = "Version") String version) {
        return repository.removeEntry(new RepositoryEntry(name, version, "", ""));
    }

    public boolean addEntry(@ParamInfo(name = "Name",
            style = "plugin-name") String name,
            @ParamInfo(name = "Version") String version,
            @ParamInfo(name = "Server") String server,
            @ParamInfo(name = "Remote Location") String location,
            @ParamInfo(name = "URL") String url,
            @ParamInfo(name = "User") String user,
            @ParamInfo(name = "Password") String pass,
            @ParamInfo(name = "File", style = "load-dialog") File file) {

        FTPFileUploader uploader = new FTPFileUploader();
        uploader.upload(user, pass, server, location, file);
        
        removeEntry(name, version);

        repository.addEntry(new RepositoryEntry(name, version, IOUtil.generateSHA1Sum(file), url + "/" + file.getName()));

        return true;
    }

    @OutputInfo(name="Repository File")
    public File saveRepository(
            @ParamInfo(name = "Server") String server,
            @ParamInfo(name = "User") String user,
            @ParamInfo(name = "Password") String pass,
            @ParamInfo(name = "Location (e.g. vrl-studio.mihosoft/updates)") String location,
            @ParamInfo(name = "OS", style = "selection",
            options = "value=[\"linux\", \"windows\", \"osx\"]") final String osFolderName) throws IOException {

        FTPFileUploader downloader = new FTPFileUploader();
        File repositoryFile = new File(IOUtil.createTempDir(), "repository.xml");



        XMLEncoder encoder = null;

        try {
            encoder = new XMLEncoder(
                    new BufferedOutputStream(new FileOutputStream(repositoryFile)));

            encoder.writeObject(repository);
        } finally {
            if (encoder != null) {
                encoder.close();
            }
        }

        downloader.upload(user, pass, server, location + "/" + osFolderName, repositoryFile);
        
        return repositoryFile;
    }
    
    public void saveRepositorySignature(
            @ParamInfo(name = "Server") String server,
            @ParamInfo(name = "User") String user,
            @ParamInfo(name = "Password") String pass,
            @ParamInfo(name = "Location (e.g. vrl-studio.mihosoft/updates)") String location,
            @ParamInfo(name = "OS", style = "selection",
            options = "value=[\"linux\", \"windows\", \"osx\"]") final String osFolderName,
            @ParamInfo(name = "Private Key File", style="load-dialog") File privKeyFile,
            @ParamInfo(name = "Repository File", style="load-dialog") File f) throws IOException {
        
        
        VisualCanvas canvas = VRL.getCurrentProjectController().getCurrentCanvas();
        
        JPasswordField pwdField = new JPasswordField();
        
        VDialog.showDialogWindow(canvas, "Enter Private Key Password", pwdField, "Sign", true);

        FTPFileUploader downloader = new FTPFileUploader();
        File repositoryFile = new File(f.getAbsolutePath()+".asc");
        // TODO use char array for password and clear it after usage
        PGPUtil.signFile(privKeyFile, new String(pwdField.getPassword()), f, repositoryFile, true);

        downloader.upload(user, pass, server, location + "/" + osFolderName, repositoryFile);
    }

    /**
     * @return the repository
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * @param repository the repository to set
     */
    public void setRepository(Repository repository) {
        this.repository = repository;
    }
}
