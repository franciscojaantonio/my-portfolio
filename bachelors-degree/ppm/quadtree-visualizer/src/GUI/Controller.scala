package GUI

import QTree._
import BitMapAlbum._
import javafx.application.Platform
import javafx.event.{ActionEvent, Event}
import javafx.fxml.{FXML, FXMLLoader}
import javafx.geometry.Pos
import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.image.{Image, ImageView}
import javafx.scene.layout.{AnchorPane, GridPane, VBox}
import javafx.scene.{Parent, Scene}
import javafx.stage.{FileChooser, Modality, Stage}
import QTree.QuadTree._
import QTree.QTreeUtils._
import GUI.ImageUtil._
import QTree.BitMap._
import QTree.Random.MyRandom
import java.io._
import scala.collection.immutable.SortedMap

class Controller {
  @FXML
  private var root: Parent = _
  @FXML
  private var search: TextField = _
  @FXML
  private var rootAP: AnchorPane = _
  @FXML
  private var gridpane1: GridPane = _
  @FXML
  private var window: Stage = _
  @FXML
  private var menuBar: MenuBar = _
  @FXML
  private var scene1Button: Button = _
  @FXML
  private var imageView1: ImageView = _
  @FXML
  private var searchImage: ImageView = _
  @FXML
  private var gridImage: ImageView = _
  @FXML
  private var resetImage: ImageView = _
  @FXML
  private var removeImage: ImageView = _
  @FXML
  private var effectsImage: ImageView = _
  @FXML
  private var renameImage: ImageView = _
  @FXML
  private var fileImage: ImageView = _
  @FXML
  private var renameAlbumImage: ImageView = _
  @FXML
  private var resetAlbumImage: ImageView = _
  @FXML
  private var previousImage: ImageView = _
  @FXML
  private var nextImage: ImageView = _
  @FXML
  private var indexInfo: Label = _
  @FXML
  private var notFound: Label = _
  @FXML
  private var imageInfo: Label = _

  private var index = 0
  private var indexFilter = 0
  private val defaultWidth: Int = 600
  private val defaultHeight: Int = 400
  private var filter = false
  private var tempDirectory: String = ""
  private var filteredAlbum: BitMapAlbum = _
  private var auxAlbum: BitMapAlbum = _
  private var myAlbum: BitMapAlbum = GUI.BitMapAlbum("My Album", SortedMap[Int, (BitMap[Int], Info)]() )

  def initialize(): Unit = {
    onStart()
    if(!Controller.alreadyOpened) {
      read()
    }
    else
      myAlbum = Controller.MyAlbum

    auxAlbum = myAlbum
    index = 1
    updateImages()
  }

  def onChangeSearch(): Unit = {
    val text = search.getText
    if (text.nonEmpty) {
      if (!filter) filter = true
      indexFilter = 1
      filteredAlbum = new BitMapAlbum(myAlbum.nome, SortedMap[Int, (BitMap[Int], Info)]())
      val results = myAlbum.data.filter(x => x._2._2._2.toLowerCase.contains(text.toLowerCase))
      for (result <- results) {
        filteredAlbum = filteredAlbum.addFromFilesNewKey(result._2._1, result._2._2)
      }
      if (filteredAlbum.data.isEmpty) {
        filter = false
        notFound.setVisible(true)
        index = 1
      }
      else notFound.setVisible(false)
    }
    else {
      filter = false
      notFound.setVisible(false)
      indexFilter = 1
    }
    updateImages()
  }

  def resetAlbumOnClicked(): Unit = {
    myAlbum = auxAlbum
    updateImages()
  }

  def renameAlbumOnClicked(): Unit = {
    val newName = {
      val aux = getInputTextField(myAlbum.nome, "Rename Album")
      if (aux.isEmpty || aux.length < 1 || aux.length > 25) {
        println("Invalid Name!")
        myAlbum.nome
      }
      else
        aux
    }
    myAlbum = new BitMapAlbum(newName, myAlbum.data)
  }

  def imageInMainAlbum(): Unit = {
    val selectedInFilter = filteredAlbum.data(indexFilter - 1)
    def aux(): Int = {
      for (file <- myAlbum.data) {
        if (file._2._2._1.contains(selectedInFilter._2._1))
          return file._2._2._4
      }
      0
    }
    val inMyAlbum = aux()
    inMyAlbum match {
      case x: Int => index = x + 1
      case _ => println("Index Invalid")
    }
  }

  def renameImageOnClicked(): Unit = {
    if (myAlbum.data.size <= 0)
      println("Impossible to rename. Image may have been removed!")
    else {
      val data = myAlbum.data(index - 1)
      val newName = {
        val aux = getInputTextField(data._2._2, "Rename Image")
        if (aux.isEmpty || aux.contains(" ") || aux.length < 1 || aux.length > 20) {
          println("Invalid Name!")
          data._2._2
        }
        else
          aux
      }
      myAlbum = myAlbum.update(data._2._4, (data._1, (data._2._1, newName, data._2._3, data._2._4)))
      //myAlbum.showContent()
      updateImages()
    }
  }

