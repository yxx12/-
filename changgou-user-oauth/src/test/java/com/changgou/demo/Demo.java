package com.changgou.demo;

import java.util.HashMap;
import java.util.Map;

public class Demo {
    public static void main(String[] args) {
        String a="aaaa";
        Map map=new HashMap();
        map.put("aq",a+"ff");
        int i = map.hashCode();
        int i1 = a.hashCode();
        System.out.println(i);
        System.out.println(i1);
    }
}
