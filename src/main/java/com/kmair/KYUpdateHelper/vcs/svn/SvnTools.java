package com.kmair.KYUpdateHelper.vcs.svn;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.tmatesoft.svn.core.*;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;
import java.util.*;

/*
 * The code Fragment from http://wiki.svnkit.com/Printing_Out_Repository_History
 */

@Repository
public class SvnTools {
    private static final Logger logger = Logger.getLogger(SvnTools.class);

    private static final String username = "";
    private static final String password = "";

    public static final String updateFilesPath = SVNLogEntryPath.TYPE_ADDED + SVNLogEntryPath.TYPE_MODIFIED + SVNLogEntryPath.TYPE_REPLACED + "";
    public static final String deleteFilesPath = SVNLogEntryPath.TYPE_DELETED + "";
    public static final String messages = "messages";

    public static String succFile = "";
    public static String failFile = "";

    public HashMap<String, ArrayList<String>> getFilesPathAndMessageByKeyWord(String keyWord, String svnUrl) throws SVNException {
        logger.info("method getFilesPathAndMessageByKeyWord(), parameters keyword,svnUrl: " + keyWord + "," + svnUrl);

        HashMap<String, ArrayList<String>> filesPathAndMessage = new HashMap<String, ArrayList<String>>();

        Collection logEntries = getLogEntries(svnUrl);

        for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
            if (logEntry.getMessage().contains(keyWord)) {

                setFilesPathAndMessage(filesPathAndMessage, logEntry);
            }
        }

