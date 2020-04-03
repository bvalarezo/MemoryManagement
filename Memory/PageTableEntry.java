package osp.Memory;

import osp.Hardware.*;
import osp.Tasks.*;
import osp.Threads.*;
import osp.Devices.*;
import osp.Utilities.*;
import osp.IFLModules.*;

/**
 * Name: Bryan Valarezo
 * StudentID: 110362410
 * 
 * I pledge my honor that all parts of this project were done by me individually, without 
 * collaboration with anyone, and without consulting any external sources that provide 
 * full or partial solutions to a similar project. 
 * I understand that breaking this pledge will result in an “F” for the entire course.
 */

/**
   The PageTableEntry object contains information about a specific virtual
   page in memory, including the page frame in which it resides.
   
   @OSPProject Memory

*/
public class PageTableEntry extends IflPageTableEntry
{
    /**
       The constructor. Must call

       	   super(ownerPageTable,pageNumber);
	   
       as its first statement.

       @OSPProject Memory
    */
    public PageTableEntry(PageTable ownerPageTable, int pageNumber)
    {
        // your code goes here
        super(ownerPageTable, pageNumber);
        // other inits?
    }

    /**
       This method increases the lock count on the page by one. 

	The method must FIRST increment lockCount, THEN  
	check if the page is valid, and if it is not and no 
	page validation event is present for the page, start page fault 
	by calling PageFaultHandler.handlePageFault().

	@return SUCCESS or FAILURE
	FAILURE happens when the pagefault due to locking fails or the thread
	that created the IORB thread gets killed.

	@OSPProject Memory
     */
    public int do_lock(IORB iorb)
    {
        // your code goes here
        //increment the lock count of the frame <= associated by this page
        //page might be invalid
        //method must first checl if the page is in memory(test validity bit)
        //if invalid, => PAGE FAULT (static method handlePageFault) does it directly, no INT needed(already in kernel mode)
        //consider these edge cases
        // thread Th1 of taskt T makes a reference to page P either via refer() or locking
        //if page is invalid, PAGEFAULT
        //Suppose th2 of T wants to lock the same page P.
        //Pagefault again?...no we can do better(see page 96)
        //we can get the thread who caused the pagefault getValidatingThread()
        //if Th2 == Th1, then return after inc the lockcount
        //else, then wait until P becomes valid by suspend() --> th2 and pass page P
        //when it does become valid(or fault fails), 
        //be sure to increment lock count on frame as well

    }

    /** This method decreases the lock count on the page by one. 

	This method must decrement lockCount, but not below zero.

	@OSPProject Memory
    */
    public void do_unlock()
    {
        // your code goes here
        // decrement the lock couint decrementLockCount()
        //make sure its not <0 WHICH WOULD BE AN ISSUE

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
