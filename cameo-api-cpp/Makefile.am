SUBDIRS = src

PROTO_PATH=$(top_srcdir)/src/proto

proto:
	protoc -I=$(PROTO_PATH) --cpp_out=$(PROTO_PATH) $(PROTO_PATH)/Messages.proto	
