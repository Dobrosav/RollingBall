package com.example.lab2.hub;

import javafx.geometry.Point2D;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Rotate;
import javafx.scene.shape.Rectangle;
import javafx.scene.Group;

public class OrientationMap extends Group
{
    private Rectangle hand;
    private Rotate rotate;
    private Scale scale;
    private double width;
    private double height;

    public OrientationMap(final double width, final double height) {
        this.width = width;
        this.height = height;
        final double rectangleHeight = height * 0.01;
        final Rectangle border = new Rectangle(this.width, this.height, Color.GREEN);
        border.getTransforms().add(new Translate(-this.width / 2.0, -this.height / 2.0));
        border.setStroke(Color.RED);
        border.setStrokeWidth(3.0);
        super.getChildren().add(border);
        (this.hand = new Rectangle(1.0, rectangleHeight)).setFill(Color.RED);
        this.rotate = new Rotate(0.0);
        this.scale = new Scale(0.0, 1.0);
        this.hand.getTransforms().addAll(this.rotate, this.scale, new Translate(0.0, -rectangleHeight / 2.0));
        super.getChildren().add(this.hand);
    }

    public void update(final double xAngle, final double zAngle, final double maxAngleOffset) {
        final double xRatio = zAngle / maxAngleOffset;
        final double yRatio = -xAngle / maxAngleOffset;
        final double x = xRatio * this.width / 2.0;
        final double y = yRatio * this.height / 2.0;
        final double length = Math.sqrt(x * x + y * y);
        this.scale.setX(length);
        double angle = new Point2D(x, y).normalize().angle(new Point2D(1.0, 0.0));
        if (xAngle > 0.0) {
            angle = 360.0 - angle;
        }
        this.rotate.setAngle(-angle);
    }
}

