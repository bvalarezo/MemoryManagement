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
        int retval, pagefault_retval;
        /* Check Page Validity */
        if(isValid())
            /* Page valid */
            retval = SUCCESS;
        else
        {
            /* Page invalid */
            
            /* Check for a Page Fault */
            if(this.getValidatingThread())
            {
                /* Page fault exists */

                /* This thread was the to initate an already existing Page Fault */
                if(this.getValidatingThread() == iorb.getThread())
                    retval = SUCCESS;
                else
                {
                    /* A different thread initiated a Pagefault */
                    
                    /* Suspend this and wait */
                    iorb.getThread().suspend(this);

                    /* Page fault was Successful */
                    if(isValid())
                    {
                        /* Double check the thread status, make sure no SIGKILL */
                        if(iorb.getThread().getStatus() != ThreadCB.ThreadKill)
                            retval = SUCCESS;
                        else
                            /* Thread was killed, fail */
                            retval = FAILURE;
                    }
                    else
                        /* Page Fault failed */
                        retval = FAILURE;
                }
            }
            else
            {
                /* Page fault does not exist */
                /* Directly handle pagefault (already in kernel mode) */
                pagefault_retval = PageFaultHandler.handlePageFault(iorb.getThread(), MemoryLock, this);
                if (pagefault_retval == SUCCESS && isValid())
                {  
                    /* Double check the thread status, make sure no SIGKILL */
                    if(iorb.getThread().getStatus() != ThreadCB.ThreadKill)
                        retval = SUCCESS;
                    else
                        /* Thread was killed, fail */
                        retval = FAILURE;                
                }
                else
                    retval = FAILURE;
            }
        }
        /* a successful lock */
        if(retval == SUCCESS)
        {
            this.getFrame().incrementLockCount();
            // this.getFrame().incrementUseCount();
        }
        return retval;
    }

    /** This method decreases the lock count on the page by one. 

	This method must decrement lockCount, but not below zero.

	@OSPProject Memory
    */
    public void do_unlock()
    {
        if(this.getFrame().getLockCount() > 0)
            this.getFrame().decrementLockCount();
    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
