package test;

import fd_rx.NativeLib;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.freedesktop.dbus.FileDescriptor;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;

/**
 * Returns a FileDescriptor over the DBus
 */
public class ReturnFD implements ReturnFDInterface {

    private static final Logger logger = LogManager.getLogger();

    private DBusConnection m_conn;
    private FileDescriptor m_fd;

    ReturnFD() throws DBusException {
        m_conn = DBusConnection.getConnection( DBusConnection.DBusBusType.SESSION );
        m_conn.requestBusName( "test.filedescriptor" );
        m_conn.exportObject( "/", this );
        logger.debug( "Waiting for incoming data" );

        int[] pipe = new int[2];
        if( NativeLib.INSTANCE.pipe( pipe ) < 0 ){
            logger.error( "Can't make pipe" );
            System.exit(1);
        }

        // Send this file descriptor to the other process.
        m_fd = new FileDescriptor( pipe[ 1 ] );

        logger.debug( "About to read" );
        int[] data = new int[1];
        NativeLib.size_t ret = NativeLib.INSTANCE.read(pipe[0], data, new NativeLib.size_t(4));
        logger.debug( "ret is {}", ret );

        logger.debug( "read the following: {}", data[0]);
    }

    @Override
    public FileDescriptor getFileDescriptor() {
        return m_fd;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public String getObjectPath() {
        return "/";
    }

    public static void main(String[] args){
        try{
            new ReturnFD();
        }catch( Exception ex ){
            logger.error( ex );
            System.exit(1);
        }
    }

}
