###############################################################################
# Version 01/07/2015
# defines LIBZMQ_LDFLAGS, LIBZMQ_LIB
#
AC_DEFUN([AC_LIBZMQ],
[ 
  AC_ARG_WITH([zmq],
         AS_HELP_STRING([--with-zmq=PREFIX],[Specify zeromq library location]),
         [],
              [with_zmq=yes])

    ZEROMQ_LIBS=
    zmq_save_LIBS="$LIBS"
    if test $with_zmq != no; then
        if test $with_zmq != yes; then
            zeromq_possible_path="$with_zmq"
        else
            zeromq_possible_path="/usr/local /usr /opt /var"
        fi
        AC_MSG_CHECKING([for zeromq -lzmq])
        zeromq_found=no
        for zeromq_path_tmp in $zeromq_possible_path ; do
            LIBS="$LIBS $ZEROMQ_LIBS -lzmq"
            AC_COMPILE_IFELSE([AC_LANG_PROGRAM([[]],[[]])],
                        [ZEROMQ_LIBS="-L$zeromq_path_tmp/lib"
                         zeromq_found=yes]
                        [])
            if test $zeromq_found = yes; then
                break;
            fi
        done

        if test $zeromq_found = yes; then
                    
            LIBZMQ_LDFLAGS="$ZEROMQ_LIBS"
            LIBZMQ_LIB="-lzmq"
        
            AC_MSG_RESULT(yes)
            AC_SUBST(LIBZMQ_LDFLAGS)
            AC_SUBST(LIBZMQ_LIB)
        else
            AC_MSG_RESULT(no)
        fi
    fi
    LIBS="$zmq_save_LIBS"
])
