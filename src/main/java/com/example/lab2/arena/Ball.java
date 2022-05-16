package com.example.lab2.arena;

import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;

public class Ball extends Sphere {
	private Translate position;
	private Point3D speed;
	
	public Ball ( double radius, Material material, Translate position ) {
		super ( radius );
		super.setMaterial ( material );
		
		this.position = position;
		
		super.getTransforms ( ).add ( this.position );
		
		this.speed = new Point3D ( 0, 0, 0 );
	}
	
	public boolean update (
			double deltaSeconds,
			double top,
			double bottom,
			double left,
			double right,
			double xAngle,
			double zAngle,
			double maxAngleOffset,
			double maxAcceleration,
			double damp
	) {
		double newPositionX = this.position.getX ( ) + this.speed.getX ( ) * deltaSeconds;
		double newPositionZ = this.position.getZ ( ) + this.speed.getZ ( ) * deltaSeconds;
		
		this.position.setX ( newPositionX );
		this.position.setZ ( newPositionZ );
		
		double accelerationX = maxAcceleration * zAngle / maxAngleOffset;
		double accelerationZ = -maxAcceleration * xAngle / maxAngleOffset;
		
		double newSpeedX = ( this.speed.getX ( ) + accelerationX * deltaSeconds ) * damp;
		double newSpeedZ = ( this.speed.getZ ( ) + accelerationZ * deltaSeconds ) * damp;
		
		this.speed = new Point3D ( newSpeedX, 0, newSpeedZ );
		
		boolean xOutOfBounds = ( newPositionX > right ) || ( newPositionX < left );
		boolean zOutOfBounds = ( newPositionZ > top ) || ( newPositionZ < bottom );
		
		return xOutOfBounds || zOutOfBounds;
	}
	public void handleObstacleCollision(Cylinder obstacle) {
		Bounds ballBounds = this.getBoundsInParent();
		double ballX = ballBounds.getCenterX();
		 double ballZ = ballBounds.getCenterZ();
		 double ballRadius = super.getRadius();
		 Bounds obstacleBounds = obstacle.getBoundsInParent();
		 double obstacleX = obstacleBounds.getCenterX();
		 double obstacleZ = obstacleBounds.getCenterZ();
		 double obstacleRadius = obstacle.getRadius();
		 double dx = ballX - obstacleX;
		 double dz = ballZ - obstacleZ;
		 double dr = ballRadius + obstacleRadius;
		 double distanceSquared = dx * dx + dz * dz;
		 double radiusSquared = dr * dr;
		 final boolean collided = distanceSquared < radiusSquared;
		 if (collided) {
			final Point3D normal = new Point3D(ballX - obstacleX, 0.0, ballZ - obstacleZ).normalize();
			final double speedDotNormal = this.speed.dotProduct(normal);
			this.speed = this.speed.subtract(normal.multiply(2.0 * speedDotNormal));
		}
	}
}
