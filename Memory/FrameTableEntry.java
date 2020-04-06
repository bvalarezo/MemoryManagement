/**
 * Name: Bryan Valarezo
 * StudentID: 110362410
 * 
 * I pledge my honor that all parts of this project were done by me individually, without 
 * collaboration with anyone, and without consulting any external sources that provide 
 * full or partial solutions to a similar project. 
 * I understand that breaking this pledge will result in an “F” for the entire course.
 */

package osp.Memory;
import osp.Tasks.*;
import osp.Interrupts.*;
import osp.Utilities.*;
import osp.IFLModules.IflFrameTableEntry;

/**
    The FrameTableEntry class contains information about a specific page
    frame of memory.

    @OSPProject Memory
*/
public class FrameTableEntry extends IflFrameTableEntry
{
    private int useCount;
    /**
       The frame constructor. Must have

       	   super(frameID)
	   
       as its first statement.

       @OSPProject Memory
    */
    public FrameTableEntry(int frameID)
    {
        super(frameID);
        useCount = 0;
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */
    public void incrementUseCount(){
        if(this.useCount < 2)
            this.useCount++;
    }

    public void decrementUseCount(){
        if(this.useCount > 0)
            this.useCount--;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setUseCount(int useCount) {
        this.useCount = useCount;
    }

}

/*
      Feel free to add local classes to improve the readability of your code
*/
