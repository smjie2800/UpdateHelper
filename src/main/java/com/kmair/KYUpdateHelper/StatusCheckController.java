package com.kmair.KYUpdateHelper;

import com.kmair.KYUpdateHelper.utils.Utils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import sun.net.www.http.HttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: mzj
 * Date: 16-1-27
 * Time: 下午2:13
 * To change this template use File | Settings | File Templates.
 */
@Controller
public class StatusCheckController {

    @Value("${application.message:Hello World}")
    private String message = "Hello World";


    @RequestMapping("/statusCheckPage")
    public String statusCheckPage(String systemName, String urls, Model model) {
              return  "reslut";
    }
    @RequestMapping("/statusCheck")
    public String statusCheck(String systemName, String urls, Model model) {
        String[] urlsArray = urls.split(";");
            String result = "<h3> status result<h3><br/>";
            for (int i = 0; i < urlsArray.length; i++) {
                result += urlsArray[i] + " status is " + Utils.URLGet(urlsArray[i]) + "<br/>";
            }
            model.addAttribute("result", result);
              return  "reslut";
    }
}




  ;


