package xinth;

import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class MAP extends GameFile
{
    private int width;
    private int height;
    private BTS bts;

    MAP(byte[] mapData, BTS bts)
    {
        super(mapData);
        width = intAt(0);
        height = intAt(4);
        this.bts = bts;
    }

    Canvas asCanvas()
    {
        Canvas oCanvas = new Canvas(width * 32, height * 32);
        GraphicsContext oContext = oCanvas.getGraphicsContext2D();
        oContext.setFill(Color.BLACK);
        oContext.fillRect(0, 0, width * 32, height * 32);

        Map<Integer, Image[]> xBTSCache = new HashMap<>();
        int noTiles = width * height;
        int curX = 0;
        int curY = 0;
        for (int t = 0; t < noTiles; t++) {
            int mpos = 8 + t*4;
            int mainTile = shortAt(mpos);
            int topTile = shortAt(mpos + 2);

            int fpos = 8 + noTiles * 4 + t*2;
            int flagByte = byteAt(fpos);
            boolean doFlipMain = ((flagByte >> 5) & 1) == 1;
            boolean doFlipTop = ((flagByte >> 6) & 1) == 1;

            Image[] oMainTile = xBTSCache.get(mainTile);
            if (oMainTile != null) {
                Image image = oMainTile[!doFlipMain ? 0:1];
                oContext.drawImage(image, curX * 32, curY * 32);
            } else {
                try {
                    Canvas main = bts.frameAsCanvasByFID(mainTile, doFlipMain);
                    SnapshotParameters params = new SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    WritableImage image = main.snapshot(params, null);
                    oContext.drawImage(image, curX * 32, curY * 32);

                    Canvas main2 = bts.frameAsCanvasByFID(mainTile, !doFlipMain);
                    WritableImage image2 = main2.snapshot(params, null);

                    Image[] lozos = new Image[2];
                    lozos[doFlipMain ? 1 : 0] = image;
                    lozos[doFlipMain ? 0 : 1] = image2;

                    xBTSCache.put(mainTile, lozos);
                } catch (NullPointerException e) {
                    //System.out.println("Tile not found!");
                }
            }

            Image[] oTopTile = xBTSCache.get(topTile);
            if (oTopTile != null) {
                Image image = oTopTile[!doFlipTop ? 0:1];
                oContext.drawImage(image, curX * 32, curY * 32);
            } else {
                try {
                    Canvas top = bts.frameAsCanvasByFID(topTile, doFlipTop);
                    SnapshotParameters params = new SnapshotParameters();
                    params.setFill(Color.TRANSPARENT);
                    WritableImage image = top.snapshot(params, null);
                    oContext.drawImage(image, curX * 32, curY * 32);

                    Canvas top2 = bts.frameAsCanvasByFID(topTile, !doFlipTop);
                    WritableImage image2 = top2.snapshot(params, null);

                    Image[] lozos = new Image[2];
                    lozos[doFlipTop ? 1 : 0] = image;
                    lozos[doFlipTop ? 0 : 1] = image2;

                    xBTSCache.put(topTile, lozos);
                } catch (NullPointerException e) {
                    //System.out.println("Tile not found!");
                }
            }

            curX++;
            if (curX >= width) {
                curX = 0;
                curY++;
            }
        }

        return oCanvas;
    }
}