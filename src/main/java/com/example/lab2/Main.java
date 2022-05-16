package com.example.lab2;

import com.example.lab2.arena.Arena;
import com.example.lab2.arena.Ball;
import com.example.lab2.arena.Hole;
import com.example.lab2.camera.PanAndZoomCamera;
import com.example.lab2.hub.OrientationMap;
import com.example.lab2.timer.Timer;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.util.Duration;
import java.util.Arrays;

public class Main extends Application {
	private static final double WINDOW_WIDTH  = 800;
	private static final double WINDOW_HEIGHT = 800;
	
	private static final double PODIUM_WIDTH  = 2000;
	private static final double PODIUM_HEIGHT = 10;
	private static final double PODIUM_DEPTH  = 2000;
	
	private static final double CAMERA_FAR_CLIP = 100000;
	private static final double CAMERA_Z        = -5000;
	private static final double CAMERA_X_ANGLE  = -45;
	
	private static final double BALL_RADIUS = 50;
	
	private static final double DAMP = 0.999;
	
	private static final double MAX_ANGLE_OFFSET = 30;
	private static final double MAX_ACCELERATION = 400;
	private static final double ARENA_DAMP=0.995;
	private static final int    NUMBER_OF_HOLES = 4;
	private static final double HOLE_RADIUS     = 2 * Main.BALL_RADIUS;
	private static final double HOLE_HEIGHT     = PODIUM_HEIGHT;
	private static final double LIFE_RADIUS=5;


	private Group root;
	private Group hubGroup;
	private Ball  ball;
	private Arena arena;
	private Hole hole;
	private SubScene scene;
	private PanAndZoomCamera camera;
	private Camera birdViewCamera;
	private Group reflector;
	private PointLight pointLight;
	private boolean isLightOn;
	private PhongMaterial reflectorMaterial;
	private Cylinder obstacles[];
	private Cylinder[] coins;
	private Box[] fences;
	private int points;
	private Circle[] lives;
	private Group hubRoot;
	private Text pointsText;
	private OrientationMap orientationMap;
	private Translate ballPosition;

	private void addPoints(int numberOfPoints) {
		this.points += numberOfPoints;
		this.pointsText.setText(Integer.toString(this.points));
	}

	private SubScene createHUBDisplay() {
		this.hubRoot = new Group();
		SubScene subScene = new SubScene(this.hubRoot, 800.0, 800.0);
		this.lives = new Circle[5];
		for (int i = 0; i < this.lives.length; ++i) {
			this.lives[i] = new Circle(Main.LIFE_RADIUS, Color.RED);
			this.lives[i].getTransforms().add(new Translate(2.0 * Main.LIFE_RADIUS + 3 * i * Main.LIFE_RADIUS, 2.0 * Main.LIFE_RADIUS));
		}
		this.hubRoot.getChildren().addAll(this.lives);
		(this.pointsText = new Text(Integer.toString(this.points))).setFill(Color.RED);
		this.pointsText.setFont(new Font(26.0));
		this.pointsText.getTransforms().add(new Translate(800.0 - 3.0 * this.pointsText.getLayoutBounds().getWidth(), this.pointsText.getLayoutBounds().getHeight()));
		this.hubRoot.getChildren().add(this.pointsText);
		this.orientationMap = new OrientationMap(160.0, 160.0);
		this.orientationMap.getTransforms().addAll(new Translate(0.0, 640.0), new Translate(80.0, 80.0));
		this.hubRoot.getChildren().add(this.orientationMap);
		return subScene;
	}

