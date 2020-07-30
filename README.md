# augmentedimage
这是一个ARCode迷宫项目
1.概述
ARCore是一个用于在Android上构建增强现实应用程序的平台。增强图像使您能够创建能够识别预注册图像并将虚拟内容锚定在其上的AR应用程序。
该代码实验室将指导您修改现有的ARCore应用，以合并已移动或固定到位的增强图像。

你会建立什么？
在此代码实验室中，您将基于现有的ARCore示例应用程序构建。到代码实验室结束时，您的应用将：
·能够检测图像目标并在目标上附加虚拟迷宫。下面的示例可视化
·只要在视图中就可以跟踪移动目标
您将学到什么
√如何使用Sceneform在ARCore中使用增强图像。
√如何评估ARCore识别图像的能力。
√如何在图像上附加虚拟内容并跟踪其运动。
你需要什么
·在启动此代码实验室之前，请确保您拥有所需的一切：
·一个支持ARCORE设备，通过USB电缆连接到您的开发机器。
·ARCore 1.9或更高版本。该APK通常会通过Play商店自动安装在设备上。如果您的设备不具备所需的ARCore版本，则可以随时从Play商店安装
·具有Android Studio（v3.1或更高版本）的开发机。
·可以访问Internet，以便在开发过程中下载库。
现在您已经准备就绪，让我们开始吧！
对Google I / O的早期SDK访问
注意：如果您在Google I / O 2019的一周内遵循此代码实验室，则很可能需要手动将ARCore 1.9.0侧面加载到开发设备上。这样可以防止您的应用提示您更新ARCore的已安装版本，即使该设备可能尚未通过Google Play商店在您的设备上提供。
侧面加载ARCore APK：
1.确认您使用的是ARCore支持的设备。
2.下载ARCore_1.9.0.apk从ARCORE SDK为Android 发布页面。
3.运行adb install -r ARCore_1.9.0.apk以将ARCore安装到您的设备上。

2.下载Sceneform SDK并构建示例项目
我们将从GitHub sceneform-android-sdk-v1.9.0.zip下载ARCore Sceneform SDK 开始。将其解压缩到您的首选位置。解压缩文件夹将被称为sceneform-android-sdk-1.9.0。
启动Android Studio，然后点击打开现有的Android Studio项目。

导航到以下解压缩的文件夹：
sceneform-android-sdk-1.9.0/samples/augmentedimage/
点击打开。
等待Android Studio完成同步项目。如果您的Android Studio没有所需的组件，则可能会失败，并显示消息“安装缺少的平台并同步项目”。按照说明解决问题。
现在您有了一个正在运行的ARCore应用程序项目，让我们对其进行测试运行。
将您的ARCore设备连接到开发机器，然后使用菜单Run>Run'app'在设备上运行调试版本。在提示您选择要从哪个设备运行的对话框中，
选择连接的设备，然后单击确定。


本示例项目使用targetSdkVersion 28。如果您遇到诸如的构建错误Failed to find Build Tools revision 28.0.3，请按照Android Studio中所述的说明下载并安装所需的Android Build Tools版本。
如果一切成功，则示例应用程序将在设备上启动，并提示您允许“增强图像”拍摄照片和视频。点击允许以授予权限。
让我们给示例应用程序一个图像以供查看。
返回Android Studio中的“project”窗口中，导航至app> asset，然后双击该文件default.jpg以将其打开。

将设备相机对准屏幕上的地球图像，然后按照说明将要扫描的图像放入十字准线中。
图像框架将覆盖在图像顶部，如下所示：

如果应用程序无法识别该图像，请尝试以下解决方法：
·将设备移近一点，以便图像占据屏幕宽度的大约三分之一。
·面对图像时，相对于图像水平移动设备。
接下来，我们将对示例应用程序进行一些小的改进。
3.改进示例项目
在此示例项目中，我们可以轻松地改进一个区域：图像看起来不清晰。
在ARCore会话中启用自动对焦
图像不清晰的原因是，默认情况下，ARCore相机会话的焦点固定为一米。通过配置ARCore会话以使相机自动对焦，可以很容易地解决此问题。为此，请Config.FocusMode.AUTO在会话配置中使用参数。
在中AugmentedImageFragment.java，向getSessionConfiguration函数添加新行。
AugmentedImageFragment.java

@Override
protected Config getSessionConfiguration(Session session) {
  Config config = super.getSessionConfiguration(session);

  // Use setFocusMode to configure auto-focus.
  config.setFocusMode(Config.FocusMode.AUTO);


  if (!setupAugmentedImageDatabase(config, session)) {
    SnackbarHelper.getInstance()
        .showError(getActivity(), "Could not setup augmented image database");
  }
  return config;
}

