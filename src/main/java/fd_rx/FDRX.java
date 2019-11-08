package fd_rx;

import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;


public class FDRX implements FDInterface {

    private static final Logger logger = LogManager.getLogger();
    
    private DBusConnection m_conn;

    FDRX() throws DBusException{
        m_conn = DBusConnection.getConnection( DBusConnection.DBusBusType.SESSION );
        m_conn.requestBusName( "test.filedescriptor" );
        m_conn.exportObject( "/", this );
        logger.debug( "Waiting for incoming data" );
    }

    @Override
    public void setFileDescriptor(FileDescriptor fd) {
        logger.debug( "Got the file descriptor.  Valid? {}", fd.valid() );
        try{
            DataOutputStream dos = new DataOutputStream( new FileOutputStream( fd ) );
            dos.writeChars( "hi" );
        }catch( IOException ex ){
            logger.error( "IOException: ", ex );
        }
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public String getObjectPath() {
        return "/";
    }

    public static void main(String[] args ) throws DBusException {
        new FDRX();
    }

}
