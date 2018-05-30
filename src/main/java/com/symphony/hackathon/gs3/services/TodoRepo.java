package com.symphony.hackathon.gs3.services;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.symphony.hackathon.gs3.JsonObjectMapper;
import com.symphony.hackathon.gs3.model.Todo;
import io.dropwizard.jackson.Jackson;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The dirtiest sqllite persistence in history
 */
public class TodoRepo {
    String url = "jdbc:sqlite:todo.sqllitedb";
    private static final String INSERT_SQL = "INSERT INTO TODOS(id, json) VALUES (?, ?)";
    private ObjectMapper mapper = JsonObjectMapper.get();
    public TodoRepo(){
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute("CREATE TABLE IF NOT EXISTS TODOS (id integer PRIMARY KEY, json text)");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void save(Todo todo){

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
            if (conn != null) {
                stmt.setLong(1,todo.id);
                stmt.setString(2,mapper.writeValueAsString(todo));
                stmt.execute();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        new TodoRepo();
    }

    public Map<Long, Todo> loadAll() {
        String sql = "SELECT id, json FROM TODOS";

        Map<Long, Todo> result = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){
            while (rs.next()) {
                result.put(rs.getLong("id"),mapper.readValue(rs.getString("json"), Todo.class));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
