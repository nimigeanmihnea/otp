package main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.util.DataUtils;
import main.util.Util;

import javax.xml.transform.Result;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Optional;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/main/view/menu.fxml"));
        primaryStage.setTitle("OTP File Protector");
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Do you really want to exit?", ButtonType.YES, ButtonType.CANCEL);
                alert.setTitle("Closing");
                Optional<ButtonType> s = alert.showAndWait();
                if(s.equals(ButtonType.YES)){
                    Platform.exit();
                    System.exit(0);
                }else if(s.equals(ButtonType.CANCEL))
                    return;
            }
        });
        primaryStage.show();
        //DataUtils.generateKey(Util.KEY_PATH.substring(0, Util.KEY_PATH.length() - 3) + "key", "64", "16");
    }

    @Override
    public void init() throws Exception {

    }

    public static void main(String[] args) {
        launch(args);
    }
}