        return filesPathAndMessage;
    }

    public HashMap<String, ArrayList<String>> getFilesPathAndMessageByCommitDate(Date commitDate, String svnUrl) throws SVNException {
        logger.info("method getFilesPathAndMessageByCommitDate(), parameters commitDate,svnUrl: " + commitDate + "," + svnUrl);

        HashMap<String, ArrayList<String>> filesPathAndMessage = new HashMap<String, ArrayList<String>>();

        Collection logEntries = getLogEntries(svnUrl);

        for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
            SVNLogEntry logEntry = (SVNLogEntry) entries.next();
            if (logEntry.getDate().getTime() > commitDate.getTime()) {

                setFilesPathAndMessage(filesPathAndMessage, logEntry);
            }
        }

        return filesPathAndMessage;
    }

    public  Collection getLogEntries(String svnUrl) throws SVNException {

        long latestRevision = -1;//HEAD the latest revision

        setupLibrary();

        SVNRepository repository = null;

        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(svnUrl));
        } catch (SVNException svne) {
            logger.info("error while creating an SVNRepository for the location '"
                    + svnUrl + "': " + svne.getMessage());
            throw svne;
        }

        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
        repository.setAuthenticationManager(authManager);

        try {
            latestRevision = repository.getLatestRevision();
        } catch (SVNException svne) {
            logger.info("error while fetching the latest repository revision: " + svne.getMessage());
            throw svne;
        }

        Collection logEntries = null;
        try {
            // 检索最近10个版本
            logEntries = repository.log(new String[] {""}, null,
                    (latestRevision > 15 ? latestRevision - 15 : 0), latestRevision, true, true);

        } catch (SVNException svne) {
            logger.info("error while collecting log information for '" + svnUrl + "': " + svne.getMessage());
            throw svne;
        }

        return logEntries;
    }

    public void setFilesPathAndMessage(HashMap<String, ArrayList<String>> filesPathAndMessage, SVNLogEntry logEntry) {
        if (logEntry.getMessage() != null) {
            if (filesPathAndMessage.get(messages) == null) {
                ArrayList<String> messagesArr = new ArrayList<String>();
                messagesArr.add(getLog(logEntry.getMessage()));
                filesPathAndMessage.put(messages, messagesArr);

            } else {
                filesPathAndMessage.get(messages).add(getLog(logEntry.getMessage()));
            }
        }

        if (logEntry.getChangedPaths().size() > 0) {
            Set changedPathsSet = logEntry.getChangedPaths().keySet();

            for (Iterator changedPaths = changedPathsSet.iterator(); changedPaths.hasNext();) {

                SVNLogEntryPath entryPath = (SVNLogEntryPath) logEntry
                        .getChangedPaths().get(changedPaths.next());

                if (entryPath.getType() == SVNLogEntryPath.TYPE_ADDED ||
                        entryPath.getType() == SVNLogEntryPath.TYPE_MODIFIED ||
                        entryPath.getType() == SVNLogEntryPath.TYPE_REPLACED) {

                    if (filesPathAndMessage.get(updateFilesPath) == null) {
                        ArrayList<String> paths = new ArrayList<String>();
                        paths.add(entryPath.getPath());
                        filesPathAndMessage.put(updateFilesPath, paths);

                    } else {
                        filesPathAndMessage.get(updateFilesPath).add(entryPath.getPath());
                    }
                }

                if (entryPath.getType() == SVNLogEntryPath.TYPE_DELETED) {
                    if (filesPathAndMessage.get(deleteFilesPath) == null) {
                        ArrayList<String> paths = new ArrayList<String>();
                        paths.add(entryPath.getPath());
                        filesPathAndMessage.put(deleteFilesPath, paths);

                    } else {
                        filesPathAndMessage.get(deleteFilesPath).add(entryPath.getPath());
                    }
                }

                logger.info(" "
                            + entryPath.getType()
                            + "	"
                            + entryPath.getPath()
                            + ((entryPath.getCopyPath() != null) ? " (from "
                            + entryPath.getCopyPath() + " revision "
                            + entryPath.getCopyRevision() + ")" : ""));
            }
        }
    }

    /**
     * 获取用户提交备注
     * @param svnMessage
     * @return
     */
    public String getLog(String svnMessage) {
        int startIndex = svnMessage.indexOf("svn:log=");
        int endIndex = svnMessage.indexOf("svn:author");

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return svnMessage.substring(startIndex, endIndex);
        } else {
            if (svnMessage.length() > 20) {
                return svnMessage.substring(0, 20);
            } else {
                return svnMessage;
            }
        }
    }

    /**
     * 从 SVN 更新项目到本地副本目录
     * @param svnUrl  SVN目录
     * @param copyDir  副本目录
     * @return
     */
    public boolean updateProjectFromSvn(String svnUrl, String copyDir) {
        SVNClientManager clientManager = authSvn(svnUrl, username, password);
        if (null == clientManager) {
            logger.error("SVN login error! >>> url:" + svnUrl
                    + " username:" + username + " password:" + password);
            return false;
        }

        // 注册一个更新事件处理器  
        clientManager.getCommitClient().setEventHandler(new UpdateEventHandler());

        SVNURL repositoryURL = null;
        try {
            repositoryURL = SVNURL.parseURIEncoded(svnUrl);
        } catch (SVNException e) {
            logger.error(e.getMessage(),e);
            return false;
        }

        File ws = new File(copyDir);
        if(!SVNWCUtil.isVersionedDirectory(ws)){
            checkout(clientManager, repositoryURL, SVNRevision.HEAD, new File(copyDir), SVNDepth.INFINITY);
        }else{
            update(clientManager, ws, SVNRevision.HEAD, SVNDepth.INFINITY);
        }
        return true;
    }

    /**
     * 验证登录svn
     */
    public SVNClientManager authSvn(String svnRoot, String username,
                                           String password) {
        // 初始化版本库
        setupLibrary();

        // 创建库连接
        SVNRepository repository = null;
        try {
            repository = SVNRepositoryFactory.create(SVNURL
                    .parseURIEncoded(svnRoot));
        } catch (SVNException e) {
            logger.error(e.getErrorMessage(), e);
            return null;
        }

        // 身份验证
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);

        // 创建身份验证管理器
        repository.setAuthenticationManager(authManager);

        DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
        SVNClientManager clientManager = SVNClientManager.newInstance(options,
                authManager);
        return clientManager;
    }

    /**
     * Updates a working copy (brings changes from the repository into the working copy).
     * @param clientManager
     * @param wcPath
     *          working copy path
     * @param updateToRevision
     *          revision to update to
     * @param depth
     *          update的深度：目录、子目录、文件
     * @return
     * @throws SVNException
     */
    public long update(SVNClientManager clientManager, File wcPath,
                              SVNRevision updateToRevision, SVNDepth depth) {
        SVNUpdateClient updateClient = clientManager.getUpdateClient();

        /*
        * sets externals not to be ignored during the update
        */
        updateClient.setIgnoreExternals(false);

        /*
        * returns the number of the revision wcPath was updated to
        */
        try {
            return updateClient.doUpdate(wcPath, updateToRevision,depth, false, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * recursively checks out a working copy from url into wcDir
     * @param clientManager
     * @param url
     *          a repository location from where a Working Copy will be checked out
     * @param revision
     *          the desired revision of the Working Copy to be checked out
     * @param destPath
     *          the local path where the Working Copy will be placed
     * @param depth
     *          checkout的深度，目录、子目录、文件
     * @return
     * @throws SVNException
     */
    public long checkout(SVNClientManager clientManager, SVNURL url,
                                SVNRevision revision, File destPath, SVNDepth depth) {

        SVNUpdateClient updateClient = clientManager.getUpdateClient();
        /*
        * sets externals not to be ignored during the checkout
        */
        updateClient.setIgnoreExternals(false);
        /*
        * returns the number of the revision at which the working copy is
        */
        try {
            return updateClient.doCheckout(url, destPath, revision, revision,depth, false);
        } catch (SVNException e) {
            logger.error(e.getErrorMessage(), e);
        }
        return 0;
    }

    /*
     * Initializes the library to work with a repository via
     * different protocols.
     */
    private void setupLibrary() {
        /*
         * For using over http:// and https://
         */
        DAVRepositoryFactory.setup();
        /*
         * For using over svn:// and svn+xxx://
         */
        SVNRepositoryFactoryImpl.setup();

        /*
         * For using over file:///
         */
        FSRepositoryFactory.setup();
    }

 /*   public static void main(String[] args) {
        new SvnTools().updateProjectFromSvn("https://10.1.11.111/svn/API/OTA_API", "D:/api/OTA_API");
    }*/
}
