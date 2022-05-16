package com.example.lab2;

import com.example.lab2.arena.Arena;
import com.example.lab2.arena.Ball;
import com.example.lab2.arena.Hole;
import com.example.lab2.camera.PanAndZoomCamera;
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

import javafx.scene.shape.Cylinder;
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
	
	private Group root;
	private Group hubGroup;
	private Ball  ball;
	private Arena arena;
	private Hole hole;
	private Scene scene;
	private PanAndZoomCamera camera;
	private Camera birdViewCamera;
	private Group reflector;
	private PointLight pointLight;
	private boolean isLightOn;
	private PhongMaterial reflectorMaterial;
	private Cylinder obstacles[];
	private Cylinder[] coins;
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

		scene = new Scene (
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
		Translate ballPosition = new Translate (
				- ( Main.PODIUM_WIDTH / 2 - 2 * Main.BALL_RADIUS ),
				- ( Main.BALL_RADIUS + Main.PODIUM_HEIGHT / 2 ),
				Main.PODIUM_DEPTH / 2 - 2 * Main.BALL_RADIUS
		);
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
		
		this.arena = new Arena ( );
		this.arena.getChildren ( ).add ( podium );
		this.arena.getChildren ( ).add ( this.ball );
		this.arena.getChildren ( ).addAll ( this.hole );
		
		this.root.getChildren ( ).add ( this.arena );
		addReflector();
		addObstacles();
		addCoins();
		Timer timer = new Timer (
				deltaSeconds -> {
					this.arena.update(ARENA_DAMP);
					if (this.ball != null)
						Arrays.stream(this.obstacles).forEach(obstacle -> this.ball.handleObstacleCollision(obstacle));
					if ( Main.this.ball != null ) {
						boolean outOfArena = Main.this.ball.update (
								deltaSeconds,
								Main.PODIUM_DEPTH / 2,
								-Main.PODIUM_DEPTH / 2,
								-Main.PODIUM_WIDTH / 2,
								Main.PODIUM_WIDTH / 2,
								this.arena.getXAngle ( ),
								this.arena.getZAngle ( ),
								Main.MAX_ANGLE_OFFSET,
								Main.MAX_ACCELERATION,
								Main.DAMP
						);
						
						boolean isInHole = this.hole.handleCollision ( this.ball );
						
						if ( outOfArena || isInHole ) {
							this.arena.getChildren ( ).remove ( this.ball );
							Main.this.ball = null;
						}
					}
				}
		);
		timer.start ( );
		Image image=new Image(this.getClass().getClassLoader().getResourceAsStream("backgrounds.jpg"));
		ImagePattern imagePattern=new ImagePattern(image);
		scene.setFill(imagePattern);
		scene.addEventHandler ( KeyEvent.ANY, event -> this.arena.handleKeyEvent ( event, Main.MAX_ANGLE_OFFSET ) );
		this.scene.addEventHandler(KeyEvent.ANY, this::handleKeyEvent);
		scene.addEventHandler(MouseEvent.ANY, event-> this.camera.handleMouseEvent(event));
		scene.addEventHandler(ScrollEvent.ANY, event->this.camera.handleScrollEvent(event));
		stage.setTitle ( "Rolling Ball" );
		stage.setScene ( scene );
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
		}
	}
	public static void main ( String[] args ) {
		launch ( );
	}
	}
