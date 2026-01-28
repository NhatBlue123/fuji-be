package com.example.fuji;
import java.sql.Connection;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Component
public class TestConnection implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--------------------------------");
        System.out.println("ĐANG KIEM TRA KET NOI DATABASE...");
        
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("KET NOI DATABASE THANH CONG!");
            System.out.println("URL: " + connection.getMetaData().getURL());
        } catch (Exception e) {
            System.out.println("KET NOI DATABASE THAT BAI!");
            e.printStackTrace();
        }
        
        System.out.println("--------------------------------");
    }
}