package com.example.lab2;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class Form extends Application {
    private Text trava=new Text(80,10, "Crvena");
    private Text pesak=new Text(200,10, "Zlatna");
    private Text beton=new Text(290,10, "Crna");

    private Text top1=new Text(80,100 ,"Teren1");
    private Text top2=new Text(200,100, "Teren2");
    private Text top3=new Text(290,100, "Teren3");

    private Button btntrava;
    private Button btnpesak;
    private Button btnBeteon;

    private Button btntop1;
    private Button btntop2;
    private Button btntop3;


    private Group root;
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.root=new Group();
        this.root.getChildren().addAll(trava,beton,pesak,top1,top3,top2);
        btntrava=new Button("Crvena");
        btntrava.setLayoutX(80);
        btntrava.setLayoutY(20);
        btntrava.setOnAction(actionEvent -> {
            SuperMain.parameters[0]="red";
        });
        btnBeteon=new Button("Crna");
        btnBeteon.setLayoutX(290);
        btnBeteon.setLayoutY(20);
        btnBeteon.setOnAction(actionEvent -> {
            SuperMain.parameters[0]="black";
        });
        btnpesak=new Button("Zlatna");
        btnpesak.setLayoutX(200);
        btnpesak.setLayoutY(20);
        btnpesak.setOnAction(actionEvent -> {
            SuperMain.parameters[0]="gold";
        });
        btntop1=new Button("Teren1");
        btntop1.setLayoutX(80);
        btntop1.setLayoutY(110);
        btntop1.setOnAction(actionEvent -> {
            SuperMain.parameters[1]="teren1";
        });
        btntop3=new Button("Teren3");
        btntop3.setLayoutX(290);
        btntop3.setLayoutY(110);
        btntop3.setOnAction(actionEvent -> {
            SuperMain.parameters[1]="teren3";
        });
        btntop2=new Button("Teren2");
        btntop2.setLayoutX(200);
        btntop2.setLayoutY(110);
        btntop2.setOnAction(actionEvent -> {
            SuperMain.parameters[1]="teren2";
        });
        Button btntart=new Button("Start");
        btntart.setLayoutX(175);
        btntart.setLayoutY(150);
        btntart.setOnAction(actionEvent -> {
            System.out.println("start");
            primaryStage.hide();
            Main main = new Main();
            try {
                main.start(new Stage());
            } catch (IOException e) {

                e.printStackTrace();
            }
        });
        this.root.getChildren().addAll(btntrava,btnBeteon,btnpesak,btntop2,btntop1,btntop3,btntart);
        Scene scene= new Scene(root,350,300);
        primaryStage.setTitle("Hello!");
        primaryStage.setScene(scene);
        primaryStage.show();

    }
    public static void main(String args[]){
        launch();
    }

}