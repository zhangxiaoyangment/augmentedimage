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

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import java.util.concurrent.CompletableFuture;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ShapeFactory;
/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
@SuppressWarnings({"AndroidApiChecker"})
public class AugmentedImageNode extends AnchorNode {

  private static final String TAG = "AugmentedImageNode";

  // The augmented image represented by this node.
  private AugmentedImage image;

  // Models of the 4 corners.  We use completable futures here to simplify
  // the error handling and asynchronous loading.  The loading is started with the
  // first construction of an instance, and then used when the image is set.
  private static CompletableFuture<ModelRenderable> ulCorner;
  private static CompletableFuture<ModelRenderable> urCorner;
  private static CompletableFuture<ModelRenderable> lrCorner;
  private static CompletableFuture<ModelRenderable> llCorner;
  private float maze_scale = 0.0f;

  private ModelRenderable ballRenderable;

//  public AugmentedImageNode(Context context) {
//    // Upon construction, start loading the models for the corners of the frame.
//    if (ulCorner == null) {
//      ulCorner =
//          ModelRenderable.builder()
//              .setSource(context, Uri.parse("models/frame_upper_left.sfb"))
//              .build();
//      urCorner =
//          ModelRenderable.builder()
//              .setSource(context, Uri.parse("models/frame_upper_right.sfb"))
//              .build();
//      llCorner =
//          ModelRenderable.builder()
//              .setSource(context, Uri.parse("models/frame_lower_left.sfb"))
//              .build();
//      lrCorner =
//          ModelRenderable.builder()
//              .setSource(context, Uri.parse("models/frame_lower_right.sfb"))
//              .build();
//    }
//  }
// Add a member variable to hold the maze model.
private Node mazeNode;

  // Add a variable called mazeRenderable for use with loading
  // GreenMaze.sfb.
  private CompletableFuture<ModelRenderable> mazeRenderable;


  private android.net.Uri chosenImageUri = null;
  private static final int REQUEST_CODE_CHOOSE_IMAGE = 1;

  // Replace USE_SINGLE_IMAGE with this value.
  private static final boolean USE_SINGLE_IMAGE = true;

  private Node ballNode;

  // Replace the definition of the AugmentedImageNode function with the
  // following code, which loads GreenMaze.sfb into mazeRenderable.
  public AugmentedImageNode(Context context) {
    mazeRenderable =
            ModelRenderable.builder()
                    .setSource(context, Uri.parse("green_maze.sfb"))
                    .build();
    // Add this code to the end of this function.
    MaterialFactory.makeOpaqueWithColor(context, new Color(android.graphics.Color.RED))
            .thenAccept(
                    material -> {
                      ballRenderable =
                              ShapeFactory.makeSphere(0.005f, new Vector3(0, 0, 0), material); });//球的半径
  }

  /**
   * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
   * created based on an Anchor created from the image. The corners are then positioned based on the
   * extents of the image. There is no need to worry about world coordinates since everything is
   * relative to the center of the image, which is the parent node of the corners.
   */
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  public void setImage(AugmentedImage image) {
    this.image = image;

    // If any of the models are not loaded, then recurse when all are loaded.
//    if (!ulCorner.isDone() || !urCorner.isDone() || !llCorner.isDone() || !lrCorner.isDone()) {
//      CompletableFuture.allOf(ulCorner, urCorner, llCorner, lrCorner)
//          .thenAccept((Void aVoid) -> setImage(image))
//          .exceptionally(
//              throwable -> {
//                Log.e(TAG, "Exception loading", throwable);
//                return null;
//              });
//    }
//
//    // Set the anchor based on the center of the image.
//    setAnchor(image.createAnchor(image.getCenterPose()));
//
//    // Make the 4 corner nodes.
//    Vector3 localPosition = new Vector3();
//    Node cornerNode;
//
//    // Upper left corner.
//    localPosition.set(-0.5f * image.getExtentX(), 0.0f, -0.5f * image.getExtentZ());
//    cornerNode = new Node();
//    cornerNode.setParent(this);
//    cornerNode.setLocalPosition(localPosition);
//    cornerNode.setRenderable(ulCorner.getNow(null));
//
//    // Upper right corner.
//    localPosition.set(0.5f * image.getExtentX(), 0.0f, -0.5f * image.getExtentZ());
//    cornerNode = new Node();
//    cornerNode.setParent(this);
//    cornerNode.setLocalPosition(localPosition);
//    cornerNode.setRenderable(urCorner.getNow(null));
//
//    // Lower right corner.
//    localPosition.set(0.5f * image.getExtentX(), 0.0f, 0.5f * image.getExtentZ());
//    cornerNode = new Node();
//    cornerNode.setParent(this);
//    cornerNode.setLocalPosition(localPosition);
//    cornerNode.setRenderable(lrCorner.getNow(null));
//
//    // Lower left corner.
//    localPosition.set(-0.5f * image.getExtentX(), 0.0f, 0.5f * image.getExtentZ());
//    cornerNode = new Node();
//    cornerNode.setParent(this);
//    cornerNode.setLocalPosition(localPosition);
//    cornerNode.setRenderable(llCorner.getNow(null));
    if (!mazeRenderable.isDone()) {
      CompletableFuture.allOf(mazeRenderable)
              .thenAccept((Void aVoid) -> setImage(image))
              .exceptionally(
                      throwable -> {
                        Log.e(TAG, "Exception loading", throwable);
                        return null;
                      });
      return;
    }
      // Set the anchor based on the center of the image.
      setAnchor(image.createAnchor(image.getCenterPose()));

      mazeNode = new Node();
      mazeNode.setParent(this);
      mazeNode.setRenderable(mazeRenderable.getNow(null));

    final float maze_edge_size = 492.65f;
    final float max_image_edge = Math.max(image.getExtentX(), image.getExtentZ());
    maze_scale = max_image_edge / maze_edge_size;
    // Scale Y an extra 10 times to lower the maze wall.
    mazeNode.setLocalScale(new Vector3(maze_scale, maze_scale * 0.5f, maze_scale));//使迷宫高度缩小0.5倍

    Node ballNode = new Node();
    ballNode.setParent(this);
    ballNode.setRenderable(ballRenderable);
    ballNode.setLocalPosition(new Vector3(0, 0.1f, 0));

    ballNode.setLocalScale(new Vector3(
            6.5f * maze_scale / 0.01f,
            6.5f * maze_scale / 0.01f,
            6.5f * maze_scale / 0.01f));

  }
  public void updateBallPose(Pose pose) {
    if (ballNode == null) {
      return;
    }

    ballNode.setLocalPosition(new Vector3(pose.tx() * maze_scale, pose.ty()* maze_scale, pose.tz()* maze_scale));
    ballNode.setLocalRotation(new Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw()));
  }

  public AugmentedImage getImage() {
    return image;
  }
}
