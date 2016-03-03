package com.theironyard;

import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by alexanderhughes on 3/2/16.
 */
public class PeopleWebTest {
    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./test");
        PeopleWeb.createTables(conn);
        return conn;
    }

    void endConnection(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE persons");
        conn.close();
    }

    @Test
    public void testPerson() throws SQLException {
        Connection conn = startConnection();
        PeopleWeb.insertPerson(conn, new Person(1, "bob", "villa", "@bobvilla", "norway", "123456765432"));
        Person person = PeopleWeb.selectPerson(conn, 1);
        endConnection(conn);
        assertTrue(person != null);
    }

    @Test
    public void testSelectPersons() throws SQLException {
        Connection conn = startConnection();
        PeopleWeb.insertPerson(conn, new Person(1, "Martha", "Jenkins", "mjenkins0@un.org", "France", "97.252.235.143"));
        PeopleWeb.insertPerson(conn, new Person(2, "Martha", "Jenkins", "mjenkins0@un.org", "France", "97.252.235.143"));
        PeopleWeb.insertPerson(conn, new Person(3, "Martha", "Jenkins", "mjenkins0@un.org", "France", "97.252.235.143"));
        ArrayList<Person> persons = PeopleWeb.selectPersons(conn, 1);
        endConnection(conn);
        assertTrue(persons.size() == 2);//i started the offset at 1, so the array list only gets the index 2 and 3
    }
}