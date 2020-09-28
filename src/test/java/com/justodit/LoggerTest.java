package com.justodit;



import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
public class LoggerTest {

    private static final Logger logger = LoggerFactory.getLogger(LoggerTest.class);


    @Test
    public void testLogger(){
        System.out.println(logger.getName());

        logger.debug("debugLog");
        logger.info("info Log");
        logger.warn("warn Log");
        logger.error("error log");

    }


}
