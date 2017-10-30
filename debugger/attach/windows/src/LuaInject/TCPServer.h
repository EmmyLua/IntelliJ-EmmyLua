
#ifndef LUA_DEBUG_SERVER_H
#define LUA_DEBUG_SERVER_H

#include <string>

class ByteInStream;
class ByteOutStream;
class DebugClient;

class DebugServerListener
{
public:
	virtual ~DebugServerListener() = default;

	virtual void onConnection(DebugClient* client) {}
	virtual void handleStream(ByteOutStream* stream) {}
};

class DebugServer
{
	SOCKET m_listenSocket;
	DebugClient* m_client;
	ByteInStream* m_stream;
	DebugServerListener* m_listener;
	HANDLE m_connectionThread;
	int m_port;

friend class DebugClient;
public:
	DebugServer();
	~DebugServer();
	bool startup(int port, DebugServerListener* listener);
	void stop();
	void disConnectAll();
	DWORD connectionProc();
	void sendMsg(const char* data, size_t size);

	void WriteUInt32(unsigned int value) const;

	void WriteSize(size_t size) const;

	/**
	* Writes a string to the channel and returns immediately.
	*/
	void WriteString(const char* value) const;

	/**
	* Writes a string to the channel and returns immediately.
	*/
	void WriteString(const std::string& value) const;

	/**
	* Writes a boolean to the channel and returns immediately.
	*/
	void WriteBool(bool value) const;

	void Flush();

	void onDisconnection(DebugClient* client);
private:
	void onConnection(SOCKET newSocket);
};

class DebugClient
{
	SOCKET m_socket;
	DebugServer* m_server;
	HANDLE m_thread;
public:
	DebugClient(DebugServer* server, SOCKET socket) : m_socket(socket), m_server(server), m_thread(nullptr)
	{}
	SOCKET getSocket() const { return m_socket; }
	void startup();
	void sendMsg(const char* data, size_t size);
	void handleMsg(const char* data, size_t size);

	DWORD receive();
};

#endif
