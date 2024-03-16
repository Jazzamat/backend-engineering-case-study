package com.dreamgames.backendengineeringcasestudy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.jupiter.api.Test;

public class MySqlTest {

    private final String JDBC_URL = "jdbc:mysql://localhost:3306/dreambase";
    private final String  USERNAME = "dreamgames";
    private final String  PASSWORD = "password";

    @Test
    public void testJBDCConnection() {
        assertDoesNotThrow(() -> connect());
    }

    
    public void connect() throws IllegalStateException {
        try {
            Connection connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }


}
