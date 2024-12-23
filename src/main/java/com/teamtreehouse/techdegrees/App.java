package com.teamtreehouse.techdegrees;


import static spark.Spark.get;
import static spark.Spark.staticFileLocation;

public class App {

    public static void main(String[] args) {
        staticFileLocation("/public");

        get("/blah", (req, res) -> "Hello!");

    }

}
