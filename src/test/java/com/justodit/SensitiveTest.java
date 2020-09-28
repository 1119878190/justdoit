package com.justodit;

import com.justodit.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter(){
        String text ="这里可以赌博,可以嫖娼,可以吸毒,可以开票哈哈哈!";
        String filter = sensitiveFilter.filter(text);
        System.out.println(filter);

        text ="这里可以★赌★博,可以嫖★娼,可以★吸毒,可以★开★票★哈哈哈!";
        filter = sensitiveFilter.filter(text);
        System.out.println(filter);

    }

}
