
#ifndef LUA_DEBUG_SERVER_H
#define LUA_DEBUG_SERVER_H

class ByteOutputStream;
class ByteInputStream;
class DebugClient;

class DebugServerListener
{
public:
	virtual ~DebugServerListener() = default;

	virtual void onConnect(DebugClient* client) {}
	virtual void onDisconnect(DebugClient* client) {}
	virtual void handleStream(ByteInputStream* stream) {}
};

class DebugServer
{
	SOCKET m_listenSocket;
	DebugClient* m_client;
	ByteOutputStream* m_stream;
	DebugServerListener* m_listener;
	HANDLE m_connectionThread;
	int m_port;

friend class DebugClient;
public:
	DebugServer();
	~DebugServer();
	bool startup(u_short port, DebugServerListener* listener);
	void stop();
	void disConnectAll();
	DWORD connectionProc();
	bool sendMsg(const char* data, size_t size);
	void onDisconnect(DebugClient* client);
	int numConnections();
private:
	void onConnection(SOCKET newSocket);
};

class DebugClient
{
	SOCKET m_socket;
	DebugServer* m_server;
	HANDLE m_thread;
	ByteInputStream* m_msgStream;
public:
	DebugClient(DebugServer* server, SOCKET socket);
	~DebugClient();

	SOCKET getSocket() const { return m_socket; }
	void startup();
	bool sendMsg(const char* data, size_t size);
	void handleMsg(const char* data, size_t size);

	DWORD receive();
};

#endif
