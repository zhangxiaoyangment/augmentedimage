/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ar.sceneform.samples.augmentedimage;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Frame;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.samples.common.helpers.SnackbarHelper;
import com.google.ar.sceneform.ux.ArFragment;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * This application demonstrates using augmented images to place anchor nodes. app to include image
 * tracking functionality.
 */
public class AugmentedImageActivity extends AppCompatActivity {

  private ArFragment arFragment;
  private ImageView fitToScanView;

  // Augmented image and its associated center pose anchor, keyed by the augmented image in
  // the database.
  private final Map<AugmentedImage, AugmentedImageNode> augmentedImageMap = new HashMap<>();
  private PhysicsController physicsController;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
    fitToScanView = findViewById(R.id.image_view_fit_to_scan);

    arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (augmentedImageMap.isEmpty()) {
      fitToScanView.setVisibility(View.VISIBLE);
    }
  }

  /**
   * Registered with the Sceneform Scene object, this method is called at the start of each frame.
   *
   * @param frameTime - time since last frame.
   */
  private void onUpdateFrame(FrameTime frameTime) {
    Frame frame = arFragment.getArSceneView().getArFrame();

    // If there is no frame or ARCore is not tracking yet, just return.
    if (frame == null || frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
      return;
    }

    Collection<AugmentedImage> updatedAugmentedImages =
        frame.getUpdatedTrackables(AugmentedImage.class);
    for (AugmentedImage augmentedImage : updatedAugmentedImages) {
      switch (augmentedImage.getTrackingState()) {
        case PAUSED:
          // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
          // but not yet tracked.
          String text = "Detected Image " + augmentedImage.getIndex();
          SnackbarHelper.getInstance().showMessage(this, text);
          break;

//        case TRACKING:
//          // Have to switch to UI Thread to update View.
//          fitToScanView.setVisibility(View.GONE);
//
//          // Create a new anchor for newly found images.
//          if (!augmentedImageMap.containsKey(augmentedImage)) {
//            AugmentedImageNode node = new AugmentedImageNode(this);
//            node.setImage(augmentedImage);
//            augmentedImageMap.put(augmentedImage, node);
//            arFragment.getArSceneView().getScene().addChild(node);
//          }
//          break;
//
//        case STOPPED:
//          augmentedImageMap.remove(augmentedImage);
//          break;
        case TRACKING:
          // Have to switch to UI Thread to update View.
          fitToScanView.setVisibility(View.GONE);

          // Create a new anchor for newly found images.
          if (!augmentedImageMap.containsKey(augmentedImage)) {
            AugmentedImageNode node = new AugmentedImageNode(this);
            node.setImage(augmentedImage);
            augmentedImageMap.put(augmentedImage, node);
            arFragment.getArSceneView().getScene().addChild(node);

            physicsController = new PhysicsController(this);


          } else {
            // If the image anchor is already created
            AugmentedImageNode node = augmentedImageMap.get(augmentedImage);
            node.updateBallPose(physicsController.getBallPose());

            // Use real world gravity, (0, -10, 0) as gravity
            // Convert to Physics world coordinate (because Maze mesh has to be static)
            // Use it as a force to move the ball
//            Pose worldGravityPose = Pose.makeTranslation(0, -10f, 0);
            float cameraZDir[] = frame.getCamera().getPose().getZAxis();
            Vector3 cameraZVector = new Vector3(cameraZDir[0], cameraZDir[1], cameraZDir[2]);
            Vector3 cameraGravity = cameraZVector.negated().scaled(10);
            Pose worldGravityPose = Pose.makeTranslation(
            cameraGravity.x, cameraGravity.y, cameraGravity.z);

            Pose mazeGravityPose = augmentedImage.getCenterPose().inverse().compose(worldGravityPose);
            float mazeGravity[] = mazeGravityPose.getTranslation();
            physicsController.applyGravityToBall(mazeGravity);

            physicsController.updatePhysics();
          }
          break;

        case STOPPED:
          AugmentedImageNode node = augmentedImageMap.get(augmentedImage);
          augmentedImageMap.remove(augmentedImage);
          arFragment.getArSceneView().getScene().removeChild(node);
          break;
      }
    }
//    float cameraZDir[] = frame.getCamera().getPose().getZAxis();
//    Vector3 cameraZVector = new Vector3(cameraZDir[0], cameraZDir[1], cameraZDir[2]);
//    Vector3 cameraGravity = cameraZVector.negated().scaled(10);
//    Pose worldGravityPose = Pose.makeTranslation(
//            cameraGravity.x, cameraGravity.y, cameraGravity.z);
  }
}