  def resetAlbum(): Unit = {
    myAlbum = auxAlbum
    updateImages()
  }

  def onStart(): Unit = {
    searchImage.setImage(new Image("file:GUI_Content/search.png"))
    gridImage.setImage(new Image("file:GUI_Content/grid.png"))
    resetImage.setImage(new Image("file:GUI_Content/reset.png"))
    removeImage.setImage(new Image("file:GUI_Content/remove.png"))
    effectsImage.setImage(new Image("file:GUI_Content/edit.png"))
    renameImage.setImage(new Image("file:GUI_Content/rename.png"))
    renameAlbumImage.setImage(new Image("file:GUI_Content/rename.png"))
    resetAlbumImage.setImage(new Image("file:GUI_Content/reset.png"))
    fileImage.setImage(new Image("file:GUI_Content/menu.png"))
    nextImage.setImage(new Image("file:GUI_Content/next.png"))
    previousImage.setImage(new Image("file:GUI_Content/previous.png"))
    imageView1.fitHeightProperty().bind(gridpane1.heightProperty())
    imageView1.fitWidthProperty().bind(gridpane1.widthProperty())
  }

  def changeToScene2(): Unit = {
    Controller.MyAlbum = myAlbum
    root = FXMLLoader.load(getClass.getResource("Controller2.fxml"))
    window = scene1Button.getScene.getWindow.asInstanceOf[Stage]
    window.setScene(new Scene(root, defaultWidth, defaultHeight))
  }

  def onResetClicked(): Unit = {
    if (myAlbum.data.size <= 0)
      println("Impossible to Reset. Images may have been removed!")
    else {
      val data = auxAlbum.data(index - 1)
      auxAlbum.writeImg(index - 1)
      myAlbum = myAlbum.update(data._2._4, data)
      updateImages()
    }
  }

  def saveFiles(): Unit = {
    save()
  }

  def menuClose(event: Event): Unit = {
    myAlbum.showContent()
    save()
    Platform.exit()
  }

  def newImage(event: Event): Unit = {
    val stage = menuBar.getScene.getWindow
    try {
      val fileChooser = new FileChooser
      fileChooser.setTitle("Select New Image")
      val filePath = fileChooser.showOpenDialog(stage)
      tempDirectory = filePath.toURI.toString
      val direc = filePath.getPath
      val name = filePath.getName
      myAlbum = myAlbum.add(direc, name.slice(0, name.length - 4))
      auxAlbum = auxAlbum.add(direc, name.slice(0, name.length - 4))
      index = myAlbum.data.size
      //myAlbum.showContent()
      updateImages()
    }
    catch {
      case e: NullPointerException => println("No image selected")
    }
  }

  def updateImages(): Unit = {
    var aux = myAlbum
    var i = index
    if (filter) {
      imageInMainAlbum()
      i = indexFilter
      aux = filteredAlbum
    }
    if (aux.data.nonEmpty) {
      try {
        myAlbum.writeImg(index - 1)
        imageView1.setImage(new Image("file:albumFiles/temp.png"))
        indexInfo.setText("Image " + i + " of " + aux.data.size)
        imageInfo.setText("Name:\n" + myAlbum.data(index - 1)._2._2 + "\nSize:\n" + (myAlbum.data(index - 1)._2._3._2, myAlbum.data(index - 1)._2._3._1))
        val file = new File("albumFiles/temp.png")
        if (file.exists()) file.delete()
      }
      catch {
        case e: IOException => println("Error loading image (" + e + ")")
        case e: IndexOutOfBoundsException => println("Index Error (" + e + ")")
      }
    }
    else {
      imageView1.setImage(null)
      indexInfo.setText("Album is Empty!")
      imageInfo.setText("")
    }
  }

  /* Noise impuro
  def onNoiseClick(): Unit = {
    applyEffect(mapColourEffect(Noise, _))
  }
  */

  def onNoiseClick(): Unit = {
    applyEffect(mapColourEffect_1(Noise_1, MyRandom(1), _))
  }

  def onSepiaClick(): Unit = {
    applyEffect(mapColourEffect(Sepia, _))
  }

  def onContrastClick(): Unit = {
    applyEffect(mapColourEffect(Contrast, _))
  }

  def onMirrorHClick(): Unit = {
    applyEffect(mirrorH)
  }

  def onMirrorVClick(): Unit = {
    applyEffect(mirrorV)
  }

