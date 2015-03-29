package java.com.chess1800.core.search;

/**
 * Created by Yonathan on 24/03/2015.
 */
public interface SearchParameters {

    //Timing information White Time, White Increment, Black Time and Black Increment.
    public void setWTime(int wTime);

    public void setWInc(int wInc);

    public void setBTime(int bTime);

    public void setBInc(int bInc);


    //How many moves till the next time control. UCI related.
    public void setMovesToGo(int movesToGo);

    //Max Depth to search
    public void setDepth(int depth);

    //Max nodes to search.
    public void setNodes(int nodes);

    public void setMoveTime(int moveTime);

    //No cap on the Search, unless internal to the engine.
    public void setInfinite(boolean infinite);

    //Set all timing related info to default and set the new MoveTime.
    public void resetMoveTime(int moveTime);
}
