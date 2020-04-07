package osp.Memory;

import java.util.*;
import osp.IFLModules.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.Utilities.*;
import osp.Hardware.*;
import osp.Interrupts.*;
import osp.FileSys.OpenFile;
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
        /* Init the Frame Table */
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
            if(P.getValidatingThread() != null)
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
                InterruptVector.setReferenceType(referenceType);
                InterruptVector.setThread(thread);

                /* Call interrupt */
                CPU.interrupt(PageFault);

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

    // /** 
    //  Return a free frame (frame not in use)
      
    //  @return a free frame, null if no free frame avaliable 
    //  */
    // public synchronized static MyTuple getFreeFrame()
    // {
    //     FrameTableEntry freeFrame = null;
    //     int status = NotEnoughMemory;
    //     /* iterate entire frame table */
    //     for(int i = 0; i < getFrameTableSize(); i++)
    //     {
    //         /* check if frame is not reserved or locked */
    //         if(!getFrame(i).isReserved() && getFrame(i).getLockCount() == 0)
    //         {
    //             status = SUCCESS;
    //             /* Check if frame is free */
    //             if(getFrame(i).getPage() == null)
    //             {
    //                 freeFrame = getFrame(i);
    //                 break;
    //             }
    //         }
    //     }
    //     MyTuple retval = new MyTuple(status, freeFrame);
    //     return retval;
    // }

    /**
     * In order to optimize things, this method will try to get a free frame
     * Else, it will invoke the chooserHand and pick an appropirate frame
     * 
     * @return A Frame, null if ENOMEM
     */
    public synchronized static FrameTableEntry chooser()
    {
        int targetUseCount = 0, status = NotEnoughMemory;
        boolean dirtySwitch = false;
        FrameTableEntry choosenFrame = null, freeFrame = null;
        while(choosenFrame == null)
        {
            for(int i = 0; i < getFrameTableSize(); i++)
            {
                /* check if frame is not reserved or locked */
                if(!getFrame(i).isReserved() && getFrame(i).getLockCount() == 0)
                {
                    status = SUCCESS; //at least some frame can be replaced
                    /* Check if frame is free */
                    if(getFrame(i).getPage() == null)
                    {
                        freeFrame = getFrame(i);
                        break;
                    }
                    else if(getFrame(i).getUseCount() == targetUseCount && getFrame(i).isDirty() == dirtySwitch)
                    {
                        choosenFrame = getFrame(i);
                        break;
                    }
                }  
            }
            /* After the first sweep, if all of the frames were reserved or locked */
            if(status == NotEnoughMemory)
            {
                return null;
            }
            else if(freeFrame != null)
            {
                return freeFrame;
            }
            else
            {
                if(choosenFrame != null)
                {
                    break;
                }
                else
                {
                    /* increase the tolerance for the M2HC */
                    if(!dirtySwitch)
                    {
                        dirtySwitch = true;
                    }
                    else
                    {
                        dirtySwitch = false;
                        targetUseCount = targetUseCount < 2 ? (targetUseCount + 1): (targetUseCount);
                    }
                }
            }
        }
        return choosenFrame;
    }

}

/*
      Feel free to add local classes to improve the readability of your code
*/
class CleanerDaemon implements DaemonInterface
{
    /**
     * The M2HC Cleaner hand
     */
    public void unleash(ThreadCB thread)
    {
        int retval;
        PageTableEntry oldPage = null;
        OpenFile swapFile = null;

         /* iterate entire frame table */
         for(int i = 0; i < MMU.getFrameTableSize(); i++)
         {
             /* check if frame is not reserved, locked, or already free */
             if(!MMU.getFrame(i).isReserved() && MMU.getFrame(i).getLockCount() == 0 && MMU.getFrame(i).getPage() != null)
             {
                 /* If the frame is dirty and has a use count of 0, swap out */
                 if(MMU.getFrame(i).getUseCount() == 0 && MMU.getFrame(i).isDirty())
                 {
                    /* Get the old page to swap */
                    oldPage = MMU.getFrame(i).getPage();
                    
                    /* Swap out operation */
                    swapFile = oldPage.getTask().getSwapFile();
                    swapFile.write(oldPage.getID(), oldPage, thread);

                    /* Free the frame */
                    MMU.getFrame(i).setPage(null);
                    MMU.getFrame(i).setDirty(false);
                    MMU.getFrame(i).setReferenced(false);
                    oldPage.setValid(false);
                    oldPage.setFrame(null);
                 }
                 else
                 {
                     /* Decrement use count */
                     MMU.getFrame(i).decrementUseCount();
                 }
             }
         }
    }
}