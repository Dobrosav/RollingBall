
package com.example.lab2.camera;

import com.example.lab2.Utilities;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.scene.PerspectiveCamera;

public class PanAndZoomCamera extends PerspectiveCamera
{
    private Translate position;
    private Rotate rotateX;
    private Rotate rotateY;
    private double previousX;
    private double previousY;
    
    public PanAndZoomCamera(boolean fixedEyeAtCameraZero, Translate position, Rotate rotateX) {
        super(fixedEyeAtCameraZero);
        this.position = position;
        this.rotateX = rotateX;
        this.rotateY = new Rotate(0.0, Rotate.Y_AXIS);
        super.getTransforms().addAll(this.rotateY, this.rotateX, this.position);
    }
    
    public void handleMouseEvent( MouseEvent event) {
        if (event.getEventType().equals(MouseEvent.MOUSE_PRESSED)) {
            this.previousX = event.getSceneX();
            this.previousY = event.getSceneY();
        }
        else if (event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)) {
            final double dx = event.getSceneX() - this.previousX;
            final double dy = event.getSceneY() - this.previousY;
            this.previousX = event.getSceneX();
            this.previousY = event.getSceneY();
            final double signX = (dx > 0.0) ? 1.0 : -1.0;
            final double signY = (dy > 0.0) ? 1.0 : -1.0;
            final double newAngleX = this.rotateX.getAngle() - signY * 0.5;
            final double newAngleY = this.rotateY.getAngle() - signX * 0.5;
            this.rotateX.setAngle(Utilities.clamp(newAngleX, -90.0, 0.0));
            this.rotateY.setAngle(newAngleY);
        }
    }
    
    public void handleScrollEvent(final ScrollEvent event) {
        if (event.getDeltaY() > 0.0) {
            this.position.setZ(this.position.getZ() + 30.0);
        }
        else {
            this.position.setZ(this.position.getZ() - 30.0);
        }
    }
}