	private void addCoins(){
		PhongMaterial coinMaterial = new PhongMaterial(Color.GOLD);
		this.coins = new Cylinder[4];
		for (int i = 0; i < this.coins.length; ++i) {
			(this.coins[i] = new Cylinder(50.0, 5.0)).setMaterial(coinMaterial);
			final Rotate rotateY = new Rotate(0.0, Rotate.Y_AXIS);
			final Translate translate = new Translate(0.0, -5.0, 0.0);
			this.coins[i].getTransforms().addAll(translate, new Rotate(90.0 * i, Rotate.Y_AXIS), new Translate(500.0, 0.0, 0.0), new Translate(0.0, -55.0, 0.0), rotateY, new Rotate(90.0, Rotate.Z_AXIS));
			Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(rotateY.angleProperty(), 0, Interpolator.LINEAR), new KeyValue(translate.yProperty(), -5.0)), new KeyFrame(Duration.seconds(3.0), new KeyValue(rotateY.angleProperty(), 180, Interpolator.LINEAR), new KeyValue(translate.yProperty(), -100.0)), new KeyFrame(Duration.seconds(6.0), new KeyValue(rotateY.angleProperty(), 360, Interpolator.LINEAR), new KeyValue(translate.yProperty(), -5.0)));
			timeline.setCycleCount(-1);
			timeline.play();
		}
		this.arena.getChildren().addAll(this.coins);

	}
	private boolean reset() {
		int remainingLives = 0;
		for (int i = 0; i < this.lives.length; ++i) {
			if (this.lives[i] != null) {
				++remainingLives;
			}
		}
		if (remainingLives != 0) {
			this.ballPosition.setX(-900.0);
			this.ballPosition.setY(-55.0);
			this.ballPosition.setZ(900.0);
			this.hubRoot.getChildren().remove(this.lives[remainingLives - 1]);
			this.lives[remainingLives - 1] = null;
			this.ball.reset();
			this.arena.reset();
		}
		else {
			Text text = new Text("Kraj igre");
			text.setFill(Color.RED);
			text.setFont(new Font(26.0));
			text.getTransforms().add(new Translate((800.0 - text.getLayoutBounds().getWidth()) / 2.0, (800.0 - text.getLayoutBounds().getHeight()) / 2.0));
			this.hubRoot.getChildren().add(text);
		}
		return remainingLives == 0;
	}

	private void addReflector() {
		this.reflector = new Group();
		this.reflectorMaterial = new PhongMaterial(Color.GRAY);
		Image selfIllumination = new Image(this.getClass().getClassLoader().getResourceAsStream("selfIlluminations.png"));
		this.reflectorMaterial.setSelfIlluminationMap(selfIllumination);
		Box box = new Box(100.0, 100.0, 100.0);
		box.setMaterial(this.reflectorMaterial);
		this.reflector.getChildren().add(box);
		this.pointLight = new PointLight(Color.WHITE);
		this.reflector.getChildren().add(this.pointLight);
		this.reflector.getTransforms().add(new Translate(0.0, -1200.0, 0.0));
		this.root.getChildren().add(this.reflector);
		this.isLightOn = true;
	}

	private void addObstacles() {
		this.obstacles = new Cylinder[4];
		 Image obstacleImage = new Image(this.getClass().getClassLoader().getResourceAsStream("obstacles.jpg"));
		 PhongMaterial obstacleMaterial = new PhongMaterial();
		 obstacleMaterial.setDiffuseMap(obstacleImage);
		for (int i = 0; i < this.obstacles.length; ++i) {
			(this.obstacles[i] = new Cylinder(50.0, 200.0)).setMaterial(obstacleMaterial);
			this.obstacles[i].getTransforms().addAll(new Rotate(90.0 * i, Rotate.Y_AXIS), new Translate(500.0, -105.0, 500.0));
		}
		this.arena.getChildren().addAll(this.obstacles);
	}
	@Override
	public void start ( Stage stage ) throws IOException {
		this.root = new Group ( );

		scene = new SubScene (
				this.root,
				Main.WINDOW_WIDTH,
				Main.WINDOW_HEIGHT,
				true,
				SceneAntialiasing.BALANCED
		);

		Box podium = new Box (
				Main.PODIUM_WIDTH,
				Main.PODIUM_HEIGHT,
				Main.PODIUM_DEPTH
		);
		podium.setMaterial ( new PhongMaterial ( Color.BLUE ) );
		Rotate defaultCameraRotateX = new Rotate(-45.0, Rotate.X_AXIS);
		Translate defaultCameraPosition = new Translate(0.0, 0.0, -5000.0);
		camera = new PanAndZoomCamera(true,defaultCameraPosition,defaultCameraRotateX);
		camera.setFarClip ( Main.CAMERA_FAR_CLIP );
		/*camera.getTransforms ( ).addAll (
				new Rotate ( Main.CAMERA_X_ANGLE, Rotate.X_AXIS ),
				new Translate ( 0, 0, CAMERA_Z )
		);*/

		this.root.getChildren ( ).add ( camera );
		scene.setCamera ( camera );

		Material ballMaterial = new PhongMaterial ( Color.RED );

		this.ballPosition = new Translate(-900.0, -55.0, 900.0);
		(this.birdViewCamera = new PerspectiveCamera(true)).setFarClip(CAMERA_FAR_CLIP);
		final Translate birdViewCameraPosition = new Translate(0.0, -2555.0, 0.0);
		this.birdViewCamera.getTransforms().addAll(birdViewCameraPosition, ballPosition, new Rotate(-90.0, Rotate.X_AXIS));

		this.ball = new Ball ( Main.BALL_RADIUS, ballMaterial, ballPosition );

		double x = ( Main.PODIUM_WIDTH / 2 - 2 * Main.HOLE_RADIUS );
		double z = - ( Main.PODIUM_DEPTH / 2 - 2 * Main.HOLE_RADIUS );

		Translate holePosition = new Translate ( x, -30, z );
		Material holeMaterial = new PhongMaterial ( Color.YELLOW );

		this.hole = new Hole (
				Main.HOLE_RADIUS,
				Main.HOLE_HEIGHT,
				holeMaterial,
				holePosition
		);
		this.fences = new Box[4];
		final PhongMaterial fenceMaterial = new PhongMaterial(Color.BROWN);
		for (int i = 0; i < this.fences.length; ++i) {
			(this.fences[i] = new Box(10.0, 100.0, 1000.0)).setMaterial(fenceMaterial);
			this.fences[i].getTransforms().addAll(new Rotate(i * 90.0, Rotate.Y_AXIS), new Translate(990.0, -55.0, 0.0));
		}
		this.arena = new Arena ( );
		this.arena.getChildren ( ).add ( podium );
		this.arena.getChildren ( ).add ( this.ball );
		this.arena.getChildren ( ).addAll ( this.hole );
		this.arena.getChildren().addAll(fences);
		this.root.getChildren ( ).add ( this.arena );
		addReflector();
		addObstacles();
		addCoins();
		Timer timer = new Timer (
				deltaSeconds -> {
					this.arena.update(ARENA_DAMP);
					this.orientationMap.update(this.arena.getXAngle(), this.arena.getZAngle(), 30.0);
					if (this.ball != null) {
						Arrays.stream(this.obstacles).forEach(obstacle -> this.ball.handleObstacleCollision(obstacle));
						for (int j= 0; j < this.coins.length; ++j) {
							if (this.coins[j] != null && this.ball.handleCoinCollision(this.coins[j])) {
								this.addPoints(5);
								this.arena.getChildren().remove(this.coins[j]);
								this.coins[j] = null;
							}
						}
						Arrays.stream(this.fences).forEach(fence -> this.ball.handleCoinCollision(fence));
						boolean outOfArena = this.ball.update(deltaSeconds, 1000.0, -1000.0, -1000.0, 1000.0, this.arena.getXAngle(), this.arena.getZAngle(), 30.0, 400.0, 0.999);
						boolean isInHole = this.hole.handleCollision(this.ball);
						if (isInHole) {
							this.addPoints(5);
						}
						if ((outOfArena || isInHole) && this.reset()) {
							this.arena.getChildren().remove(this.ball);
							this.ball = null;
						}
					}
					return;
				}
				);
		timer.start ( );
		Image image=new Image(this.getClass().getClassLoader().getResourceAsStream("backgrounds.jpg"));
		ImagePattern imagePattern=new ImagePattern(image);
		Scene mainScene = new Scene(new Group(new Node[] { this.scene, this.createHUBDisplay() }), 800.0, 800.0, true, SceneAntialiasing.BALANCED);
		mainScene.setFill(imagePattern);
		mainScene.addEventHandler ( KeyEvent.ANY, event -> this.arena.handleKeyEvent ( event, Main.MAX_ANGLE_OFFSET ) );
		mainScene.addEventHandler(KeyEvent.ANY, this::handleKeyEvent);
		mainScene.addEventHandler(MouseEvent.ANY, event-> this.camera.handleMouseEvent(event));
		mainScene.addEventHandler(ScrollEvent.ANY, event->this.camera.handleScrollEvent(event));
		stage.setTitle ( "Rolling Ball" );
		stage.setScene ( mainScene );
		stage.show ( );
	}
	private void handleKeyEvent(KeyEvent event){
		if (event.getEventType().equals(KeyEvent.KEY_PRESSED)) {
			if (event.getCode().equals(KeyCode.DIGIT1) || event.getCode().equals(KeyCode.NUMPAD1)) {
				this.scene.setCamera(this.camera);
			}
			else if (event.getCode().equals(KeyCode.DIGIT2) || event.getCode().equals(KeyCode.NUMPAD2)) {
				this.scene.setCamera(this.birdViewCamera);
			}
			else if (event.getCode().equals(KeyCode.DIGIT0) || event.getCode().equals(KeyCode.NUMPAD0)) {
				if (this.isLightOn) {
					this.reflectorMaterial.setSelfIlluminationMap(null);
					this.reflector.getChildren().remove(this.pointLight);
				}
				else {
					final Image selfIllumination = new Image(this.getClass().getClassLoader().getResourceAsStream("selfIlluminations.png"));
					this.reflectorMaterial.setSelfIlluminationMap(selfIllumination);
					this.reflector.getChildren().add(this.pointLight);
				}
				this.isLightOn = !this.isLightOn;
			}
		}
	}
	public static void main ( String[] args ) {
		launch ( );
	}
	}
