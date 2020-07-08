package com.ganyoubing.study;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class Engineer extends HttpServlet implements Observer {

    private List<Bird> birds;
    public static void main(String[] args) {
        Engineer engineer = new Engineer();
        if (engineer.birds != null) {//1层判空
            for (Bird bird : engineer.birds) {
                if (bird != null){//二层判空
                    if (bird.getColor() != null){//三层判空
                        System.out.println(bird.getColor().length());
                    }
                }
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

    }

    @Override
    public void update(Observable o, Object arg) {

    }
}