现在，让我们对其进行测试。这次我们应该能够在预览窗口中清晰地看到图像。像这样：

4.导入迷宫模型
正如我们在本代码实验室开始时提到的那样，我们将在图像上有一个迷宫游戏。首先，让我们在https://poly.google.com/上找到一个迷宫模型，该模型包含许多CC-BY许可免费提供的3D模型。
对于此代码实验室，我们将使用Evol的“ Circle Maze-Green”，并根据CC-BY 3.0进行许可。

请按照以下步骤下载模型并将其放入Android Studio：
1.导航到模型的Poly页面。
2.单击Download，然后选择OBJ File。
这将下载一个名为的文件green-maze.zip。
3.解压缩green-maze.zip内容并将其复制到以下位置：
sceneform-android-sdk-1.9.0/samples/augmentedimage/app/sampledata/green-maze/
4.在Android Studio中，导航至app> sampledata> green-maze。
此文件夹中应该有两个文件：GreenMaze.obj和GreenMaze.mtl。

接下来，我们将使用Android Studio的Sceneform插件将此OBJ文件导入Android Studio。
添加适用于Android Studio的Sceneform插件
如果您的Android Studio上尚未安装Sceneform插件，那么该了。
转到Android Studio首选项（在Windows上为“Flie”>“Settings”，在macOS上为“Android Studio”>“Preferences”）。单击Plugins插件，然后浏览存储库以获取Google Sceneform Tools（测试版）

安装插件。
导入迷宫模型
我们将使用Sceneform插件将转换任务添加到build.gradle文件中并预览模型。
在Project窗口中：
转到app> sampledata> green-maze> GreenMaze.obj。
右键单击GreenMaze.obj，然后选择“New”>“Sceneform Asset”。
这将打开“导入向导”对话框，并将所有内容初始化为默认值。

保留默认值，然后单击“Finish”以开始导入模型。如果看到关于build.gradle和SFB文件为只读的警告，则可以忽略它。

注意：如果导入模型失败，请参考以下方法解决。
1.将以下几行手动添加到build.gradle（app）文件的底部：
apply plugin: 'com.google.ar.sceneform.plugin'
sceneform.asset('sampledata/maze/green_maze.obj', 
               'default',                           'sampledata/models/green_maze.sfa',         'src/main/res/raw/green_maze')
2.重建您的项目，并且可正确渲染可渲染对象-请确认green_maze.sfb是否已出现在“ res / raw”文件夹中。

导入完成后，它将.sfb在编辑器中打开文件，它实际上是.sfa文件内容。Sceneform查看器也被打开，显示导入的模型：

您可以通过调整.sfa文件中的值来更改外观。我们将更改设置，将metallic其设置为0并将roughness设置为0.4。在GreenMaze.sfa文件中进行这些更改，然后保存。
GreenMaze.sfa

Metallic: 0
roughness: 0.4
Android Studio会构建一个新.sfb文件并刷新“查看器”面板。使用这些新设置，这就是迷宫的样子。

5.在图像上显示迷宫模型
显示迷宫模型
现在，我们有了一个新的3D模型，GreenMaze.sfb让我们在图像上方显示它。
在中AugmentedImageNode.java，添加一个名为的成员变量mazeNode来保存迷宫模型。因为迷宫是由图像控制的，所以mazeNode将AugmentedImageNode类放入内部是有意义的。
添加一个名为的变量mazeRenderable以帮助加载GreenMaze.sfb。
在AugmentedImageNode构造函数中，加载GreenMaze.sfb到中mazeRenderable。
在该setImage功能中，检查是否mazeRenderable已完成加载。
在setImage函数中，初始化mazeNode，并设置其父项和可渲染对象。
在中AugmentedImageNode.java，进行这些更改。
// Add a member variable to hold the maze model. 
  private Node mazeNode;

  // Add a variable called mazeRenderable for use with loading 
  // GreenMaze.sfb.
  private CompletableFuture<ModelRenderable> mazeRenderable;

  // Replace the definition of the AugmentedImageNode function with the
  // following code, which loads GreenMaze.sfb into mazeRenderable.
  public AugmentedImageNode(Context context) {
    mazeRenderable =
          ModelRenderable.builder()
              .setSource(context, Uri.parse("GreenMaze.sfb"))
              .build();
  }

  // Replace the definition of the setImage function with the following
  // code, which checks if mazeRenderable has completed loading.

  public void setImage(AugmentedImage image) {
    this.image = image;

    // Initialize mazeNode and set its parents and the Renderable. 
    // If any of the models are not loaded, process this function 
    // until they all are loaded.
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
  }

