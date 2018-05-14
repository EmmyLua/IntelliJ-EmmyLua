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
	stream->WriteString(data);
}

void StackNodeContainer::AddChild(StackNode* child)
{
	m_children.push_back(child);
}

StackNode* StackNodeContainer::GetChild(int idx) const
{
	if (m_children.size() > idx)
		return m_children[idx];
	return nullptr;
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
	stream->WriteUInt64(m_children.size());
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
		stream->WriteUInt64(list.size());
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
	stream->WriteUInt64(size);
	stream->Write(data, size);
}

StackStringNode::StackStringNode(std::string& data) : StackLuaObjectNode(StackNodeId::String)
{
	this->data = data;
}

StackErrorNode::StackErrorNode(const std::string& message) : StackLuaObjectNode(StackNodeId::Error)
{
	this->data = message;
}

StackUserData::StackUserData(std::string toString) : StackLuaObjectNode(StackNodeId::UserData)
{
	this->data = toString;
}

StackPrimitiveNode::StackPrimitiveNode(std::string& data) : StackLuaObjectNode(StackNodeId::Primitive)
{
	this->data = data;
}
