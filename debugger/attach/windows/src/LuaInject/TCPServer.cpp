#include <WinSock2.h>

#include <thread>
#include "Stream.h"
#include "DebugBackend.h"
#include "TCPServer.h"
// Need to link with Ws2_32.lib
#pragma comment (lib, "Ws2_32.lib")

DWORD WINAPI connectionThread(LPVOID param)
{
	DebugServer* self = (DebugServer*)param;
	return self->connectionProc();
}

DebugServer::DebugServer() : m_listenSocket(0) , m_client(nullptr) , m_listener(nullptr), m_connectionThread(nullptr), m_port(0)
{
	m_stream = new ByteInStream();
}

DebugServer::~DebugServer()
{
	delete m_stream;
}

bool DebugServer::startup(int port, DebugServerListener* listener)
{
	m_port = port;
	m_listener = listener;

	//----------------------
	// Initialize Winsock.
	WSADATA wsaData;
	int iResult = WSAStartup(MAKEWORD(2, 2), &wsaData);
	if (iResult != NO_ERROR) {
		wprintf(L"WSAStartup failed with error: %ld\n", iResult);
		return false;
	}
	//----------------------
	// Create a SOCKET for listening for
	// incoming connection requests.
	m_listenSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if (m_listenSocket == INVALID_SOCKET) {
		wprintf(L"socket failed with error: %ld\n", WSAGetLastError());
		WSACleanup();
		return false;
	}
	//----------------------
	// The sockaddr_in structure specifies the address family,
	// IP address, and port for the socket that is being bound.
	sockaddr_in addrServer;
	addrServer.sin_family = AF_INET;
	addrServer.sin_addr.s_addr = htonl(INADDR_ANY); //实际上是0
	addrServer.sin_port = htons(port);


	//绑定套接字到一个IP地址和一个端口上
	if (bind(m_listenSocket, (SOCKADDR *)& addrServer, sizeof(addrServer)) == SOCKET_ERROR) {
		wprintf(L"bind failed with error: %ld\n", WSAGetLastError());
		closesocket(m_listenSocket);
		WSACleanup();
		return false;
	}

	//将套接字设置为监听模式等待连接请求
	//----------------------
	// Listen for incoming connection requests.
	// on the created socket
	if (listen(m_listenSocket, 1) == SOCKET_ERROR) {
		wprintf(L"listen failed with error: %ld\n", WSAGetLastError());
		closesocket(m_listenSocket);
		WSACleanup();
		return false;
	}

	DWORD threadId;
	m_connectionThread = CreateThread(nullptr, 0, connectionThread, this, 0, &threadId);
	connectionThread(this);
	return true;
}

DWORD DebugServer::connectionProc()
{
	SOCKADDR_IN addrClient;
	int len = sizeof(SOCKADDR);
	//以一个无限循环的方式，不停地接收客户端socket连接
	while (true)
	{
		printf("wait for connection.\n");
		//请求到来后，接受连接请求，返回一个新的对应于此次连接的套接字
		SOCKET AcceptSocket = accept(m_listenSocket, (SOCKADDR*)&addrClient, &len);
		if (AcceptSocket == INVALID_SOCKET)
		{
			break;
		}
		onConnection(AcceptSocket);
	}

	closesocket(m_listenSocket);
	WSACleanup();
	m_listenSocket = INVALID_SOCKET;
	return 0;
}

void DebugServer::stop()
{
	closesocket(m_listenSocket);
	WSACleanup();
}

void DebugServer::disConnectAll()
{
	if (m_client != nullptr)
	{
		closesocket(m_client->getSocket());
		delete m_client;
		m_client = nullptr;
	}
}

void DebugServer::sendMsg(const char * data, size_t size)
{
	if (m_client != nullptr)
	{
		m_client->sendMsg(data, size);
	}
}

void DebugServer::WriteUInt32(unsigned int value) const
{
	m_stream->WriteUInt32(value);
}

void DebugServer::WriteSize(size_t size) const
{
	m_stream->WriteSize(size);
}

void DebugServer::WriteString(const char * value) const
{
	m_stream->WriteString(value);
}

void DebugServer::WriteString(const std::string & value) const
{
	m_stream->WriteString(value);
}

void DebugServer::WriteBool(bool value) const
{
	WriteUInt32(value ? 1 : 0);
}

void DebugServer::Flush()
{
	char* buff = (char*)malloc(m_stream->GetSize());
	m_stream->Reset();
	sendMsg(buff, m_stream->GetSize());
	free(buff);
}

void DebugServer::onDisconnection(DebugClient * client)
{
	m_client = nullptr;
}

void DebugServer::onConnection(SOCKET newSocket)
{
	if (m_client == nullptr)
	{
		printf("--> onConnection.\n");
		m_client = new DebugClient(this, newSocket);
		m_client->startup();
		m_listener->onConnection(m_client);
	}
	else closesocket(newSocket);
}

DWORD WINAPI receiveThread(LPVOID param)
{
	printf("receiveThread\n");
	DebugClient* client = (DebugClient*)param;

	return client->receive();
}

void DebugClient::startup()
{
	DWORD threadId;
	m_thread = CreateThread(nullptr, 0, receiveThread, this, 0, &threadId);
}

void DebugClient::sendMsg(const char * data, size_t size)
{
	int leftSize = size;
	int totalSend = 0;
	while (leftSize > 0)
	{
		int sendCount = send(m_socket, data + totalSend, leftSize, 0);
		if (sendCount == SOCKET_ERROR)
		{
			break;
		}
		leftSize -= sendCount;
		totalSend += sendCount;
	}
}

void DebugClient::handleMsg(const char * data, size_t size)
{
	char* temp = (char*)malloc(size);
	memcpy(temp, data, size);
	ByteOutStream os(temp, size);
	this->m_server->m_listener->handleStream(&os);
}

DWORD DebugClient::receive()
{
	ByteInStream is;

	const int BUFF_SIZE = 1024 * 1024;
	char* buff = (char*)malloc(BUFF_SIZE);
	int buffLen = 0;

	int msgLen = 0;

	while (true)
	{
		int count = recv(m_socket, buff, BUFF_SIZE, 0);
		if (count == 0 || count == SOCKET_ERROR) //disconnected
		{
			m_server->onDisconnection(this);
			break;
		}

		buffLen += count;
		//split packs
		int bufPos = 0;
		while (bufPos < buffLen)
		{
			int remain = buffLen - bufPos;
			//get size of message pack
			if (msgLen == 0)
			{
				if (remain < 4)
					break;
				msgLen = *(int*)(buff + bufPos);
				bufPos += sizeof(int);
			}
			remain = buffLen - bufPos;
			//read bytes
			int rc = min(remain, msgLen);
			is.Write(buff + bufPos, rc);
			bufPos += rc;
			msgLen -= rc;

			//check full
			if (msgLen == 0)
			{
				handleMsg(is.GetBuf(), msgLen);
				is.Reset();
			}
		}
	}

	free(buff);
	return 0;
}