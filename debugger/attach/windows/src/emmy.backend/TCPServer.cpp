#include <WinSock2.h>

#include <thread>
#include "Stream.h"
#include "DebugBackend.h"
#include "TCPServer.h"
#include "endian.h"
// Need to link with Ws2_32.lib
#pragma comment (lib, "Ws2_32.lib")

DWORD WINAPI connectionThread(LPVOID param)
{
	DebugServer* self = (DebugServer*)param;
	return self->connectionProc();
}

DebugServer::DebugServer() : m_listenSocket(0) , m_client(nullptr) , m_listener(nullptr), m_connectionThread(nullptr), m_port(0)
{
	m_stream = new ByteOutputStream();
}

DebugServer::~DebugServer()
{
	delete m_stream;
}

bool DebugServer::startup(u_short port, DebugServerListener* listener)
{
	m_port = port;
	m_listener = listener;

	//----------------------
	// Initialize Winsock.
	WSADATA wsaData;
	int iResult = WSAStartup(MAKEWORD(2, 2), &wsaData);
	if (iResult != NO_ERROR) {
		//wprintf(L"WSAStartup failed with error: %ld\n", iResult);
		return false;
	}
	//----------------------
	// Create a SOCKET for listening for
	// incoming connection requests.
	m_listenSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
	if (m_listenSocket == INVALID_SOCKET) {
		//wprintf(L"socket failed with error: %ld\n", WSAGetLastError());
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
		//wprintf(L"bind failed with error: %ld\n", WSAGetLastError());
		closesocket(m_listenSocket);
		WSACleanup();
		return false;
	}

	//将套接字设置为监听模式等待连接请求
	//----------------------
	// Listen for incoming connection requests.
	// on the created socket
	if (listen(m_listenSocket, 1) == SOCKET_ERROR) {
		//wprintf(L"listen failed with error: %ld\n", WSAGetLastError());
		closesocket(m_listenSocket);
		WSACleanup();
		return false;
	}

	//printf("begin tcp server on port:%d\n", port);

	DWORD threadId;
	m_connectionThread = CreateThread(nullptr, 0, connectionThread, this, 0, &threadId);
	return true;
}

DWORD DebugServer::connectionProc()
{
	SOCKADDR_IN addrClient;
	int len = sizeof(SOCKADDR);
	//以一个无限循环的方式，不停地接收客户端socket连接
	while (true)
	{
		//printf("wait for connection.\n");
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

bool DebugServer::sendMsg(const char * data, size_t size)
{
	if (m_client != nullptr)
	{
		return m_client->sendMsg(data, size);
	}
	return true;
}

void DebugServer::onDisconnect(DebugClient * client)
{
	m_client = nullptr;
	m_listener->onDisconnect(client);
}

int DebugServer::numConnections()
{
	return m_client == nullptr ? 0 : 1;
}

void DebugServer::onConnection(SOCKET newSocket)
{
	if (m_client == nullptr)
	{
		//printf("--> onConnection.\n");
		m_client = new DebugClient(this, newSocket);
		m_client->startup();
		m_listener->onConnect(m_client);
	}
	else closesocket(newSocket);
}

DWORD WINAPI receiveThread(LPVOID param)
{
	//printf("receiveThread\n");
	DebugClient* client = (DebugClient*)param;

	return client->receive();
}

DebugClient::DebugClient(DebugServer* server, SOCKET socket) : m_socket(socket), m_server(server), m_thread(nullptr)
{
	m_msgStream = new ByteInputStream;
}

DebugClient::~DebugClient()
{
	if (m_msgStream != nullptr)
		delete m_msgStream;
}

void DebugClient::startup()
{
	DWORD threadId;
	m_thread = CreateThread(nullptr, 0, receiveThread, this, 0, &threadId);
}

bool DebugClient::sendMsg(const char * data, size_t size)
{
	size_t leftSize = size;
	size_t totalSend = 0;
	while (leftSize > 0)
	{
		const size_t sendCount = send(m_socket, data + totalSend, leftSize, 0);
		if (sendCount == SOCKET_ERROR)
		{
			return false;
		}
		leftSize -= sendCount;
		totalSend += sendCount;
	}
	return true;
}

void DebugClient::handleMsg(const char * data, size_t size)
{
	m_msgStream->Reset(data, size);
	this->m_server->m_listener->handleStream(m_msgStream);
}

DWORD DebugClient::receive()
{
	ByteOutputStream is;

	const int BUFF_SIZE = 1024 * 1024;
	char* buff = (char*)malloc(BUFF_SIZE);
	int bufLen = 0;

	int msgLen = 0;

	while (true)
	{
		int count = recv(m_socket, buff, BUFF_SIZE, 0);
		//printf("receive msg pack size : %d\n", count);

		if (count == 0 || count == SOCKET_ERROR) //disconnected
		{
			m_server->onDisconnect(this);
			break;
		}

		bufLen += count;
		//split packs
		int bufPos = 0;
		while (bufPos < bufLen)
		{
			int remain = bufLen - bufPos;
			//get size of message pack
			if (msgLen == 0)
			{
				if (remain < 4) {
					break;
				}
				msgLen = readInt32InBigEndian(buff + bufPos);

				//printf("\t>msg len: %d\n", msgLen);
				bufPos += sizeof(int);
			}
			remain = bufLen - bufPos;
			//read bytes
			int rc = min(remain, msgLen);
			is.Write(buff + bufPos, rc);
			bufPos += rc;
			msgLen -= rc;

			//check full
			if (msgLen == 0)
			{
				//printf("\t>handle msg.\n");
				handleMsg(is.GetBuf(), is.GetPositon());
				is.Reset();
			}
		}
		bufLen -= bufPos;
	}

	free(buff);
	return 0;
}