package com.example.lab2.arena;

import com.example.lab2.Utilities;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.paint.Material;
import javafx.scene.shape.Box;
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
	public void handleCoinCollision(final Box fence) {
		final Bounds ballBounds = this.getBoundsInParent();
		final double ballCenterX = ballBounds.getCenterX();
		final double ballCenterZ = ballBounds.getCenterZ();
		final double ballRadius = this.getRadius();
		final Bounds fenceBounds = fence.getBoundsInParent();
		final double fenceMinX = fenceBounds.getMinX();
		final double fenceMaxX = fenceBounds.getMaxX();
		final double fenceMinZ = fenceBounds.getMinZ();
		final double fenceMaxZ = fenceBounds.getMaxZ();
		final double closestX = Utilities.clamp(ballCenterX, fenceMinX, fenceMaxX);
		final double closestZ = Utilities.clamp(ballCenterZ, fenceMinZ, fenceMaxZ);
		final double dx = closestX - ballCenterX;
		final double dz = closestZ - ballCenterZ;
		final double distanceSquared = dx * dx + dz * dz;
		final double radiusSquared = ballRadius * ballRadius;
		final boolean collisionDetected = distanceSquared < radiusSquared;
		if (collisionDetected) {
			if (closestX == fenceMaxX || closestX == fenceMinX) {
				this.speed = new Point3D(-this.speed.getX(), 0.0, this.speed.getZ());
			}
			else if (closestZ == fenceMaxZ || closestZ == fenceMinZ) {
				this.speed = new Point3D(this.speed.getX(), 0.0, -this.speed.getZ());
			}
		}
	}

	public void reset() {
		this.speed = new Point3D(0.0, 0.0, 0.0);
	}

	public boolean handleCoinCollision(final Cylinder coin) {
		final Bounds ballBounds = this.getBoundsInParent();
		final Bounds coinBounds = coin.getBoundsInParent();
		return ballBounds.intersects(coinBounds);
	}
}
