if TESTS
TESTSDIR = test
endif

SUBDIRS = . $(TESTSDIR)

INCLUDES = -I$(top_srcdir)/src

lib_LTLIBRARIES = libcameo.la

# header files that must be in the dist package but not installed are sources
libcameo_la_SOURCES = \
	cameo/impl/Serializer.cpp \
	cameo/Event.cpp \
	cameo/StatusEvent.cpp \
	cameo/ResultEvent.cpp \
	cameo/PublisherEvent.cpp \
	cameo/PortEvent.cpp \
	cameo/RemoteException.cpp \
	cameo/ConnectionTimeout.cpp \
	cameo/StateException.cpp \
	cameo/PublisherCreationException.cpp \
	cameo/SubscriberCreationException.cpp \
	cameo/StarterServerException.cpp \
	cameo/ResponderCreationException.cpp \
	cameo/RequesterCreationException.cpp \
	cameo/impl/SocketImpl.cpp \
	cameo/impl/SocketWaitingImpl.cpp \
	cameo/impl/GenericWaitingImpl.cpp \
	cameo/impl/WaitingImplSet.cpp \
	cameo/impl/CancelIdGenerator.cpp \
	cameo/EventStreamSocket.cpp \
	cameo/Response.cpp \
	cameo/impl/PublisherImpl.cpp \
	cameo/impl/SubscriberImpl.cpp \
	cameo/impl/RequestImpl.cpp \
	cameo/impl/ResponderImpl.cpp \
	cameo/impl/RequesterImpl.cpp \
	cameo/Services.cpp \
	cameo/impl/ServicesImpl.cpp \
	cameo/Server.cpp \
	cameo/impl/HandlerImpl.cpp \
	cameo/impl/ApplicationImpl.cpp \
	cameo/Application.cpp \
	cameo/impl/SocketImpl.h \
	cameo/impl/WaitingImpl.h \
	cameo/impl/SocketWaitingImpl.h \
	cameo/impl/GenericWaitingImpl.h \
	cameo/impl/WaitingImplSet.h \
	cameo/impl/CancelIdGenerator.h \
	cameo/impl/PublisherImpl.h \
	cameo/impl/HandlerImpl.h \
	cameo/impl/ApplicationImpl.h \
	cameo/impl/ServicesImpl.h \
	cameo/impl/SubscriberImpl.h \
	cameo/impl/RequestImpl.h \
	cameo/impl/ResponderImpl.h \
	cameo/impl/RequesterImpl.h \
	cameo/impl/Serializer.h \
	cameo/impl/zmq.hpp \
	proto/Messages.pb.cc \
	proto/Messages.proto
		
# header files that are installed
nobase_include_HEADERS = \
	cameo/Application.h \
	cameo/ProtoType.h \
	cameo/ConnectionTimeout.h \
	cameo/Event.h \
	cameo/EventStreamSocket.h \
	cameo/PublisherCreationException.h \
	cameo/ResponderCreationException.h \
	cameo/RequesterCreationException.h \
	cameo/PublisherEvent.h \
	cameo/PortEvent.h \
	cameo/RemoteException.h \
	cameo/Response.h \
	cameo/Server.h \
	cameo/Services.h \
	cameo/StateException.h \
	cameo/StatusEvent.h \
	cameo/ResultEvent.h \
	cameo/SubscriberCreationException.h \
	cameo/StarterServerException.h \
	cameo/cameo.h
		
libcameo_la_CPPFLAGS = \
	$(CAMEO_CXXFLAGS) -export-dynamic