  def applyEffect(f:QTree[Coords] => QTree[Coords]): Unit = {
    if (myAlbum.data.isEmpty)
      println("Nothing to transform!")
    else {
      val qt = makeQTree(myAlbum.data(index - 1)._1)
      val aux = f(qt)
      val backToBitMap = makeBitmap(QuadTree(aux))
      val data = (backToBitMap, myAlbum.data(index - 1)._2._1, myAlbum.data(index - 1)._2._2, backToBitMap.getLength, myAlbum.data(index - 1)._2._4)
      writeImage(listOfListToArrayOfArray(backToBitMap.points), "albumFiles/temp.png", "png")
      myAlbum = myAlbum.update(data._5, (data._1, (data._2, data._3, data._4, data._5)))
      updateImages()
    }
  }

  def onScaleClick(): Unit = {
    val input = toDouble(getInputTextField("Enter factor value", "Scale Factor"))
    val factor = {
      input match {
        case Some(value) => value
        case None => println("Invalid Input"); return
      }
    }
    applyEffect(scale(factor, _))
  }

  def onRotateLClick(): Unit = {
    applyEffect(rotateL)
  }

  def onRotateRClick(): Unit = {
    applyEffect(rotateR)
  }

  def onNextClick(): Unit = {
    var alb = myAlbum
    if (filter) {
      alb = filteredAlbum
      if (indexFilter < alb.data.size)
        indexFilter += 1
      else
        indexFilter = 1
      updateImages()
    }
    else {
      if (index < alb.data.size)
        index += 1
      else
        index = 1
      updateImages()
    }
  }

  def onPreviousClick(): Unit = {
    var alb = myAlbum
    if (filter) {
      alb = filteredAlbum
      if (indexFilter > 1)
        indexFilter -= 1
      else
        indexFilter = alb.data.size
      updateImages()
    }
    else {
      if (index > 1)
        index -= 1
      else
        index = alb.data.size
      updateImages()
    }
  }

  def onRemoveClick(): Unit = {
    if (myAlbum.data.isEmpty)
      println("Nothing to Remove!")
    else if (displayModalPopup("Are you sure?", "Yes", "Cancel")) {
      myAlbum = myAlbum.remove(index - 1)
      auxAlbum = myAlbum
      if (filter) onChangeSearch()
      else onPreviousClick()
    }
  }

  def read(): Unit = {
    val albumFiles = new File("albumFiles")
    for (file <- albumFiles.listFiles) {
      val in = new ObjectInputStream(new FileInputStream(file.getPath))
      val readResult = in.readAllBytes()
      val objectToAdd = Serialization.deserialise(readResult).asInstanceOf[(BitMap[Int], Info)]
      myAlbum = myAlbum.addFromFiles(objectToAdd._1, objectToAdd._2)
      in.close()
    }
  }

  def toDouble(s: String): Option[Double] = {
    try {
      Some(s.toDouble)
    } catch {
      case e: Exception => None
    }
  }

  def reloadFiles(): Unit = {
    index = 1
    myAlbum = new BitMapAlbum("Meu Albúm", SortedMap[Int, (BitMap[Int], Info)]())
    read()
    auxAlbum = myAlbum
    updateImages()
  }

  def save(): Unit = {
    val albumFiles = new File("albumFiles")
    for(file <- albumFiles.listFiles())
      file.delete()
    for (i <- 0 until myAlbum.data.size) {
      val path = "albumFiles/" + myAlbum.data(i)._2._2
      val out = new ObjectOutputStream(new FileOutputStream(path))
      out.write(Serialization.serialise(myAlbum.data(i)._1, myAlbum.data(i)._2))
      out.reset()
      out.close()
    }
  }

  def displayModalPopup(message: String, yesmessage: String, nomessage: String): Boolean = {
    val alert = new Alert(AlertType.CONFIRMATION)
    alert.setTitle("Image Remove Warning")
    alert.setContentText("This image cannot be recovered.\nAre you sure you want to remove it?")
    val result = alert.showAndWait
    if (result.get eq ButtonType.OK)
      true
    else
      false
  }

  def getInputTextField(text: String, text2: String): String = {
    val name = new TextField()
    var output = new String
    val button = new Button("OK")
    val secondStage = new Stage()
    button.setOnAction((_: ActionEvent) => {
      output = name.getText
      secondStage.close()
    })
    val vbox = new VBox()
    vbox.setAlignment(Pos.CENTER)
    name.setText(text)
    vbox.getChildren.addAll(name, button)
    val secondScene = new Scene(vbox, 150, 80)
    secondStage.setScene(secondScene)
    secondStage.initModality(Modality.WINDOW_MODAL)
    secondStage.initOwner(rootAP.getScene.getWindow)
    secondStage.setResizable(false)
    secondStage.setTitle(text2)
    secondStage.showAndWait()
    output
  }
}

object Controller {
  var alreadyOpened: Boolean = false
  var MyAlbum: BitMapAlbum =_
}
