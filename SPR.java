package xinth;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;

import java.util.ArrayList;
import java.util.Arrays;

public class SPR extends GameFile
{
    private byte team = 7;
    private boolean isCompressed;
    private int imageCount;
    private ArrayList<SPRFrame> frames = new ArrayList<>();
    private int[][] palette = new int[256][3];
    private int[][] fallbackPalette;

    public SPR(byte[] sprData)
    {
        super(sprData);

        isCompressed = byteAt(0) == 129;
        imageCount = shortAt(2);

        int dataStart = 776 + (imageCount * 8);
        int lenCumul = 0;

        int x1 = isCompressed ? intAt(4) + imageCount * 4 : intAt(4);
        int x2 = isCompressed ? sprData.length - dataStart : sprData.length - 776 + imageCount * 4;
        if (x1 != x2) System.out.println("File integrity check failed! " + x1 + " " + x2);

        for (int i = 0; i < 256; i++) {
            palette[i][0] = (byteAt(8+i*3+0)*4+3);
            palette[i][1] = (byteAt(8+i*3+1)*4+3);
            palette[i][2] = (byteAt(8+i*3+2)*4+3);
        }

        for (int f = 0; f < imageCount; f++) {
            int infoOffset = 776 + (f * 8);
            int width = shortAt(infoOffset);
            int height = shortAt(infoOffset+2);
            int disX = shortAt(infoOffset+4);
            int disY = shortAt(infoOffset+6);

            int start = dataStart + lenCumul;
            int len;

            if (isCompressed) {
                len = intAt(start);
                lenCumul += len + 4;
            } else {
                len = width * height;
                lenCumul += len;
            }

            SPRFrame frame = new SPRFrame(start, len);
            frame.setSize(width, height);
            frame.setDisplacement(disX, disY);
            frames.add(frame);
        }
    }

    int[] rgb(int id)
    {
        // red(0)		-7*6	Pan Luma
        // blue(1)		-6*6	Stratus
        // yellow(2)	-5*6	Taar
        // purple(3)	-4*6	Taar Council
        // green(4)		-3*6	Unknown
        // orange(5)	-2*6	Roswell Taar (Enemy Drones)
        // peach(6)		-1*6	Unknown
        // cyan(7)		-0*6	Aerogen

        int[] specialIDs = {138, 139, 140, 141, 142, 143};
        if (Arrays.binarySearch(specialIDs, id) >= 0) {
            id += (this.team - 7)*6;

            if ( // fallback for missing Stratus color in some sprites
                team != 7 &&
                palette[138][0] == 3 && palette[138][1] == 255 && palette[138][2] == 255 &&
                fallbackPalette != null
            )
            return fallbackPalette[id];
        }
        return palette[id];
    }

    int[][] getPalette()
    {
        return palette;
    }

    void setPalette(int[][] palette)
    {
        this.palette = palette;
    }

    void setFallbackPalette(int[][] palette)
    {
        this.fallbackPalette = palette;
    }

    void setTeam(byte team)
    {
        this.team = team;
    }

    int getFrameCount()
    {
        return imageCount;
    }

    SPRFrame getFrameInfo(int f)
    {
        return frames.get(f);
    }

    void setFrameInfo(int f, SPRFrame info)
    {
        frames.set(f, info);
    }

    Canvas frameAsImage(int f, boolean doFlip, boolean doDisplace)
    {
        SPRFrame frame = frames.get(f);
        PixelCanvas canvas = new PixelCanvas(frame.getWidth(), frame.getHeight(), doFlip);

        if (isCompressed) {
            int c = (frame.getStart()+4);
            while (c < (frame.getStart() + frame.getLength())) {
                int x = byteAt(c);
                if (x < 128) {
                    int noNextRawBytes = x + 1;
                    for (int r = 1; r <= noNextRawBytes; r++) {
                        x = byteAt(c+r);
                        int[] rgb = rgb(x);
                        canvas.addPixel(rgb[0], rgb[1], rgb[2]);
                    }
                    c += (noNextRawBytes+1);
                } else {
                    int noBlackPixels = 256 - x;
                    for (int b = 0; b < noBlackPixels; b++) {
                        canvas.addPixel(0,0,0);
                    }
                    c++;
                }
            }
        } else {
            for (int c = frame.getStart(); c < (frame.getStart() + frame.getLength()); c++) {
                int x = byteAt(c);
                int[] rgb = this.rgb(x);
                canvas.addPixel(rgb[0], rgb[1], rgb[2]);
            }
        }

        return canvas.asCanvas();
    }
}

class SPRFrame
{
    private int width;
    private int height;
    private int disX;
    private int disY;
    private int start;
    private int len;

    public SPRFrame(int start, int len)
    {
        this.start = start;
        this.len = len;
    }

    public void setSize(int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    public void setDisplacement(int disX, int disY)
    {
        this.disX = disX;
        this.disY = disY;
    }

    int getWidth()
    {
        return width;
    }

    int getHeight()
    {
        return height;
    }

    int getStart()
    {
        return start;
    }

    int getLength()
    {
        return len;
    }
}