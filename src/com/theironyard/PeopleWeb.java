package com.theironyard;

import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class PeopleWeb {

    static ArrayList<Person> persons = new ArrayList<>();


    public static void main(String[] args) throws FileNotFoundException {
        readFile("people.csv");

        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    HashMap m = new HashMap();
                    String offsetStr = request.queryParams("offset");
                    int start = 0;
                    if (offsetStr != null) {
                        start += Integer.valueOf(offsetStr);
                    }
                    int offset = 20 + start;
                    ArrayList<Person> temp = new ArrayList<>(persons.subList(start, Math.min(offset, persons.size())));

                    m.put("end", start >= persons.size() - 20);
                    m.put("beginning", start == 0);
                    m.put("persons", temp);
                    m.put("offsetUp", offset);
                    m.put("offsetDown", offset - 40);
                    return new ModelAndView(m, "home.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.get(
                "/info",
                ((request, response) -> {
                    HashMap m = new HashMap();
                    int index = Integer.valueOf(request.queryParams("id")) - 1;
                    Person person = persons.get(index);
                    m.put("person", person);
                    return new ModelAndView(m, "person.html");
                }),
                new MustacheTemplateEngine()
        );
    }

    static void readFile(String fileName) throws FileNotFoundException {
        File f = new File(fileName);
        Scanner fileScanner = new Scanner(f);
        fileScanner.nextLine();
        while (fileScanner.hasNext()) {
            String[] columns = fileScanner.nextLine().split(",");
            Person person = new Person(Integer.valueOf(columns[0]), columns[1], columns[2], columns[3], columns[4], columns[5]);
            persons.add(person);
        }
    }
}
