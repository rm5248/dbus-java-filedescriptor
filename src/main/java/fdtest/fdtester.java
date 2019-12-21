/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fdtest;

import java.io.IOException;
import java.nio.ByteBuffer;
import jnr.constants.platform.AddressFamily;
import jnr.constants.platform.Sock;
import jnr.posix.CmsgHdr;
import jnr.posix.MsgHdr;
import jnr.posix.POSIX;
import jnr.posix.POSIXFactory;

/**
 *
 * @author robert
 */
public class fdtester {

    private static POSIX posix;
    public static final int SCM_RIGHTS = 1;
    private static final int MSG_NOSIGNAL = 16384;

    static void child(int pipeRead){
//        try{
//            Thread.sleep(500);
//        }catch( Exception ex ){}

        System.out.println( "in child" );
        MsgHdr inMessage = posix.allocateMsgHdr();
        System.out.println( "after msghdr" );
        ByteBuffer[] inData = new ByteBuffer[1];
        inData[0] = ByteBuffer.allocateDirect(10);

        System.out.println( "Child about to allcoate control" );
        inMessage.allocateControl(4);
        inMessage.setIov(inData);

        System.out.println( "Child about to read" );
        int bytesRead = posix.recvmsg(pipeRead, inMessage, 0);
        if( bytesRead < 0 ){
            int errno = posix.errno();
            System.out.println( "Unable to receive data: " + posix.strerror(errno) );
        }

        System.out.println( inMessage );
        if( inMessage.getControlLen() == 0 ){
            System.exit(0);
            return;
        }
        System.out.println( "Got fd " + inMessage.getControls()[0].getData().asIntBuffer().get() );
    }

    static void parent(int pipeWrite){
        System.out.println( "in parent" );


        int fd = posix.open("/tmp/z7.c", 0, 0);

        System.out.println( "Got fd of open " + fd );

        MsgHdr msghdr = posix.allocateMsgHdr();

        CmsgHdr cmsghdr = msghdr.allocateControl(4);
        cmsghdr.setType(jnr.constants.platform.SocketLevel.SOL_SOCKET.intValue());
        cmsghdr.setLevel(SCM_RIGHTS);
        ByteBuffer bb = ByteBuffer.allocateDirect(4);
        bb.putInt( fd );
        bb.flip();
        //System.out.println( "bb contains " + bb.getInt() );
        cmsghdr.setData(bb);

        ByteBuffer dataSend = ByteBuffer.allocateDirect(10);
        dataSend.put((byte)5);
        dataSend.put((byte)6);
        dataSend.put((byte)7);
        dataSend.put((byte)8);
        dataSend.flip();
        msghdr.setIov(new ByteBuffer[]{dataSend});

        System.out.println( "Message to send on FD: " + pipeWrite + " " + msghdr );
        int bytesWrote = posix.sendmsg(pipeWrite, msghdr, 0);
        if( bytesWrote < 0 ){
            int errno = posix.errno();
            System.out.println( "Unable to send data: " + posix.strerror(errno) );
            System.out.println( errno + "" );
        }

        try{
            Thread.sleep(1500);
        }catch( Exception ex ){
            
        }
    }

    public static void main(String[] args){
        posix = POSIXFactory.getPOSIX();

        posix.allocateMsgHdr();
        System.out.println( "Allocated msghdr" );

        int[] fds = {0, 0};

        int ret = posix.socketpair(AddressFamily.AF_UNIX.intValue(),
                Sock.SOCK_DGRAM.intValue(), 0, fds);

        if( ret < 0 ){
            System.out.println( "Unabel to create socket pair" );
            return;
        }

        parent(fds[0]);
        System.exit(0);

        int pid = posix.fork();

        if( pid > 0 ){

            System.out.println("parent about to exit" );
            System.exit(0);
            posix.close(fds[1]);
            parent(fds[0]);
        }else{
            System.out.println( "in child!" );
                    try{
            Thread.sleep(1500);
        }catch( Exception ex ){}
            posix.allocateMsgHdr();
            System.out.println( "Allocated msghdr in child" );
            posix.close(fds[0]);
            child(fds[1]);
        }

        System.exit(0);
    }
}
