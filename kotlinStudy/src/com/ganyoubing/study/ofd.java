package com.ganyoubing.study;

public class ofd {

    public static void main(String[] args) {
        String url = "http://localhost:8080/web-reader/reader?file=test.ofd";
        String url2 = url.replaceAll("/+", "/").substring(1);
        System.out.println("url2: "+url);
        String[] array = url2.split("/");
        System.out.println("array: "+ array.toString());

    }
}
