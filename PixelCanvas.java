package xinth;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class PixelCanvas
{
    private int curX = 0;
    private int curY = 0;
    private int imgW = 0;
    private int imgH = 0;
    private int disX = 0;
    private int disY = 0;
    private boolean doFlip = false;
    private Canvas canvas;
    private PixelWriter pw;

    public PixelCanvas(int w, int h, boolean doFlip)
    {
        imgW = w;
        imgH = h;

        canvas = new Canvas(w, h);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        pw = gc.getPixelWriter();

        this.doFlip = doFlip;
    }

    public void addPixel(int r, int g, int b)
    {
        if (r > 255) r = 255;
        if (g > 255) g = 255;
        if (b > 255) b = 255;
        Color pixelColor = Color.rgb(r, g, b);
        if (!doFlip) pw.setColor(curX, curY, pixelColor);
        else pw.setColor(imgW - 1 - curX + disX, curY, pixelColor);
        next();
    }

    public void next()
    {
        curX++;
        if (curX >= imgW) {
            curX = disX;
            curY++;
        }
    }

    Canvas asCanvas()
    {
        return canvas;
    }
}