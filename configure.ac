# -----------------------------------------------------------------------------
#
# Autoconf
#
# -----------------------------------------------------------------------------

AC_INIT(cameo-api-cpp, 0.0.1)
LIBRARY_VERSION=0:0:1

AC_CONFIG_AUX_DIR(config)
AC_CONFIG_SRCDIR(src/cameo/Application.cpp)

AM_INIT_AUTOMAKE([subdir-objects])

AC_PROG_CXX
AC_LANG(C++)

AM_DISABLE_STATIC

AC_PROG_LIBTOOL

AC_CONFIG_MACRO_DIR([m4])

AC_ARG_ENABLE(tests,
             [  --enable-tests compile test applications],
             [case "${enableval}" in
             yes) tests=true ;;
             no)  tests=false ;;
             *) AC_MSG_ERROR(bad value ${enableval} for --enable-tests) ;;
             esac],[tests=false])
AM_CONDITIONAL([TESTS], [test "$tests" = true])

AC_SUBST(LIBRARY_VERSION)

# temporarily disabled
#AX_CXX_COMPILE_STDCXX_11
AX_PTHREAD(,[AC_MSG_ERROR([Posix threads required])])
AX_BOOST_BASE([1.41],, [AC_MSG_ERROR([Boost 1.41 required])])
AX_BOOST_THREAD
AX_BOOST_SYSTEM
AC_ZMQ
AC_PROTOBUF_LITE

CAMEO_CXXFLAGS="$BOOST_CPPFLAGS \
	$ZMQ_CFLAGS \
	$PROTOBUF_CFLAGS"
	
CAMEO_LDFLAGS="$BOOST_LDFLAGS \
	$ZMQ_LDFLAGS \
	$PROTOBUF_LDFLAGS"
 
CAMEO_LIBS="$BOOST_THREAD_LIB \
	$BOOST_SYSTEM_LIB \
	$ZMQ_LIB \
	$PROTOBUF_LIB"

AC_SUBST(CAMEO_CXXFLAGS)
AC_SUBST(CAMEO_LDFLAGS)
AC_SUBST(CAMEO_LIBS)

top_srcdir=.

AC_OUTPUT([
Makefile
src/Makefile
src/test/Makefile
])
