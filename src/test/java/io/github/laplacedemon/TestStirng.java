package io.github.laplacedemon;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class TestStirng {
    
    @Test
    public void testFindString() {
        String str = "hello ${hello} world ${world} asdf${hello.world}";
        
        String key = "\\$\\{.*?\\}";
        Pattern pattern = Pattern.compile(key) ; 
        Matcher matcher = pattern.matcher(str) ;
        
        while(matcher.find()){
            int count = matcher.groupCount() ;
            System.out.println(count);
            for (int i = 0; i <= count; i++) {
                String ret = matcher.group(i);
                int start = matcher.start(i);
                int end = matcher.end(i);
                System.out.println(ret + start + "," + end);
            }
        }
    }
}
