package osp.Memory;
import java.util.*;
import osp.Hardware.*;
import osp.Threads.*;
import osp.Tasks.*;
import osp.FileSys.FileSys;
import osp.FileSys.OpenFile;
import osp.IFLModules.*;
import osp.Interrupts.*;
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
    The page fault handler is responsible for handling a page
    fault.  If a swap in or swap out operation is required, the page fault
    handler must request the operation.

    @OSPProject Memory
*/
public class PageFaultHandler extends IflPageFaultHandler
{
    /**
        This method handles a page fault. 

        It must check and return if the page is valid, 

        It must check if the page is already being brought in by some other
	thread, i.e., if the page's has already pagefaulted
	(for instance, using getValidatingThread()).
        If that is the case, the thread must be suspended on that page.
        
        If none of the above is true, a new frame must be chosen 
        and reserved until the swap in of the requested 
        page into this frame is complete. 

	Note that you have to make sure that the validating thread of
	a page is set correctly. To this end, you must set the page's
	validating thread using setValidatingThread() when a pagefault
	happens and you must set it back to null when the pagefault is over.

        If a swap-out is necessary (because the chosen frame is
        dirty), the victim page must be dissasociated 
        from the frame and marked invalid. After the swap-in, the 
        frame must be marked clean. The swap-ins and swap-outs 
        must are preformed using regular calls read() and write().

        The student implementation should define additional methods, e.g, 
        a method to search for an available frame.

	Note: multiple threads might be waiting for completion of the
	page fault. The thread that initiated the pagefault would be
	waiting on the IORBs that are tasked to bring the page in (and
	to free the frame during the swapout). However, while
	pagefault is in progress, other threads might request the same
	page. Those threads won't cause another pagefault, of course,
	but they would enqueue themselves on the page (a page is also
	an Event!), waiting for the completion of the original
	pagefault. It is thus important to call notifyThreads() on the
	page at the end -- regardless of whether the pagefault
	succeeded in bringing the page in or not.

        @param thread the thread that requested a page fault
        @param referenceType whether it is memory read or write
        @param page the memory page 

	@return SUCCESS is everything is fine; FAILURE if the thread
	dies while waiting for swap in or swap out or if the page is
	already in memory and no page fault was necessary (well, this
	shouldn't happen, but...). In addition, if there is no frame
	that can be allocated to satisfy the page fault, then it
	should return NotEnoughMemory

        @OSPProject Memory
    */
    public static int do_handlePageFault(ThreadCB thread, 
					 int referenceType,
					 PageTableEntry page)
    {
        int retval;
        SystemEvent pfEvent = new SystemEvent("pfEvent");
        FrameTableEntry selectedFrame = null;
        PageTableEntry oldPage = null;
        OpenFile swapFile = null;
        page.setValidatingThread(thread);
        /* Check if the page is valid */
        if(page.isValid())
        {
            /* Page is valid */
            retval = FAILURE;
        }
        else
        {
            /* Page invalid */
            
            /* Get an eligible frame */
            MyTuple t = MMU.getFreeFrame();
            
            /* Check if memory is avaliable */
            if(t.getStatus() == NotEnoughMemory)
                /* ENOMEM */
                retval = NotEnoughMemory;
            else
            {
                /* Suspend thread with page fault event */
                thread.suspend(pfEvent);

                /* Get the free frame */
                selectedFrame = t.getFreeFrame();

                /* Check if free frame was found */
                if(selectedFrame != null)
                {
                    /* Free frame avaliable */

                    /* Reserve the frame, preventing it from being taken away */
                    selectedFrame.setReserved(thread.getTask());
                    
                    /* assign frame to page */
                    page.setFrame(selectedFrame);

                    /* swap in operation */
                    swapFile = thread.getTask().getSwapFile();
                    swapFile.read(page.getID(), page, thread);
                }
                else
                {
                    /* No free frame avaliable */

                    /* Call chooser to get a frame to free*/
                    selectedFrame = MMU.chooser();
                    /* Reserve the frame, preventing it from being taken away */
                    selectedFrame.setReserved(thread.getTask());

                    /* Get the old page to swap */
                    oldPage = selectedFrame.getPage();

                    /* Check the dirty bit */
                    if(selectedFrame.isDirty())
                    {
                        /* Frame is dirty */

                        /* swap out operation */
                        swapFile = oldPage.getTask().getSwapFile();
                        swapFile.write(oldPage.getID(), oldPage, thread);
                        
                        /* Perform SIGKILL check */
                        if(thread.getStatus() == ThreadCB.ThreadKill)
                        {
                            retval = FAILURE;
                        }
                        else
                        {
                            /* Free the frame */
                            selectedFrame.setPage(null);
                            selectedFrame.setDirty(false);
                            selectedFrame.setReferenced(false);
                            oldPage.setValid(false);
                            oldPage.setFrame(null);

                            /* assign frame to page */
                            page.setFrame(selectedFrame);

                            /* swap in operation */
                            swapFile = thread.getTask().getSwapFile();
                            swapFile.read(page.getID(), page, thread);
                        }
                    }
                    else
                    {
                        /* Frame is clean */

                        /* Free the frame */
                        selectedFrame.setPage(null);
                        selectedFrame.setDirty(false);
                        selectedFrame.setReferenced(false);
                        oldPage.setValid(false);
                        oldPage.setFrame(null);

                        /* assign frame to page */
                        page.setFrame(selectedFrame);

                        /* swap in operation */
                        swapFile = thread.getTask().getSwapFile();
                        swapFile.read(page.getID(), page, thread);
                    }
                }
                /* Double check the thread status, make sure no SIGKILL */
                if(thread.getStatus() != ThreadCB.ThreadKill)
                    retval = SUCCESS;
                else
                {
                    /* Thread was killed, unset frame to page */
                    page.setFrame(null);
                    retval = FAILURE;
                }
            }
        }

        /* Perform Operations */
        if(retval == SUCCESS)
        {
            /* assign new page to frame */
            selectedFrame.setPage(page);
            page.setValid(true);
            if(referenceType == MemoryWrite)
                selectedFrame.setDirty(true);
            selectedFrame.setUnreserved(thread.getTask());
            pfEvent.notifyThreads();
        }
        /* end */
        page.notifyThreads();
        page.setValidatingThread(null);
        ThreadCB.dispatch();
        return retval;
    }

    /*
       Feel free to add methods/fields to improve the readability of your code
    */

}

/*
      Feel free to add local classes to improve the readability of your code
*/
