SUBDIRS = .

INCLUDES = -I$(top_srcdir)/src

lib_LTLIBRARIES = libcameo.la

# header files that must be in the dist package but not installed are sources
libcameo_la_SOURCES = \
	cameo/message/JSON.cpp \
	cameo/Serializer.cpp \
	cameo/TimeCondition.cpp \
	cameo/Event.cpp \
	cameo/StatusEvent.cpp \
	cameo/ResultEvent.cpp \
	cameo/PublisherEvent.cpp \
	cameo/PortEvent.cpp \
	cameo/CancelEvent.cpp \
	cameo/EventListener.cpp \
	cameo/RemoteException.cpp \
	cameo/InvalidArgumentException.cpp \
	cameo/UnmanagedApplicationException.cpp \
	cameo/ConnectionTimeout.cpp \
	cameo/SocketException.cpp \
	cameo/PublisherCreationException.cpp \
	cameo/SubscriberCreationException.cpp \
	cameo/StarterServerException.cpp \
	cameo/ResponderCreationException.cpp \
	cameo/RequesterCreationException.cpp \
	cameo/Response.cpp \
	cameo/impl/StreamSocketImpl.cpp \
	cameo/impl/RequestSocketImpl.cpp \
	cameo/impl/SocketWaitingImpl.cpp \
	cameo/impl/GenericWaitingImpl.cpp \
	cameo/impl/WaitingImplSet.cpp \
	cameo/impl/CancelIdGenerator.cpp \
	cameo/impl/HandlerImpl.cpp \
	cameo/impl/ServicesImpl.cpp \
	cameo/impl/PublisherImpl.cpp \
	cameo/impl/SubscriberImpl.cpp \
	cameo/impl/RequestImpl.cpp \
	cameo/impl/ResponderImpl.cpp \
	cameo/impl/RequesterImpl.cpp \
	cameo/ConnectionChecker.cpp \
	cameo/EventStreamSocket.cpp \
	cameo/OutputStreamSocket.cpp \
	cameo/EventThread.cpp \
	cameo/Services.cpp \
	cameo/Server.cpp \
	cameo/Application.cpp \
	cameo/impl/StreamSocketImpl.h \
	cameo/impl/RequestSocketImpl.h \
	cameo/impl/WaitingImpl.h \
	cameo/impl/SocketWaitingImpl.h \
	cameo/impl/GenericWaitingImpl.h \
	cameo/impl/WaitingImplSet.h \
	cameo/impl/CancelIdGenerator.h \
	cameo/impl/PublisherImpl.h \
	cameo/impl/HandlerImpl.h \
	cameo/impl/ServicesImpl.h \
	cameo/impl/SubscriberImpl.h \
	cameo/impl/RequestImpl.h \
	cameo/impl/ResponderImpl.h \
	cameo/impl/RequesterImpl.h
		
# header files that are installed
nobase_include_HEADERS = \
	cameo/message/JSON.h \
	cameo/Serializer.h \
	cameo/TimeCondition.h \
	cameo/Application.h \
	cameo/ConnectionTimeout.h \
	cameo/Event.h \
	cameo/EventThread.h \
	cameo/ConcurrentQueue.h \
	cameo/EventListener.h \
	cameo/EventStreamSocket.h \
	cameo/OutputStreamSocket.h \
	cameo/PublisherCreationException.h \
	cameo/ResponderCreationException.h \
	cameo/RequesterCreationException.h \
	cameo/PublisherEvent.h \
	cameo/PortEvent.h \
	cameo/CancelEvent.h \
	cameo/RemoteException.h \
	cameo/Response.h \
	cameo/ConnectionChecker.h \
	cameo/Server.h \
	cameo/Services.h \
	cameo/InvalidArgumentException.h \
	cameo/UnmanagedApplicationException.h \
	cameo/SocketException.h \
	cameo/StatusEvent.h \
	cameo/ResultEvent.h \
	cameo/SubscriberCreationException.h \
	cameo/StarterServerException.h \
	cameo/cameo.h
		
libcameo_la_CPPFLAGS = \
	$(CAMEO_CXXFLAGS)

libcameo_la_LDFLAGS = \
	-version-number $(LIBRARY_VERSION) -export-dynamic $(LDFLAGS)
