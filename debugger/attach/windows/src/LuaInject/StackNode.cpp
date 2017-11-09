#include "StackNode.h"
#include "Stream.h"

StackNode::StackNode(StackNodeId id): id(id)
{
}

void StackNode::Write(ByteOutputStream* stream)
{
	stream->WriteByte((char)id);
}

void StackLuaObjectNode::Write(ByteOutputStream* stream)
{
	StackNode::Write(stream);

	stream->WriteString(name);
	stream->WriteString(type);
}

void StackNodeContainer::AddChild(StackNode* child)
{
	m_children.push_back(child);
}

void StackNodeContainer::Write(ByteOutputStream* stream)
{
	StackNode::Write(stream);
	WriteChildren(stream);
}

StackNodeContainer::~StackNodeContainer()
{
	for (auto node : m_children)
	{
		delete node;
	}
}

void StackNodeContainer::WriteChildren(ByteOutputStream* stream)
{
	stream->WriteSize(m_children.size());
	for (auto node : m_children)
	{
		node->Write(stream);
	}
}

EvalResultNode::EvalResultNode() : StackNodeContainer(StackNodeId::Eval), success(false)
{
}

void EvalResultNode::Write(ByteOutputStream* stream)
{
	StackNodeContainer::Write(stream);
	stream->WriteBool(success);
	if (!success)
		stream->WriteString(error);
}

StackRootNode::StackRootNode() : StackNodeContainer(StackNodeId::StackRoot), scriptIndex(0), line(0)
{
}

void StackRootNode::Write(ByteOutputStream* stream)
{
	StackNodeContainer::Write(stream);

	stream->WriteUInt32(scriptIndex);
	stream->WriteUInt32(line);
	stream->WriteString(functionName);
}

StackTableNode::StackTableNode(): StackLuaObjectNode(StackNodeId::Table), deep(false)
{
}

StackTableNode::~StackTableNode()
{
	for (auto pair : list)
	{
		delete pair->key;
		delete pair->value;
		delete pair;
	}
}

void StackTableNode::AddChild(StackNode* key, StackNode* value)
{
	KVPair* pair = new KVPair();
	pair->key = key;
	pair->value = value;
	list.push_back(pair);
}

void StackTableNode::Write(ByteOutputStream* stream)
{
	StackLuaObjectNode::Write(stream);
	stream->WriteBool(deep);
	if (deep)
	{
		stream->WriteSize(list.size());
		for (auto pair : list)
		{
			pair->key->Write(stream);
			pair->value->Write(stream);
		}
	}
}

StackFunctionNode::StackFunctionNode(): StackLuaObjectNode(StackNodeId::Function), scriptIndex(0), line(0)
{
}

void StackFunctionNode::Write(ByteOutputStream* stream)
{
	StackLuaObjectNode::Write(stream);
	stream->WriteUInt32(scriptIndex);
	stream->WriteUInt32(line);
}

StackBinaryNode::StackBinaryNode(): StackLuaObjectNode(StackNodeId::Binary), data(nullptr), size(0)
{
}

void StackBinaryNode::Write(ByteOutputStream* stream)
{
	StackLuaObjectNode::Write(stream);
	stream->WriteSize(size);
	stream->Write(data, size);
}

StackStringNode::StackStringNode(std::string& data) : StackLuaObjectNode(StackNodeId::String)
{
	value = data;
}

void StackStringNode::Write(ByteOutputStream* stream)
{
	StackLuaObjectNode::Write(stream);
	stream->WriteString(value);
}

void StackErrorNode::Write(ByteOutputStream* stream)
{
	StackLuaObjectNode::Write(stream);
	stream->WriteString(message);
}

StackUserData::StackUserData(std::string toString) : StackLuaObjectNode(StackNodeId::UserData)
{
	this->toString = toString;
}

void StackUserData::Write(ByteOutputStream* stream)
{
	StackLuaObjectNode::Write(stream);
	stream->WriteString(toString);
}

void StackPrimitiveNode::Write(ByteOutputStream* stream)
{
	StackLuaObjectNode::Write(stream);
	
	stream->WriteString(value);
}
