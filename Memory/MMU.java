package osp.Memory;

import java.util.*;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.Hardware.*;
import osp.Interrupts.*;

/**
    The MMU class contains the student code that performs the work of
    handling a memory reference.  It is responsible for calling the
    interrupt handler if a page fault is required.

    @OSPProject Memory
*/
public class MMU extends IflMMU
{    
    /** 
        This method is called once before the simulation starts. 
	Can be used to initialize the frame table and other static variables.

        @OSPProject Memory
    */
    public static void init()
    {
        FrameTableEntry newEntry;
        for(int i = 0; i < getFrameTableSize(); i++)
        {
            newEntry = new FrameTableEntry(i);
            setFrame(i, newEntry);
        }
        Daemon.create("Cleaner Daemon", new CleanerDaemon(), 4000);
    }

    /**
       This method handles memory references. The method must 
       calculate, which memory page contains the memoryAddress,
       determine, whether the page is valid, start page fault 
       by making an interrupt if the page is invalid, finally, 
       if the page is still valid, i.e., not swapped out by another 
       thread while this thread was suspended, set its frame
       as referenced and then set it as dirty if necessary.
       (After pagefault, the thread will be placed on the ready queue, 
       and it is possible that some other thread will take away the frame.)
       
       @param memoryAddress A virtual memory address
       @param referenceType The type of memory reference to perform 
       @param thread that does the memory access
       (e.g., MemoryRead or MemoryWrite).
       @return The referenced page.

       @OSPProject Memory
    */
    static public PageTableEntry do_refer(int memoryAddress,
					  int referenceType, ThreadCB thread)
    {
        int offsetBits, pageSize, pageNumber;
        boolean refer = false;
        /* Calculate the page number based on the memoryAddress(VirtualAddress) */
        offsetBits = getVirtualAddressBits() - getPageAddressBits();
        pageSize = (int) Math.pow(2, offsetBits);
        pageNumber = memoryAddress/pageSize;

        /* Get the page from the page table */
        PageTableEntry P = thread.getTask().getPageTable().getPageTable()[pageNumber];

        /* Check page validity */
        if(P.isValid())
        {
            /* Page Valid */
            refer = true;
        }
        else
        {
            /* Page Invalid */

            /* Check for page fault */
            if(P.getValidatingThread())
            {
                /* Page fault exists */
                
                /* Suspend the thread */
                thread.suspend(P);

                /* Double check the thread status, make sure no SIGKILL */
                if(thread.getStatus() != ThreadCB.ThreadKill && P.isValid())
                    refer = true;
            }
            else
            {
                /* Page fault does not exist */

                /* Setup a page fault interrupt */
                InterruptVector.setPage(P);
                InterruptVector.setReferenceTpye(referenceType);
                InterruptVector.setThread(thread);

                /* Call interrupt */
                CPU.interrrupt(PageFault);

                /* Double check the thread status, make sure no SIGKILL */
                if(thread.getStatus() != ThreadCB.ThreadKill && P.isValid())
                    refer = true;
            }

        }
        /* Perform reference */
        if(refer)
        {
            P.getFrame().setReferenced(true);
            P.getFrame().incrementUseCount();
            if(referenceType == MemoryWrite)
                P.getFrame().setDirty(true);
        }
        return P;
        //If valid, then set the referenced the dirty bits and quit
        //else, then
        //1. Some other thread of the same task has already caused the pagefault and we are waiting
        //2. No other thread caused a page fault(FRESH)
        //use getValidatingThread()
        //if 1. Suspend() thread
        //make sure Thread is not ThreadKill with getStatus()
        //if 2. cause a PageFault
        //InterruptVector Class construct with setPage(), setReferenceTpye(), setThread()
        //call interrupt() of type PageFault
        //After interrrupt(), PAGE WILL BE IN Main Mem.
        //thread will be in readyQ
        //before exit, set the reference and dirty bits
        //Keep in mind of SIGKILL 
        //return the referenced page

    }

    /** Called by OSP after printing an error message. The student can
	insert code here to print various tables and data structures
	in their state just after the error happened.  The body can be
	left empty, if this feature is not used.
     
	@OSPProject Memory
     */
    public static void atError()
    {
        // your code goes here

    }

    /** Called by OSP after printing a warning message. The student
	can insert code here to print various tables and data
	structures in their state just after the warning happened.
	The body can be left empty, if this feature is not used.
     
      @OSPProject Memory
     */
    public static void atWarning()
    {
        // your code goes here

    }


    /*
       Feel free to add methods/fields to improve the readability of your code
    */

    /** 
     Return a free frame (frame not in use)
      
     @return a free frame, null if no free frame avaliable 
     */
    public synchronized static MyTuple<Integer, FrameTableEntry> getFreeFrame()
    {
        FrameTableEntry freeFrame = null;
        Integer status = NotEnoughMemory;
        /* iterate entire frame table */
        for(int i = 0; i < getFrameTableSize(); i++)
        {
            /* check if frame is not reserved or locked */
            if(!getFrame(i).isReserved() && getFrame(i).getLockCount() == 0)
            {
                status = SUCCESS;
                /* Check if frame is free */
                if(getFrame(i).getPage() == null)
                {
                    freeFrame = getFrame(i);
                    break;
                }
            }
        }
        MyTuple<Integer, FrameTableEntry> retval = new Tuple(status, freeFrame);
        return retval;
    }
    // public static boolean isOutOfMemory()
    // {
    //     boolean retval = true;
    //     /* iterate entire frame table */
    //     for(int i = 0; i < getFrameTableSize(); i++)
    //     {
    //         /* check if frame is not reserved or locked */
    //         if(!getFrame(i).isReserved() && getFrame(i).getLockCount() == 0)
    //         {
    //             /* there exists an eligible frame */
    //             retval = false;
    //             break;
    //         }
    //     }
    //     return retval;
    // }
}

/*
      Feel free to add local classes to improve the readability of your code
*/
class CleanerDaemon implements DaemonInterface
{
    public void unleash(ThreadCB thread)
    {
        //TODO
    }
}