package OSAssignment;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  Project by Marco, and Tomas
 */
public class ImageViewerWindowController {
    private final List<Image> images = new ArrayList<>();
    private final List<String> imageUris = new ArrayList<>();
    private final Object slideshowObject = new Object();
    private final Object rgbPixelObject = new Object();
    private int currentImageIndex = 0;


    @FXML
    private Slider slideshowSlider;
    @FXML
    private ToggleButton slideshowBtn;
    @FXML
    private Label rgbLbl, nameLbl;
    @FXML
    Parent root;
    @FXML
    private ImageView imageView;

    @FXML
    public void handleBtnLoadAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select image files");
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Images",
                "*.png", "*.jpg", "*.gif", "*.tif", "*.bmp"));
        List<File> files = fileChooser.showOpenMultipleDialog(new Stage());

        if (!files.isEmpty()) {
            files.forEach((File f) ->
            {
                images.add(new Image(f.toURI().toString()));
                imageUris.add(f.getName());
            });
            displayImage();

        }
    }


    @FXML
    private void handleBtnPreviousAction() {
        previousImage();
    }

    @FXML
    private void handleBtnNextAction() {
        nextImage();
    }


    @FXML
    private void handleBtnSlideshow() {
        if (!images.isEmpty()) {
            Thread slideshow = new Thread(() -> {
                synchronized (slideshowObject) {
                    while (slideshowBtn.isSelected()) {
                        Platform.runLater(this::nextImage);
                        try {
                            TimeUnit.MILLISECONDS.sleep((int) slideshowSlider.getValue());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            slideshow.setDaemon(true);

            if (!slideshowBtn.isSelected()) {
                try {
                    slideshow.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                if (!slideshow.isAlive()) slideshow.start();
            }
        } else {
            System.out.println("No images loaded!");
        }
    }

    private void rgbPixelImageCounter(Image image) {
        final PixelReader rgbReader = image.getPixelReader();
        AtomicInteger redPixelAtomic = new AtomicInteger();
        AtomicInteger greenPixelAtomic = new AtomicInteger();
        AtomicInteger bluePixelAtomic = new AtomicInteger();
        AtomicInteger mixedPixelAtomic = new AtomicInteger();

        Thread rgbPixelCounterThread = new Thread(() -> {
            synchronized (rgbPixelObject) {
                for (int y = 0; y < image.getHeight(); y++) {

                    for (int x = 0; x < image.getWidth(); x++) {

                        Color colorOfPixel = rgbReader.getColor(x, y);

                        double redPixel = colorOfPixel.getRed();
                        double greenPixel = colorOfPixel.getGreen();
                        double bluePixel = colorOfPixel.getBlue();

                        if (redPixel > bluePixel && redPixel > greenPixel) {
                            redPixelAtomic.set(redPixelAtomic.get() + 1);
                        } else if (greenPixel > bluePixel && greenPixel > redPixel) {
                            greenPixelAtomic.set(greenPixelAtomic.get() + 1);
                        } else if (bluePixel > redPixel && bluePixel > greenPixel) {
                            bluePixelAtomic.set(bluePixelAtomic.get() + 1);
                        } else {
                            mixedPixelAtomic.set(mixedPixelAtomic.get() + 1);
                        }
                    }
                }
                Platform.runLater(() -> rgbLbl.setText("Pixels: R: " + redPixelAtomic + " G: " + greenPixelAtomic + " B: " + bluePixelAtomic + " Mixed: " + mixedPixelAtomic));
            }
        });
        rgbPixelCounterThread.setDaemon(true);
        rgbPixelCounterThread.start();

    }

    private void displayImage()
    {
        if (!images.isEmpty())
        {
            Image newImage = images.get(currentImageIndex);
            imageView.setImage(newImage);
            imageView.setImage(images.get(currentImageIndex));
            nameLbl.setText(imageUris.get(currentImageIndex));
            rgbPixelImageCounter(images.get(currentImageIndex));
        }
    }
    private synchronized void previousImage(){
        if (!images.isEmpty())
        {
            currentImageIndex =
                    (currentImageIndex - 1 + images.size()) % images.size();
            displayImage();
        }
    }

    private synchronized void nextImage(){
        if (!images.isEmpty())
        {
            currentImageIndex =
                    (currentImageIndex + 1) % images.size();
            displayImage();
        }
    }

}