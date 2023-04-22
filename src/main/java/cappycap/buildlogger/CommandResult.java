package cappycap.buildlogger;

// Holds variables for inserted dataset entries to share with user.
public class CommandResult {
    public int insertedId;
    public int xDim;
    public int yDim;
    public int zDim;
    public int xDimOld;
    public int yDimOld;
    public int zDimOld;
    public String labels;

    public CommandResult(int insertedId, String labels, int xDim, int yDim, int zDim, int xDimOld, int yDimOld, int zDimOld) {
        this.insertedId = insertedId;
        this.xDim = xDim;
        this.yDim = yDim;
        this.zDim = zDim;
        this.xDimOld = xDimOld;
        this.yDimOld = yDimOld;
        this.zDimOld = zDimOld;
        this.labels = labels;
    }
}
