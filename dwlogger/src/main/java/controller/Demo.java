package controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
@Slf4j
@Controller
public class Demo {


    @ResponseBody
    @RequestMapping("/test")
    public String test(){
        System.out.println("***********");
        log.info("hhhhhhhhh");
        log.error("rrrrr*");
        return "succes";
    }
}
