package com.theironyard;
import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class PeopleWeb {


    public static void main(String[] args) throws FileNotFoundException, SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        populateDatabase("people.csv", conn);

        Spark.init();
        Spark.get(
                "/",
                ((request, response) -> {
                    HashMap m = new HashMap();
                    String offsetStr = request.queryParams("offset");
                    int offset = 0;
                    if (offsetStr != null) {
                        offset = Integer.valueOf(offsetStr);
                    }
                    ArrayList<Person> temp = selectPersons(conn, offset);

                    m.put("end", offset >= getCount(conn) - 20);
                    m.put("beginning", offset == 0);
                    m.put("persons", temp);
                    m.put("offsetUp", offset + 20);
                    m.put("offsetDown", offset - 20);
                    return new ModelAndView(m, "home.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.get(
                "/info",
                ((request, response) -> {
                    HashMap m = new HashMap();
                    int index = Integer.valueOf(request.queryParams("id")) - 1;
                    Person person = selectPerson(conn, index);
                    m.put("person", person);
                    return new ModelAndView(m, "person.html");
                }),
                new MustacheTemplateEngine()
        );
    }

    static void populateDatabase(String fileName, Connection conn) throws FileNotFoundException, SQLException {
        File f = new File(fileName);
        Scanner fileScanner = new Scanner(f);
        fileScanner.nextLine();
        while (fileScanner.hasNext()) {
            String[] columns = fileScanner.nextLine().split(",");
            Person person = new Person(Integer.valueOf(columns[0]), columns[1], columns[2], columns[3], columns[4], columns[5]);
            insertPerson(conn, person);
        }
    }

    public static void createTables(Connection conn) throws SQLException {
        dropTables(conn);
        Statement stmt = conn. createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS persons (id Identity, first_name VARCHAR, last_name VARCHAR, email VARCHAR, country VARCHAR, ip_address VARCHAR)");
    }
    public static void dropTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS persons");
    }

    public static void insertPerson(Connection conn, Person person) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO persons VALUES (NULL, ?, ?, ?, ?, ?)");
        stmt.setString(1, person.firstName);
        stmt.setString(2, person.lastName);
        stmt.setString(3, person.email);
        stmt.setString(4, person.country);
        stmt.setString(5, person.ipAddress);
        stmt.execute();
    }

    public static Person selectPerson(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM persons WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        results.next();
        int personId = results.getInt("id");
        String firstName = results.getString("first_name");
        String lastName = results.getString("last_name");
        String email = results.getString("email");
        String country = results.getString("country");
        String ipAddress = results.getString("ip_address");
        return new Person(personId, firstName, lastName, email, country, ipAddress);
    }

    public static ArrayList<Person> selectPersons(Connection conn, int offset) throws SQLException {
        ArrayList<Person> persons = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM persons LIMIT 20 OFFSET ?");
        stmt.setInt(1, offset);
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            int personId = results.getInt("id");
            String firstName = results.getString("first_name");
            String lastName = results.getString("last_name");
            String email = results.getString("email");
            String country = results.getString("country");
            String ipAddress = results.getString("ip_address");
            persons.add(new Person(personId, firstName, lastName, email, country, ipAddress));
        }
        return persons;
    }

    public static int getCount(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet results = stmt.executeQuery("SELECT COUNT(id) AS size FROM persons");
        results.next();
        return results.getInt("size");
    }
}
