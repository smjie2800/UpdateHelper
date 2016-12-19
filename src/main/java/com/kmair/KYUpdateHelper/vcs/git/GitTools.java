package com.kmair.KYUpdateHelper.vcs.git;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.log4j.Logger;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.util.*;

public class GitTools {

    private static final Logger logger = Logger.getLogger(GitTools.class);

    public HashMap<String, ArrayList<String>> getFilesPathAndMessageByKeyWord(String keyWord, String svnUrl) throws SVNException {
        logger.info("method getFilesPathAndMessageByKeyWord(), parameters keyword,svnUrl: " + keyWord + "," + svnUrl);

        HashMap<String, ArrayList<String>> filesPathAndMessage = new HashMap<String, ArrayList<String>>();

        Collection logEntries = getLogEntries(svnUrl);

        for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
            if (logEntry.getMessage().contains(keyWord)) {
            }
        }

        return filesPathAndMessage;
    }

    SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
        @Override
        protected void configure(OpenSshConfig.Host host, Session session ) {
            // do nothing
        }
        @Override
        protected JSch createDefaultJSch(FS fs ) throws JSchException {
            JSch defaultJSch = super.createDefaultJSch(fs);
            defaultJSch.addIdentity( "/resources/git/ssh/key" );
            return defaultJSch;
        }
    };

    public  Collection getLogEntries(String svnUrl) throws SVNException {

        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI( "git@git.airkunming.com:B2C/B2CResources.git" );
        cloneCommand.setTransportConfigCallback(new TransportConfigCallback() {
            @Override
            public void configure( Transport transport ) {
                SshTransport sshTransport = (SshTransport)transport;
                sshTransport.setSshSessionFactory(sshSessionFactory);
            }
        } );
        return null;
    }
}
