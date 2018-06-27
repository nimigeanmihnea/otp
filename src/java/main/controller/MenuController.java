package main.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import main.misc.PasswordDialog;
import main.model.KeyMetadata;
import main.service.DecoderService;
import main.service.DefragmentService;
import main.service.EncoderService;
import main.service.MailService;
import main.util.DataUtils;
import main.util.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

public class MenuController implements Initializable{

    @FXML
    private TextFlow aboutText;

    @FXML
    private Pane sendMessagePane;

    @FXML
    private TextField messageText;

    @FXML
    private TextField messageSender;

    @FXML
    private TextField messageReciever;

    @FXML
    private TextField messageSubject;

    @FXML
    private Pane infoPane;

    @FXML
    private Pane aboutPane;

    @FXML
    private Label sizeLabel;

    @FXML
    private Label freeLabel;

    private String encPath;

    private KeyMetadata keyMetadata;
    private ArrayList<Rectangle> decList = new ArrayList<>();
    private Rectangle rectangle;

    public MenuController(){
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sendMessagePane.setVisible(false);
        infoPane.setVisible(false);
        aboutText.getChildren().clear();


        Text text = new Text();
        text.setText("OTP Este.......");
        text.setFont(Font.font ("Verdana", 20));
        text.setFill(Color.WHITE);

        aboutText.getChildren().add(text);
        aboutPane.setVisible(true);
    }

