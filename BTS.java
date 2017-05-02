package xinth;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.canvas.Canvas;

public class BTS extends GameFile
{
    private int noImages;
    private int[][] palette = new int[256][3];
    private int[] frameFIDs;
    private Map<Integer, Integer> framesByFID;

    BTS(byte[] btsData)
    {
        super(btsData);
        noImages = this.shortAt(4);

        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 3; j++) palette[i][j] = byteAt(8+i*3+j)*4+3;
        }

        frameFIDs = new int[noImages];
        framesByFID = new HashMap<>();
        for (int f = 0; f < noImages; f++) {
            int frameFID = intAt(776 + 1028 * f);
            frameFIDs[f] = frameFID;
            framesByFID.put(frameFID, f);
        }
    }

    int[] rgb(int id)
    {
        return palette[id];
    }

    int[][] getPalette()
    {
        return palette;
    }

    int getFrameCount()
    {
        return noImages;
    }

    int getFrameFID(int f)
    {
        return frameFIDs[f];
    }

    Canvas frameAsCanvas(int f, boolean doFlip)
    {
        int frameStart = 776 + 1028 * f;
        PixelCanvas canvas = new PixelCanvas(32, 32, doFlip);

        int c = frameStart + 4;
        while (c < (frameStart + 1028)) {
            int x = byteAt(c);
            int[] rgb = rgb(x);
            if (rgb[0] != 255 && rgb[1] != 3 && rgb[2] != 255) canvas.addPixel(rgb[0], rgb[1], rgb[2]);
            else canvas.next();
            c++;
        }

        return canvas.asCanvas();
    }

    Canvas frameAsCanvasByFID(int fid, boolean doFlip) throws NullPointerException
    {
        int f = framesByFID.get(fid);
        return frameAsCanvas(f, doFlip);
    }
}