package osp.Memory;
/**
    The PageTable class represents the page table for a given task.
    A PageTable consists of an array of PageTableEntry objects.  This
    page table is of the non-inverted type.

    @OSPProject Memory
*/
import java.lang.Math;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.IFLModules.*;
import osp.Hardware.*;

/**
 * Name: Bryan Valarezo
 * StudentID: 110362410
 * 
 * I pledge my honor that all parts of this project were done by me individually, without 
 * collaboration with anyone, and without consulting any external sources that provide 
 * full or partial solutions to a similar project. 
 * I understand that breaking this pledge will result in an “F” for the entire course.
 */

public class PageTable extends IflPageTable
{
    /** 
	The page table constructor. Must call
	
	    super(ownerTask)

	as its first statement.

	@OSPProject Memory
    */
    public PageTable(TaskCB ownerTask)
    {
        super(ownerTask);
        MyOut.print(this,"Entering Student Method... constructor PageTable()");
        int i, maxPages = (int) Math.pow(2, MMU.getPageAddressBits());
        setPageTable(new PageTableEntry[maxPages]);
        for(i = 0; i < maxPages; i++)
        {
            getPageTable()[i] = new PageTableEntry(this, i);
        }
    }

    /**
       Frees up main memory occupied by the task.
       Then unreserves the freed pages, if necessary.

       @OSPProject Memory
    */
    public void do_deallocateMemory()
    {
        FrameTableEntry currentFrame;
        for(int i =0; i < getPageTable().length; i++)
        {
            currentFrame = getPageTable()[i].getFrame();
            if(currentFrame != null)
            {
                /* Free the frame */
                currentFrame.setPage(null);
                currentFrame.setDirty(false);
                currentFrame.setReferenced(false);

                /* Set the page invalid */
                getPageTable()[i].setValid(false);

                /* If the current frame was reserved by this task, unreserve it */
                if(currentFrame.getReserved() == getTask())
                {
                    currentFrame.setUnreserved(getTask());
                }
            }
        }
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

    public void setPageTable(PageTableEntry[] pageTable)
    {
        this.pages = pageTable;
    }

    public PageTableEntry[] getPageTable()
    {
        return this.pages;
    }
}

/*
      Feel free to add local classes to improve the readability of your code
*/
