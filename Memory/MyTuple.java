package osp.Memory;

/**
 * My Custom Tuple Class
 * 
 */
public class MyTuple { 
    public final int status; 
    public final FrameTableEntry freeFrame;

    public MyTuple(int status, FrameTableEntry freeFrame) { 
      this.status = status;
      this.freeFrame = freeFrame;

    }
    
    public int getStatus()
    {
      return status;
    }

    public FrameTableEntry getFreeFrame() {
      return freeFrame;
    }
  } 