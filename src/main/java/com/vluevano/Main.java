package com.vluevano;

import com.vluevano.view.LoginScreen;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(Main.class).run();
    }

    @Override
    public void start(Stage primaryStage) {
        LoginScreen loginScreen = context.getBean(LoginScreen.class);
        loginScreen.show(primaryStage);
    }

    @Override
    public void stop() {
        context.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}