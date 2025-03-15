package ru.nesterov.generalTest;

import org.junit.jupiter.api.Test;


public class StringFormatterTest {
    @Test
    public void formatStringTest(){
        String string = "<pre><span>предыдущая дата : 2025-02-1T14:02:36.950+00:00</span></pre><pre><span>предыдущая дата : 2025-02-1T14:02:36.950+00:00</span></pre><pre><span>предыдущая дата : 2025-02-1T14:02:36.950+00:00</span></pre><pre><span>предыдущая дата : 2025-02-1T14:02:36.950+00:00</span></pre><pre>запланировано : нет</pre>";

        String actual3 = string.replaceAll("</span>", "\n").replaceAll("<[^>]*>", "");
        System.out.println(actual3);
    }
}