好的，似乎我们只是进行了足够的代码更改，以default.jpg在地球图片上方显示迷宫。在运行它之前，我们仍然需要调整迷宫模型的大小。
我们想要迷宫有多大？对于此代码实验室，假设我们希望迷宫与图像一样大。AugmentedImage允许ARCore支持的设备评估图像的大小。我们可以通过getExtentX()和getExtentZ()函数获得评估后的图片大小。
由于Sceneform插件会在导入过程中自动调整模型的大小，因此我们需要在中更改默认比例GreenMaze.sfa。让我们将比例更改为1，以使迷宫的大小完全与其OBJ文件中的大小相同。
在中GreenMaze.sfa，进行更改。

Scale: 1,
接下来，我们需要知道迷宫模型的原始大小。我们不会在此代码实验室中这样做，我只是在此处给出值。迷宫模型的尺寸为492.65x120x492.65。因此，我们需要将迷宫模式的比例设置为image_size / 492.65。
找出obj模型大小的一种方法是在文本编辑器中打开GreenMaze.obj，获取以v，<x>，<y>，<z>开头的所有行，并获取的最大值和最小值。每个组件。然后可以将大小计算为差：X的大小= X的最大值-X的最小值。对于重复的计算，我们可以编写一个程序来做到这一点；在这种情况下，因为这只是一次性任务，所以我只使用了Google工作表。
为此，我们添加一个名为的新成员变量maze_scale，AugmentedImageNode.java并使用它来存储迷宫的比例。我们可以在setImage函数中分配它的值。
另外，由于迷宫墙对于我们的代码实验室来说仍然有点高，让我们将其缩放0.1倍。这样可以降低墙壁的高度，以便在接触迷宫的底部时可以看到球。
在中AugmentedImageNode.java，更改这些代码。
private float maze_scale = 0.0f;

    ...
public void setImage(AugmentedImage image) {
    // At the end of this function, add code for scaling the maze Node.

    ...

    // Make sure the longest edge fits inside the image.
    final float maze_edge_size = 492.65f;
    final float max_image_edge = Math.max(image.getExtentX(), image.getExtentZ());
    maze_scale = max_image_edge / maze_edge_size;

    // Scale Y an extra 10 times to lower the maze wall.
    mazeNode.setLocalScale(new Vector3(maze_scale, maze_scale * 0.1f, maze_scale));

    ...}
好的，让我们尝试在您的ARCore支持的设备上运行。现在，迷宫大小应与图像大小相同。

6.在迷宫里放一个球
现在，让我们添加一个在迷宫内滚动的球。在Sceneform中，这样做很容易：我们将使用ShapeFactory在默认材质中创建一个彩色的球。
首先，让我们在图像顶部的固定距离（0.1米）处添加一个半径为0.01米的球。
将此代码添加到中AugmentedImageNode.java。
// Add these lines at the top with the rest of the imports.import com.google.ar.sceneform.rendering.Color;import com.google.ar.sceneform.rendering.MaterialFactory;import com.google.ar.sceneform.rendering.ShapeFactory;

  // Add a ModelRenderable called ballRenderable.
  private ModelRenderable ballRenderable;

  // In the AugmentedImageNode function, you're going to add some code
  // at the end of the function. (See below.)
  public AugmentedImageNode(Context context) {

  ...

    // Add this code to the end of this function.
    MaterialFactory.makeOpaqueWithColor(context, new Color(android.graphics.Color.RED))
        .thenAccept(
            material -> {
              ballRenderable =
                  ShapeFactory.makeSphere(0.01f, new Vector3(0, 0, 0), material); });
  }

  // At the end of the setImage function, you're going to add some code
  // to add the ball. (See below.)
  public void setImage(AugmentedImage image) {

    ...

    // Add the ball at the end of the setImage function.
    Node ballNode = new Node();
    ballNode.setParent(this);
    ballNode.setRenderable(ballRenderable);
    ballNode.setLocalPosition(new Vector3(0, 0.1f, 0));

    ...
  }

然后，让我们尝试在设备上运行它。我们应该看到这样的东西。

7.使用您的任何图像作为目标
这个代码实验室是关于移动增强图像的，因此理想情况下，我们应该使用易于移动到多个固定位置的东西，例如可调节的显示器臂，附加的手机，或者具有坚硬表面和带有可用图像的纹理的对象，例如杂志或产品包装。

