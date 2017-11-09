#pragma once
#include <vector>

class ByteInputStream;
class ByteOutputStream;

enum class StackNodeId
{
	Table,
	Function,
	UserData,
	Binary
};

class StackNode
{
public:
	StackNode(StackNodeId id);
	virtual ~StackNode() = default;
	virtual void Write(ByteOutputStream* stream);

	std::string key;
private:
	StackNodeId id;
};

class StackNodeContainer : StackNode
{
public:
	explicit StackNodeContainer(StackNodeId id)
		: StackNode(id)
	{
	}

	void AddChild(StackNode* child);

	~StackNodeContainer();
protected:
	void WriteChildren(ByteOutputStream* stream);
private:
	std::vector<StackNode*> m_children;
};

class StackNodeTable : StackNodeContainer
{
public:
	StackNodeTable();
};

class StackNodeBinary : StackNode
{
public:
	StackNodeBinary();

	char* data;
	size_t size;
};