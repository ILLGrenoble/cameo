###############################################################################
# Version 01/07/2015
# defines CAMEO_CFLAGS, CAMEO_LDFLAGS, CAMEO_LIBS
#
AC_DEFUN([AC_CAMEO],
[ 
  AC_ARG_WITH([cameo],
         AS_HELP_STRING([--with-cameo=PREFIX],[Specify cameo library location]),
         [],
              [with_cameo=yes])

    AC_LIBZMQ
    AC_LIBPROTOBUF_LITE
    AX_BOOST_THREAD

    CAMEO_CFLAGS=
    CAMEO_LIBS=
    if test $with_cameo != no; then
        if test $with_cameo != yes; then
            cameo_possible_path="$with_cameo"
        else
            cameo_possible_path="/usr/local /usr /opt /var"
        fi
        AC_MSG_CHECKING([for cameo headers])
        cameo_save_CXXFLAGS="$CXXFLAGS"
        cameo_found=no
        for cameo_path_tmp in $cameo_possible_path ; do
            # test include
            CXXFLAGS="$CXXFLAGS -I$cameo_path_tmp/include"
            AC_COMPILE_IFELSE([AC_LANG_PROGRAM([[#include <cameo/cameo.h>]],[[]])],
                        [CAMEO_CFLAGS="-I$cameo_path_tmp/include"
                         CAMEO_LIBS="-L$cameo_path_tmp/lib"
                         cameo_found=yes]
                        [])
            CXXFLAGS="$cameo_save_CXXFLAGS"
            if test $cameo_found = yes; then
                break;
            fi
        done

        AC_MSG_RESULT($cameo_found)
        if test $cameo_found = yes; then
            AC_MSG_CHECKING([for cameo -lcameo])
            cameo_save_LIBS="$LIBS"
            CXXFLAGS="$CXXFLAGS $CAMEO_CFLAGS"

            # search for library
            LIBS="$LIBS $CAMEO_LIBS $LIBZMQ_LDFLAGS $LIBPROTOBUF_LDFLAGS $BOOST_LDFLAGS $LIBZMQ_LIB $LIBPROTOBUF_LIB $BOOST_THREAD_LIB -lcameo"

            AC_LINK_IFELSE([AC_LANG_PROGRAM([[]],
                                     [[]])],
                     [ cameo_found=yes],
                     [ cameo_found=no])
            CXXFLAGS="$cameo_save_CXXFLAGS"
            LIBS="$cameo_save_LIBS"
            if test $cameo_found = yes; then
                    
                HAVE_CAMEO=1
                LIBS="$cameo_save_LIBS"
                CAMEO_LDFLAGS="$LIBZMQ_LDFLAGS $LIBPROTOBUF_LDFLAGS $CAMEO_LIBS"
                CAMEO_LIBS="$LIBZMQ_LIB $LIBPROTOBUF_LIB $BOOST_THREAD_LIB -lcameo"
            fi

            if test $cameo_found = yes; then
                AC_MSG_RESULT(yes)
                AC_SUBST(CAMEO_CFLAGS)
                AC_SUBST(CAMEO_LDFLAGS)
                AC_SUBST(CAMEO_LIBS)
            else
                AC_MSG_RESULT(no)
            fi
        fi
    fi
])
