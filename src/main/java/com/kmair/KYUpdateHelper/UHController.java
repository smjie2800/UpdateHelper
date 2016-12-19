package com.kmair.KYUpdateHelper;

import com.kmair.KYUpdateHelper.service.UHService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class UHController {
    private static final Logger logger = Logger.getLogger(UHController.class);

    @Autowired
    private UHService uhService;

    @RequestMapping("/")
    public String home(Model model) {
        return "index";
    }


    @RequestMapping(value="/updateFiles",method= RequestMethod.POST)
    public String updateFiles(String svnDirs, String srcDirs, Model model) throws Exception {
        logger.info("method updateFiles(), parameters svnDirs, srcDirs: " + svnDirs + "," + srcDirs);

        String updateRootDirName = uhService.updateFiles(svnDirs.split(";"), srcDirs.split(";"));

        model.addAttribute("updateRootDirName", updateRootDirName);
        return "index";
    }

    @RequestMapping(value="/compileFiles",method= RequestMethod.POST)
    public String compileFiles(String keywords, String commitTime, String svnDirs, String srcDirs, String projectDirs, String encoding, String compileTool, String classpath, Model model) throws Exception {
        logger.info("method compileFiles(), parameters srcDirs, projectDirs: " + srcDirs + "," + projectDirs);

        String compileRootDirName = uhService.compileFiles(keywords, commitTime, svnDirs.split(";"), srcDirs.split(";"), projectDirs.split(";"), encoding, compileTool, classpath);

        model.addAttribute("keywords", keywords);
        model.addAttribute("commitTime", commitTime);
        model.addAttribute("updateRootDirName", compileRootDirName);
        return "index";
    }

    @RequestMapping(value="/extractFiles",method= RequestMethod.POST)
    public String extractFiles(String keywords, String commitTime, String svnDirs, String projectDirs, Model model) throws Exception {
        logger.info("method extractFiles(), parameters keywords,commitTime,svnDir, projectDirs: " + keywords + "," + commitTime + "," + svnDirs + "," + projectDirs);

        String extractRootDirName = uhService.extractFiles(keywords, commitTime, svnDirs.split(";"), projectDirs.split(";"));

        model.addAttribute("keywords", keywords);
        model.addAttribute("commitTime", commitTime);
        model.addAttribute("updateRootDirName", extractRootDirName);
        return "index";
    }

    @RequestMapping(value="/createDirs",method= RequestMethod.POST)
    public String createDirs(String dirs) throws Exception {
        logger.info("method createDirs(), parameters dirs: " + dirs);

        return "index";
    }
}