注意：在手机屏幕上显示时，迷宫有点小，因此可以随意调整maze_scale（在“Show the maze model on image”部分中介绍的）值以使其变大。
在此代码实验室中，我们将从设备存储中选择一个图像作为目标图像。（再次说明此部分是可选的，因此，如果您手边没有什么需要处理的内容，则可以跳到下一部分。）
现在，如果您确实拥有一些方便的东西，并且具有足以被识别的独特纹理，我们可以为其拍照并用作目标图像。
您的照片必须具有与扫描相同或相似的质量，因此在拍摄照片时请注意以下提示：
1.将纹理放在平坦的表面上。
2.尝试直接在上面有图像的对象上方，以使图像具有矩形或正方形形状。
3.尽量避免光线或阳光刺眼。

注意：可以使用“全能扫描王”应用程序来帮助您对笔记本进行良好的扫描。

例如，假设我要将这个漂亮的笔记本用作目标图像。

我可能会使用PhotoScan对正面进行高质量的扫描。我得到的结果是这张照片。


让我们将这张照片保存在设备中。在我们的代码实验室应用程序中，开始任何操作之前，我们将提示用户选择图像。
在中AugmentedImageFragment.java，进行这些更改。

// Add a Uri that stores the path of the target image chosen from
  // device storage.
  private android.net.Uri chosenImageUri = null;
  private static final int REQUEST_CODE_CHOOSE_IMAGE = 1;

  // Replace USE_SINGLE_IMAGE with this value.
  private static final boolean USE_SINGLE_IMAGE = true;

  // At the end of the AugmentedImageFragment.onAttach function, call 
  // chooseNewImage. 
  public void onAttach(Context context) {

    ...

    chooseNewImage();
  }

   ...
 
  // Replace the loadAugmentedImageBitmap function with this new code
  // that attempts to use the image chosen by the user. 
  private Bitmap loadAugmentedImageBitmap(AssetManager assetManager) {
    if (chosenImageUri == null) {
      try (InputStream is = assetManager.open(DEFAULT_IMAGE_NAME)) {
        return BitmapFactory.decodeStream(is);
      } catch (IOException e) {
        Log.e(TAG, "IO exception loading augmented image bitmap.", e);
      }
    } else {
      try (InputStream is = getContext().getContentResolver().openInputStream(chosenImageUri)) {
        return BitmapFactory.decodeStream(is);
      } catch (IOException e) {
        Log.e(TAG, "IO exception loading augmented image bitmap from storage.", e);
      }
    }
    return null;
  }

  // Add a new function that prompts the user to choose an image from
  // device storage.
  void chooseNewImage() {
    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
    intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
    intent.setType("image/*");
    startActivityForResult(
        android.content.Intent.createChooser(intent, "Select target augmented image"),
        REQUEST_CODE_CHOOSE_IMAGE);
  }

  // Add a new onActivityResult function to handle the user-selected
  // image, and to reconfigure the ARCore session in the internal
  // ArSceneView.
  @Override
  public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    try {
      if (resultCode == android.app.Activity.RESULT_OK) {
        if (requestCode == REQUEST_CODE_CHOOSE_IMAGE) {
          // Get the Uri of target image
          chosenImageUri = data.getData();

          // Reconfig ARCore session to use the new image
          Session arcoreSession = getArSceneView().getSession();
          Config config = getSessionConfiguration(arcoreSession);
          config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
          arcoreSession.configure(config);
        }
      }
    } catch (Exception e) {
      Log.e(TAG, "onActivityResult - target image selection error ", e);
    }
  }

现在，假设我们已经在相机存储中使用了不错的目标图像（也许您是用PhotoScan拍摄的），让我们在设备上运行该应用进行测试。
在启动时，该应用程序显示要选择的图像列表。
导航到目标图像。点击以选择它。根据您的Android版本和版本，此用户界面外观可能有所不同。
该应用程序以AR模式启动，显示将扫描图像适合十字准线的说明。
我们可以将相机对准实际物体进行检测。如果应用正确识别了图像，则迷宫应出现在图像上。
迷宫出现在图像上之后，在对象周围移动并注意迷宫如何随其移动。

确定目标图像质量
为了识别图像，ARCore依靠图像中的视觉功能。并非所有图像都具有相同的质量，并且容易识别。
通过ARCore Android SDK中的arcoreimg工具，您可以验证目标图像的质量。我们可以运行此命令行工具来确定ARCore识别图像的程度。此工具输出0到100之间的数字，其中100是最容易识别的数字。这是一个例子：

