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
        int i, maxPages = (int) Math.pow(2, MMU.getPageAddressBits());
        setPageTable(new PageTableEntry[maxPages]);
        for(i = 0; i < maxPages; i++)
        {
            getPageTable()[i] = new PageTableEntry(this, i);
        }
        // your code goes here
        // SIZE = MMU.getPageAddressBits shift it and stuff lol
        // init an array of size SIZE
        // each page must be ninit with a suitable PageTableEntry Obj
        // use PageIDs
    }

    /**
       Frees up main memory occupied by the task.
       Then unreserves the freed pages, if necessary.

       @OSPProject Memory
    */
    public void do_deallocateMemory()
    {
        //TODO
        // your code goes here
        // unset the various flags (setPage() to null it ) of the Frame
        //for i in pgaetable
        //setDirty() to clean it
        //setReferenced() to unset it
        // unreserves each frame(use getReserved() of FrameTableEntry)
        //DONT SET FRAME ATTRIBUTE NULL DIRECTLY
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

    public void setPageTable(PageTableEntry[] pageTable)
    {
        pages = pageTable;
    }

    public PageTableEntry[] getPageTable()
    {
        return pages;
    }
}

/*
      Feel free to add local classes to improve the readability of your code
*/
