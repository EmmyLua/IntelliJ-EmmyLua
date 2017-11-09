#include "StackNode.h"
#include "Stream.h"

StackNode::StackNode(StackNodeId id): id(id)
{
}

void StackNode::Write(ByteOutputStream* stream)
{
	stream->WriteByte((char)id);
	stream->WriteString(key);
}

void StackNodeContainer::AddChild(StackNode* child)
{
	m_children.push_back(child);
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

StackNodeTable::StackNodeTable(): StackNodeContainer(StackNodeId::Table)
{
}

StackNodeBinary::StackNodeBinary(): StackNode(StackNodeId::Binary), data(nullptr), size(0)
{
}

void StackNodeBinary::Write(ByteOutputStream* stream)
{
	StackNode::Write(stream);
	stream->WriteSize(size);
	stream->Write(data, size);
}