    public void encryptActionListener(){
        sendMessagePane.setVisible(false);
        aboutPane.setVisible(false);
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        if(file == null){
            Alert alert = new Alert(Alert.AlertType.WARNING, "You must select a file!", ButtonType.OK);
            alert.setTitle("No file selected");
            alert.showAndWait();
        }else{
            String path = file.getAbsolutePath();
            EncoderService encoderService = new EncoderService();
            try {
                keyMetadata = DataUtils.readKeyMetadata(Util.KEY_METADATA_PATH);
                if((new File(path)).length() <= 0){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "File is empty! Nothing to encrypt.", ButtonType.OK);
                    alert.setTitle("Empty file");
                    alert.showAndWait();
                    return;
                }
                int length = encoderService.encode(Util.KEY_PATH, path, path.substring(0, path.length() - file.getName().length()), keyMetadata);
                if(length == 0){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Not enough space on the key to encrypt file!", ButtonType.OK);
                    alert.setTitle("Not enough space");
                    alert.showAndWait();
                }else {
                    int width = calculateWidth((int) keyMetadata.getOffset());
                    if (infoPane.getChildren().contains(rectangle)) {
                        infoPane.getChildren().remove(rectangle);
                        rectangle = new Rectangle(37, 36, width, 23);
                        rectangle.setFill(Color.PURPLE);
                        infoPane.getChildren().add(rectangle);
                    } else {
                        rectangle = new Rectangle(37, 36, width, 23);
                        rectangle.setFill(Color.PURPLE);
                        infoPane.getChildren().add(rectangle);
                    }

                    decList = DataUtils.readDecList();
                    if (decList != null && decList.size() > 0) {
                        for (Rectangle rec : decList) {
                            if (infoPane.getChildren().contains(rec))
                                infoPane.getChildren().remove(rec);
                            infoPane.getChildren().add(rec);
                        }
                    } else {
                        decList = new ArrayList<>();
                    }
                    keyMetadata = DataUtils.readKeyMetadata(Util.KEY_METADATA_PATH);
                    sizeLabel.setText(Integer.toString((int) (((new File(Util.KEY_PATH)).length()) / 1000)) + " KB");
                    double free = ((((new File(Util.KEY_PATH)).length()) - keyMetadata.getOffset()));
                    freeLabel.setText(Double.toString(free) + " B");
                }
                infoPane.setVisible(true);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error encrypting file!", ButtonType.OK);
                alert.setTitle("Encryption error");
                alert.showAndWait();
            }
        }
    }

    public void decryptActionListener(){
        sendMessagePane.setVisible(false);
        aboutPane.setVisible(false);
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        if(file == null){
            Alert alert = new Alert(Alert.AlertType.WARNING, "You must select a file!", ButtonType.OK);
            alert.setTitle("No file selected");
            alert.showAndWait();
        }else{
            keyMetadata = DataUtils.readKeyMetadata(Util.KEY_METADATA_PATH);
            long stop = keyMetadata.getOffset();
            String path = file.getAbsolutePath();
            DecoderService decoderService = new DecoderService();
            boolean success = decoderService.decode(Util.KEY_PATH, path, path.substring(0, path.length() - file.getName().length()), (int) stop, decList);
            if(!success){
                Alert alert = new Alert(Alert.AlertType.WARNING, "The file is corrupted, or it was already decrypted.", ButtonType.OK);
                alert.setTitle("Corrupted file");
                alert.showAndWait();
            }else {

                int width = calculateWidth((int) keyMetadata.getOffset());
                if (infoPane.getChildren().contains(rectangle)) {
                    infoPane.getChildren().remove(rectangle);
                    rectangle = new Rectangle(37, 36, width, 23);
                    rectangle.setFill(Color.PURPLE);
                    infoPane.getChildren().add(rectangle);
                } else {
                    rectangle = new Rectangle(37, 36, width, 23);
                    rectangle.setFill(Color.PURPLE);
                    infoPane.getChildren().add(rectangle);
                }
                if (decList.size() > 0) {
                    DataUtils.writeDecList(decList);
                    for (Rectangle rec : decList) {
                        if (infoPane.getChildren().contains(rec))
                            infoPane.getChildren().remove(rec);
                        infoPane.getChildren().add(rec);
                    }
                }
                keyMetadata = DataUtils.readKeyMetadata(Util.KEY_METADATA_PATH);
                sizeLabel.setText(Integer.toString((int) (((new File(Util.KEY_PATH)).length()) / 1000)) + " KB");
                double free = ((((new File(Util.KEY_PATH)).length()) - keyMetadata.getOffset()));
                freeLabel.setText(Double.toString(free) + " B");
            }
            infoPane.setVisible(true);
        }
    }

    public void sysInfoActionListener(){
        aboutPane.setVisible(false);
        sendMessagePane.setVisible(false);
        keyMetadata = DataUtils.readKeyMetadata(Util.KEY_METADATA_PATH);
        sizeLabel.setText(Integer.toString((int) (((new File(Util.KEY_PATH)).length()) / 1000)) + " KB");
        double free =  ((((new File(Util.KEY_PATH)).length()) - keyMetadata.getOffset()));
        freeLabel.setText(Double.toString(free) + " B");
        int width = calculateWidth((int) keyMetadata.getOffset());
        if(infoPane.getChildren().contains(rectangle)){
            infoPane.getChildren().remove(rectangle);
            rectangle = new Rectangle(37, 36, width, 23);
            rectangle.setFill(Color.PURPLE);
            infoPane.getChildren().add(rectangle);
        }else {
            rectangle = new Rectangle(37, 36, width, 23);
            rectangle.setFill(Color.PURPLE);
            infoPane.getChildren().add(rectangle);
        }

        decList = DataUtils.readDecList();
        if(decList != null && decList.size() > 0){
            for(Rectangle rec : decList){
                if(infoPane.getChildren().contains(rec))
                    infoPane.getChildren().remove(rec);
                infoPane.getChildren().add(rec);
            }
        }else {
            decList = new ArrayList<>();
        }
        infoPane.setVisible(true);
    }

    public void defragmentActionListener(){
        keyMetadata = DataUtils.readKeyMetadata(Util.KEY_METADATA_PATH);
        DefragmentService defragmentService = new DefragmentService();
        defragmentService.defragment(Util.KEY_PATH, (int) keyMetadata.getOffset(), keyMetadata);
        sendMessagePane.setVisible(false);
        aboutPane.setVisible(false);
        keyMetadata = DataUtils.readKeyMetadata(Util.KEY_METADATA_PATH);
        if(decList != null && decList.size() > 0){
            for(Rectangle rec : decList){
                if(infoPane.getChildren().contains(rec)){
                    infoPane.getChildren().remove(rec);
                }
            }
            FileWriter fos = null;
            try {
                fos = new FileWriter("/home/mihter/Desktop/.data.xml");
                fos.write("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            for(Rectangle rec : decList) {
                if (infoPane.getChildren().contains(rec)) {
                    infoPane.getChildren().remove(rec);;
                }
            }
            FileWriter fos = null;
            try {
                fos = new FileWriter("/home/mihter/Desktop/.data.xml");
                fos.write("");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        sizeLabel.setText(Integer.toString((int) (((new File(Util.KEY_PATH)).length()) / 1000)) + " KB");
        double free =  ((((new File(Util.KEY_PATH)).length()) - keyMetadata.getOffset()));
        freeLabel.setText(Double.toString(free) + " B");
        int width = calculateWidth((int) keyMetadata.getOffset());
        if(infoPane.getChildren().contains(rectangle)){
            infoPane.getChildren().remove(rectangle);
            rectangle = new Rectangle(37, 36, width, 23);
            rectangle.setFill(Color.PURPLE);
            infoPane.getChildren().add(rectangle);
        }else {
            rectangle = new Rectangle(37, 36, width, 23);
            rectangle.setFill(Color.PURPLE);
            infoPane.getChildren().add(rectangle);
        }
        infoPane.setVisible(true);
    }

    public void aboutActionListener(){
        sendMessagePane.setVisible(false);
        infoPane.setVisible(false);
        aboutText.getChildren().clear();
        aboutPane.setVisible(true);

        Text text = new Text();
        text.setText("OTP Este.......");
        text.setFont(Font.font ("Verdana", 20));
        text.setFill(Color.WHITE);

        aboutText.getChildren().add(text);
    }

    public void sendMessageActionListener(){
        aboutPane.setVisible(false);
        sendMessagePane.setVisible(true);
        infoPane.setVisible(false);
    }

    public void browseActionListener(ActionEvent event){
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);

        if(file == null){
            Alert alert = new Alert(Alert.AlertType.WARNING, "Do you want to send mail without attachments?", ButtonType.YES, ButtonType.NO);
            alert.setTitle("No attachment");
            alert.show();
        }else{
            String path = file.getAbsolutePath();
            EncoderService encoderService = new EncoderService();
            try {
                keyMetadata = DataUtils.readKeyMetadata(Util.KEY_METADATA_PATH);
                if((new File(path)).length() <= 0){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "File is empty! Nothing to encrypt.", ButtonType.OK);
                    alert.setTitle("Empty file");
                    alert.showAndWait();
                    return;
                }
                int length = encoderService.encode(Util.KEY_PATH, path, path.substring(0, path.length() - file.getName().length()), keyMetadata);
                encPath = path.substring(0, path.length() - 4) + ".enc";
                if(length == 0){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Not enough space on the key to encrypt file!", ButtonType.OK);
                    alert.setTitle("Not enough space");
                    alert.showAndWait();

                }else {
                    int width = calculateWidth((int) keyMetadata.getOffset());
                    if (infoPane.getChildren().contains(rectangle)) {
                        infoPane.getChildren().remove(rectangle);
                        rectangle = new Rectangle(37, 36, width, 23);
                        rectangle.setFill(Color.PURPLE);
                        infoPane.getChildren().add(rectangle);
                    } else {
                        rectangle = new Rectangle(37, 36, width, 23);
                        rectangle.setFill(Color.PURPLE);
                        infoPane.getChildren().add(rectangle);
                    }
                    decList = DataUtils.readDecList();
                    if (decList != null && decList.size() > 0) {
                        for (Rectangle rec : decList) {
                            if (infoPane.getChildren().contains(rec))
                                infoPane.getChildren().remove(rec);
                            infoPane.getChildren().add(rec);
                        }
                    } else {
                        decList = new ArrayList<>();
                    }
                    keyMetadata = DataUtils.readKeyMetadata(Util.KEY_METADATA_PATH);
                    sizeLabel.setText(Integer.toString((int) (((new File(Util.KEY_PATH)).length()) / 1000)) + " KB");
                    double free = ((((new File(Util.KEY_PATH)).length()) - keyMetadata.getOffset()));
                    freeLabel.setText(Double.toString(free) + " B");
                }
                infoPane.setVisible(true);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error encrypting file!", ButtonType.OK);
                alert.setTitle("Encryption error");
                alert.showAndWait();
            }
        }
    }

    public void sendActionListener(){
        String from = messageSender.getText();
        String to = messageReciever.getText();
        String subject = messageSubject.getText();
        String text = messageText.getText();
        String password;

        MailService mailService = new MailService();


        if(from.isEmpty() || to.isEmpty() ||
                mailService.isValidEmailAddress(from) == false || mailService.isValidEmailAddress(to) == false){
            if (from.isEmpty()  || mailService.isValidEmailAddress(from) == false) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You must complete the required field with a valid email address!", ButtonType.OK);
                alert.setTitle("No sender email");
                alert.showAndWait();
                messageSender.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
            }

            if (to.isEmpty() || mailService.isValidEmailAddress(to) == false) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "You must complete the required field with a valid email address!", ButtonType.OK);
                alert.setTitle("No receiver email");
                alert.showAndWait();
                messageReciever.setBackground(new Background(new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        }else {
            PasswordDialog dialog = new PasswordDialog();
            dialog.setTitle("Account login");
            dialog.setHeaderText("You must enter your password in order to send mail.");
            dialog.setContentText("Please enter your password:");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){
                password = result.get();
                MailService service = new MailService(from, password);
                service.sendMail(to, subject, text, encPath);
                try {
                    Files.deleteIfExists(Paths.get(encPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR, "Can not send the email!", ButtonType.OK);
                alert.setTitle("No authentication");
                alert.showAndWait();
            }
        }
        messageSender.clear();
        messageReciever.clear();
        messageSubject.clear();
        messageText.clear();
        sendMessagePane.setVisible(false);
        infoPane.setVisible(true);
    }

    public void cancelActionListener(){
        messageSender.clear();
        messageReciever.clear();
        messageSubject.clear();
        messageText.clear();
    }

    public void fromActionListener(){
        messageSender.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public void toActionListener(){
        messageReciever.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public int calculateWidth(int x){
        return (775 * x)/1000;
    }

    public int inverse(int x){
        return (x * 1000)/775;
    }

}