arcore-android-sdk-1.9.0 / tools / arcoreimg / macos $
$ ./arcoreimg eval-img --input_image_path = / Users / username / maze.jpg
100
8.使球在迷宫中旋转
最后一部分与ARCore或Sceneform并没有真正的联系，但它的附加部分使此示例应用程序变得有趣。如果您跳过这一部分，那完全没问题。
我们将使用开源物理引擎jBullet来处理物理模拟。
这是我们要做的：
1.将GreenMaze.obj添加到project assets，以便我们可以在运行时加载它。
创建了PhysicsController类来管理所有与物理相关的功能。在内部，它使用JBullet物理引擎。
识别图像后调用PhysicsController并更新Physics
使用真实世界的重力在迷宫中移动球。注意，我们必须稍微缩放球的大小，以便它可以穿过迷宫中的间隙。
下载PhysicsController.java代码并将其添加到此目录中的项目中./sceneform-android-sdk-1.9.0/samples/augmentedimage/app/src/main/java/com/google/ar/sceneform/samples/augmentedimage/
然后在现有的Java代码中进行这些更改。如下，
在Android Studio中，GreenMaze.obj从
app > sampledata > green-maze
至：
app > assets
您的项目目录应如下所示。

在中app/build.gradle，添加此代码。
    // Add these dependencies.
    implementation 'cz.advel.jbullet:jbullet:20101010-1'

    // Obj - a simple Wavefront OBJ file loader
    // https://github.com/javagl/Obj
    implementation 'de.javagl:obj:0.2.1'
在中AugmentedImageNode.java，添加此代码。
// Add these lines at the top with the rest of the imports.import com.google.ar.core.Pose;import com.google.ar.sceneform.math.Quaternion;

  // Change ballNode to a member variable.
  private Node ballNode;

  // At the end of the setImage function, update some code art.
  public void setImage(AugmentedImage image) {

    ...
    
    // Add the ball, but this time ballNode is a member variable
    ballNode = new Node();
    ballNode.setParent(this);
    ballNode.setRenderable(ballRenderable);
    ballNode.setLocalPosition(new Vector3(0, 0.1f, 0)); // start position for debugging


    // add below code at the end of this function
    // Add the expected maze mesh size of ball. In, which is 13 diagram, 6.5 radius (in original mesh
    // vertices) is.
    // when mesh is scaled down to max_image_edge, radius will be scaled down to 6.5 * scale.
    // The sphere is already 0.01, so we need to scale the ball ball_scale = 6.5 * scale / 0.01f
    ballNode.setLocalScale(new Vector3(
        6.5f * maze_scale / 0.01f,
        6.5f * maze_scale / 0.01f,
        6.5f * maze_scale / 0.01f));
  }

  public void updateBallPose(Pose pose) {
    if (ballNode == null)
      return;

    ballNode.setLocalPosition(new Vector3(pose.tx() * maze_scale, pose.ty()* maze_scale, pose.tz()* maze_scale));
    ballNode.setLocalRotation(new Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw()));
  }

在中AugmentedImageActivity.java，添加此代码。

// Add these lines at the top with the rest of the imports.import com.google.ar.sceneform.math.Vector3;import com.google.ar.core.Pose;

  // Declare the PhysicsController class. 
  private PhysicsController physicsController;

  // Add a TRACKING case to the onUpdateFrame method.
  private void onUpdateFrame(FrameTime frameTime) {
    
    ...

    for (AugmentedImage augmentedImage : updatedAugmentedImages) {
      switch (augmentedImage.getTrackingState()) {
        
        ...

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
            Pose worldGravityPose = Pose.makeTranslation(0, -10f, 0);
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

然后，我们可以像这样移动它。

提示：将目标图像上下颠倒时，更容易找到出口。
如果屏幕上除了监视器以外没有其他东西可以显示目标图像，这是使应用程序更有趣的一个窍门：使用相机方向进行引力。为此，我们只需要更改原始重力方向即可。
在中AugmentedImageActivity.java，添加此代码。

  // Make these changes in onUpdateFrame.
  private void onUpdateFrame(FrameTime frameTime) {
            
            ...

            // Replace this line to the code below
            // Pose worldGravityPose = Pose.makeTranslation(0, -10f, 0);

            // Fun experiment, use camera direction as gravity
            float cameraZDir[] = frame.getCamera().getPose().getZAxis();
            Vector3 cameraZVector = new Vector3(cameraZDir[0], cameraZDir[1], cameraZDir[2]);
            Vector3 cameraGravity = cameraZVector.negated().scaled(10);
            Pose worldGravityPose = Pose.makeTranslation(
                cameraGravity.x, cameraGravity.y, cameraGravity.z);
            // ...
  }
现在，我们受ARCore支持的设备的行为就像吹叶机一样，我们可以使用它将球吹出迷宫。
玩得开心！


