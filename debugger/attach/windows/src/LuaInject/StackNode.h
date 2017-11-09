#pragma once
#include <vector>

class ByteInputStream;
class ByteOutputStream;

enum class StackNodeId
{
	List,
	Eval,
	StackRoot,

	Table,
	Function,
	UserData,
	String,
	Binary,
	Primitive,

	Error,
};

class StackNode
{
public:
	StackNode(StackNodeId id);
	virtual ~StackNode() = default;
	virtual void Write(ByteOutputStream* stream);

private:
	StackNodeId id;
};

class StackLuaObjectNode : public StackNode
{
public:
	StackLuaObjectNode(StackNodeId id) : StackNode(id) {}

	void Write(ByteOutputStream* stream) override;

	std::string name;
	std::string type;
};

class StackNodeContainer : public StackNode
{
public:
	explicit StackNodeContainer(StackNodeId id)
		: StackNode(id)
	{
	}

	void AddChild(StackNode* child);
	void Write(ByteOutputStream* stream) override;

	~StackNodeContainer();

private:
	void WriteChildren(ByteOutputStream* stream);

	std::vector<StackNode*> m_children;
};

class EvalResultNode : public StackNodeContainer
{
public:
	EvalResultNode();

	void Write(ByteOutputStream* stream) override;

	bool success;
	std::string error;
};

class StackRootNode : public StackNodeContainer
{
public:
	StackRootNode();

	void Write(ByteOutputStream* stream) override;

	int scriptIndex;
	int line;
	std::string functionName;
};

class StackTableNode : public StackLuaObjectNode
{
	struct KVPair
	{
		StackNode* key;
		StackNode* value;
	};
public:
	StackTableNode();

	~StackTableNode();

	void AddChild(StackNode* key, StackNode* value);

	void Write(ByteOutputStream* stream) override;

	bool deep;
private:
	std::vector<KVPair*> list;
};

class StackFunctionNode : public StackLuaObjectNode
{
public:
	StackFunctionNode();
	
	void Write(ByteOutputStream* stream) override;

	int scriptIndex;
	int line;
};

class StackBinaryNode : public StackLuaObjectNode
{
public:
	StackBinaryNode();

	void Write(ByteOutputStream* stream) override;

	char* data;
	size_t size;
};

class StackStringNode : public StackLuaObjectNode
{
public:
	StackStringNode(std::string& data);

	void Write(ByteOutputStream* stream) override;

	std::string value;
};

class StackErrorNode : public StackLuaObjectNode
{
public:
	StackErrorNode(const std::string& message)
		: StackLuaObjectNode(StackNodeId::Error)
	{
		this->message = message;
	}

	void Write(ByteOutputStream* stream) override;

	std::string message;
};

class StackUserData : public StackLuaObjectNode
{
public:
	StackUserData(std::string toString);

	void Write(ByteOutputStream* stream) override;

	std::string toString;
};

class StackPrimitiveNode : public StackLuaObjectNode
{
public:
	StackPrimitiveNode(std::string& data)
		: StackLuaObjectNode(StackNodeId::Primitive)
	{
		this->value = data;
	}

	void Write(ByteOutputStream* stream) override;

	std::string value;
};