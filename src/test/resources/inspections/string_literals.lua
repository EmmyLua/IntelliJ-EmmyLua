---@type string
local str

local implictlyTypedString = "notTypedAsALiteral"

---@type "stringLiteral"
local explictlyTypedLiteral

str = "some string"
str = implictlyTypedString
str = explictlyTypedLiteral

explictlyTypedLiteral = "stringLiteral"
explictlyTypedLiteral = str -- Expect error
explictlyTypedLiteral = implictlyTypedString -- Expect error

local literalTable = {
    a = "stringLiteral",
    b = "aDifferentStringLiteral"
}

str = literalTable.a
explictlyTypedLiteral = literalTable.a
explictlyTypedLiteral = literalTable.b -- Expect error
