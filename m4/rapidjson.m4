###############################################################################
# Version 27/11/2019
# defines RAPIDJSON_CFLAGS
#
AC_DEFUN([AC_RAPIDJSON],
[ 
  AC_ARG_WITH([rapidjson],
         AS_HELP_STRING([--with-rapidjson=PREFIX],[Specify rapidjson library location]),
         [],
              [with_rapidjson=yes])

    RAPIDJSON_CFLAGS=
    if test $with_rapidjson != no; then
        if test $with_rapidjson != yes; then
            rapidjson_possible_path="$with_rapidjson"
        else
            rapidjson_possible_path="/usr/local /usr /opt /var"
        fi
        AC_MSG_CHECKING([for rapidjson headers])
        rapidjson_save_CXXFLAGS="$CXXFLAGS"
        rapidjson_found=no
        for rapidjson_path_tmp in $rapidjson_possible_path ; do
            # test include
            CXXFLAGS="$CXXFLAGS -I$rapidjson_path_tmp/include"
            AC_COMPILE_IFELSE([AC_LANG_PROGRAM([[#include <rapidjson/rapidjson.h>]],[[]])],
                        [RAPIDJSON_CFLAGS="-I$rapidjson_path_tmp/include"
                         rapidjson_found=yes]
                        [])
            CXXFLAGS="$rapidjson_save_CXXFLAGS"
            if test $rapidjson_found = yes; then
                break;
            fi
        done

        AC_MSG_RESULT($rapidjson_found)
        if test $rapidjson_found = yes; then
            HAVE_RAPIDJSON=1
            AC_MSG_RESULT(yes)
            AC_SUBST(RAPIDJSON_CFLAGS)
        else
            AC_MSG_RESULT(no)
        fi
    fi
])
