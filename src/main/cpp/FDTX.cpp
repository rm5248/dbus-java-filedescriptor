#include <dbus-cxx.h>
#include <iostream>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <time.h>

static char datebuffer[ 128 ];

void mylog( const char* logger_name, const struct SL_LogLocation* location,
      const enum SL_LogLevel level,
      const char* log_string ){
    time_t now = time( NULL );
    struct tm* timeinfo;
 
    timeinfo = localtime( &now );
    strftime( datebuffer, 128, "%H:%M:%S", timeinfo );
    std::cerr << "[" << datebuffer << "] " 
        <<  logger_name << " - " << log_string << std::endl;
}

int main( int argc, char** argv ){
  /* This program creates a pipe and gives it to the server to write to
   */
  int pipe_fds[2];
  uint8_t data[128];

  DBus::setLoggingFunction( mylog );

  DBus::init();
  DBus::Dispatcher::pointer dispatcher = DBus::Dispatcher::create();
  DBus::Connection::pointer conn = dispatcher->create_connection(DBus::BUS_SESSION);
  DBus::ObjectProxy::pointer object = conn->create_object_proxy("test.filedescriptor", "/");

  DBus::MethodProxy<void,DBus::FileDescriptor::pointer>& methodref = *(object->create_method<void,DBus::FileDescriptor::pointer>("fd_rx.FDInterface", "setFiledescriptor"));

  if( pipe( pipe_fds ) < 0 ){
    std::cerr << "Can't create pipes" << std::endl;
    return 1;
  }

  std::cout << "Running..." << std::flush;

  // Tell the server what FD to write to
try{
  DBus::FileDescriptor::pointer fd = DBus::FileDescriptor::create( pipe_fds[1] );
  methodref( fd );
}catch( std::shared_ptr<DBus::Error> ex ){
  std::cerr << "Got error: " << ex->what() << std::endl;
}
  
  if( read( pipe_fds[0], data, 128 ) < 0 ){
    std::cerr << "Error reading from file descriptor: " << strerror( errno ) << std::endl;
    return 1;
  }
  
  std::cout << "Done!" << std::endl;
 
  return 0;
}
