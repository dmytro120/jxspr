package xinth;

public class GameFile
{
    private byte[] fileData;

    public GameFile(byte[] fileData)
    {
        this.fileData = fileData;
    }

    public int byteAt(int index)
    {
        return this.fileData[index] & 0xff;
    }

    public int shortAt(int index)
    {
        return (int)(this.byteAt(index) + this.byteAt(index+1) * 256);
    }

    public int intAt(int index)
    {
        return (int)(this.byteAt(index) + this.byteAt(index+1) * 256 + this.byteAt(index+2) * Math.pow(256,2) + this.byteAt(index+3) * Math.pow(256,3));
    }

    public String stringAt(int index, int len)
    {
        StringBuilder output = new StringBuilder();
        for (int i = index; i < len; i++) {
            output.append(fileData[i]);
        }
        return output.toString();
    }
}