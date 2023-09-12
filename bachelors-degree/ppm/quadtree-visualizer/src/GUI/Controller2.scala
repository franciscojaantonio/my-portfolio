package GUI

import ImageUtil.writeImage
import javafx.fxml.{FXML, FXMLLoader}
import javafx.geometry.{Insets, Pos}
import javafx.scene.control.{Button, TextField}
import javafx.scene.image._
import javafx.scene.layout.{BorderPane, GridPane, TilePane, VBox}
import javafx.scene.{Parent, Scene}
import javafx.stage.{Modality, Stage}
import javafx.event.ActionEvent
import QTree.QTreeUtils._
import java.io.File


class Controller2 {
  private var defaultWidth: Int = 720
  private var defaultHeight: Int = 480
  @FXML
  private var scene2Button: Button = _
  @FXML
  private var tilePane1: TilePane = _
  @FXML
  private var rootBP: BorderPane = _
  @FXML
  private var gridPane1: GridPane = _
  @FXML
  private var root: Parent = _
  @FXML
  private var window: Stage = _
  private var myAlbum: BitMapAlbum = Controller.MyAlbum

  def initialize(): Unit = {
    onStart()
    loadGrid()
  }

  def onStart(): Unit = {
    tilePane1.setPadding(new Insets(20, 20, 20, 20))
    tilePane1.setHgap(20)
    tilePane1.setVgap(20)
  }

  def changeToScene1(): Unit = {
    Controller.alreadyOpened = true
    Controller.MyAlbum = myAlbum
    root = FXMLLoader.load(getClass.getResource("controller.fxml"))
    window = scene2Button.getScene.getWindow.asInstanceOf[Stage]
    window.setScene(new Scene(root, defaultWidth, defaultHeight))
  }

  def clearGrid(): Unit = {
    tilePane1.getChildren.clear()
  }

  def changeOrder(): Unit = {
    if(myAlbum.data.size <= 1)
      return

    val result = getInputTextField("Possible numbers: 1 To " + myAlbum.data.size, "Change order")
    result match {
      case Some(value) => myAlbum = myAlbum.changeOrder(value._1 - 1, value._2 - 1)
      case None => println("Invalid Input"); return
    }
    //myAlbum.showContent()
    loadGrid()
  }

  def loadGrid(): Unit = {
    clearGrid()
    for(i <- 0 until myAlbum.data.size) {
      val path = "albumFiles/temp.png"
      writeImage(listOfListToArrayOfArray(myAlbum.data(i)._1.points), "albumFiles/temp.png", "png")
      val img = new ImageView("file:" + path)
      img.fitWidthProperty().bind(gridPane1.widthProperty().divide(5))
      img.fitHeightProperty().bind(gridPane1.heightProperty().divide(3))
      img.setPreserveRatio(true)
      tilePane1.getChildren.add(img)
      val file = new File("albumFiles/temp.png")
      if(file.exists()) file.delete()
    }
  }

  def toInt(s1: String, s2: String): Option[(Int, Int)] = {
    try {
      Some(Integer.parseInt(s1.trim), Integer.parseInt(s2.trim))
    } catch {
      case e: Exception => None
    }
  }

  def getInputTextField(text: String, text2: String): Option[(Int, Int)] = {
    val inputFrom = new TextField()
    val inputTo = new TextField()
    var from = new String
    var to = new String
    val button = new Button("OK")
    val secondStage = new Stage()
    button.setOnAction((_: ActionEvent) => {
      from = inputFrom.getText
      to = inputTo.getText
      secondStage.close()
    })
    val vbox = new VBox()
    vbox.setAlignment(Pos.CENTER)
    inputFrom.setText(text)
    inputTo.setText(text)
    vbox.getChildren.addAll(inputFrom, inputTo, button)
    val secondScene = new Scene(vbox, 150, 80)
    secondStage.setScene(secondScene)
    secondStage.initModality(Modality.WINDOW_MODAL)
    secondStage.initOwner(rootBP.getScene.getWindow)
    secondStage.setResizable(false)
    secondStage.setTitle(text2)
    secondStage.showAndWait()
    toInt(from, to)
  }
}
