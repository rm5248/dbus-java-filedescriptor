/***************************************************************************
 *   Copyright (C) 2007,2010 by Rick L. Vinyard, Jr.                       *
 *   rvinyard@cs.nmsu.edu                                                  *
 *   Copyright (C) 2017 by Robert Middleton                                *
 *                                                                         *
 *   This file is part of the dbus-cxx library.                            *
 *                                                                         *
 *   The dbus-cxx library is free software; you can redistribute it and/or *
 *   modify it under the terms of the GNU General Public License           *
 *   version 3 as published by the Free Software Foundation.               *
 *                                                                         *
 *   The dbus-cxx library is distributed in the hope that it will be       *
 *   useful, but WITHOUT ANY WARRANTY; without even the implied warranty   *
 *   of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU   *
 *   General Public License for more details.                              *
 *                                                                         *
 *   You should have received a copy of the GNU General Public License     *
 *   along with this software. If not see <http://www.gnu.org/licenses/>.  *
 ***************************************************************************/

#include <dbus-cxx.h>
#include <iostream>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <fcntl.h>
#include <sys/socket.h>

static int pipes[2];
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

std::shared_ptr<DBus::FileDescriptor> getFiledescriptor(){
  return DBus::FileDescriptor::create( pipes[1] );
}

void setFiledescriptor( std::shared_ptr<DBus::FileDescriptor> desc ){
  std::cerr << "Setting the file descriptor.  descriptor int is " << desc->getDescriptor() << std::endl;
  char toWrite = 'f';

  write( desc->getDescriptor(), &toWrite, 1 );
}

static void msghdr_sizes(){
    struct msghdr hdr = { 0 };
    struct cmsghdr* cmsg = CMSG_FIRSTHDR(&hdr);

    std::cout << "sizeof(cmsghdr->cmsg_len) is " << sizeof(cmsg->cmsg_len) << std::endl;
    std::cout << "sizeof(cmsghdr->cmsg_level) is " << sizeof(cmsg->cmsg_level) << std::endl;
    std::cout << "sizeof(cmsghdr->cmsg_type) is " << sizeof(cmsg->cmsg_type) << std::endl;
    std::cout << "cmsg len is " << CMSG_LEN(sizeof(int)) << std::endl;
    std::cout << "controllen is " << CMSG_SPACE(sizeof(int)) << std::endl;
}

#define NUM_FD 1
static void msghdr_linux_manpage(){
    struct msghdr msg = {0};
    struct cmsghdr *cmsg;
    int myfds[NUM_FD]; /* Contains the file descriptors to pass. */
    char buf[CMSG_SPACE(sizeof myfds)];  /* ancillary data buffer */
    int *fdptr;

    myfds[0] = 4;

    msg.msg_control = buf;
    msg.msg_controllen = sizeof buf;
    cmsg = CMSG_FIRSTHDR(&msg);
    cmsg->cmsg_level = SOL_SOCKET;
    cmsg->cmsg_type = SCM_RIGHTS;
    cmsg->cmsg_len = CMSG_LEN(sizeof(int) * NUM_FD);
    /* Initialize the payload: */
    fdptr = (int *) CMSG_DATA(cmsg);
    memcpy(fdptr, myfds, NUM_FD * sizeof(int));
    /* Sum of the length of all control messages in the buffer: */
    msg.msg_controllen = cmsg->cmsg_len;

    std::cout << "sizeof size_t=" << sizeof(size_t) << std::endl;
    std::cout << "msg_control={" << std::endl;
    std::cout << "  cmsg {" << std::endl;
    std::cout << "    cmsg_len=" << cmsg->cmsg_len << std::endl;
    std::cout << "    cmsg_level=" << cmsg->cmsg_level << std::endl;
    std::cout << "    cmsg_type=" << cmsg->cmsg_type << std::endl;
    std::cout << "    cmsg_data=" << *fdptr << std::endl;
    std::cout << "  }" << std::endl;
    std::cout << "msg_controllen=" << msg.msg_controllen << std::endl;
    std::cout << "CMSG_LEN(4)=" << CMSG_LEN(4) << std::endl;
    std::cout << "CMSG_SPACE(4)=" << CMSG_SPACE(4) << std::endl;
}

int main( int argc, char** argv ){
  //uncomment the following line to enable logging from the library.
  //DBus::setLoggingFunction( mylog );

  msghdr_sizes();
  msghdr_linux_manpage();

  DBus::init();
  std::shared_ptr<DBus::Dispatcher> dispatcher = DBus::Dispatcher::create();
  std::shared_ptr<DBus::Connection> conn = dispatcher->create_connection(DBus::BUS_SESSION);

  DBus::ObjectProxy::pointer objectproxy = conn->create_object_proxy("test.filedescriptor", "/");
  DBus::MethodProxy<DBus::FileDescriptor::pointer>& methodref = *(objectproxy->create_method<DBus::FileDescriptor::pointer>("test.ReturnFDInterface", "getFileDescriptor"));

  std::shared_ptr<DBus::FileDescriptor> desc = methodref();
  std::cout << "GOT FD " << desc->getDescriptor() << std::endl;
  int val = 55;
  if( write( desc->getDescriptor(), &val, 4 ) < 0 ){
      std::cout << "error: " << strerror(errno) << std::endl;
  }

  std::cout << "Done writing!" <<  std::endl;
}
