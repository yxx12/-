package com.changgou.filter;

/**
 * @ClassName URLFilter
 * @Description 对请求地址是否放行
 * @Author
 * @Date 12:27 2019/10/29
 * @Version 2.1
 **/
public class URLFilter {

    // 对需要放行的地址进行维护：常量
    private static String uri = "/api/user/add,/api/user/login,/api/search";

    public static boolean hasAuthorize(String url) {
        String[] uris = uri.split(",");
        for (String uri : uris) {
            if (url.startsWith(uri)) {
                // 放行
                return true;
            }
        }
        return false;
    }
}
