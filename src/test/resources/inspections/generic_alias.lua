---@shape OurGenericShape<N>
---@field parameterOrNumber N | number
---@field aKnownStringLiteral 'a' | 'b' | 'c'

---@alias AliasAsParam 'one' | 'two' | 'three'

---@alias GenericAlias<N> string | OurGenericShape<N>

---@type GenericAlias<AliasAsParam>
local genericAlias = {
    parameterOrNumber = 'one',
    aKnownStringLiteral = 'a'
}

genericAlias = {
    parameterOrNumber = 'two',
    aKnownStringLiteral = 'b'
}

genericAlias = {
    parameterOrNumber = 1,
    aKnownStringLiteral = 'c'
}

genericAlias = {
    parameterOrNumber = <error>'invalid'</error>,
    aKnownStringLiteral = 'a'
}

genericAlias = {
    parameterOrNumber = 'three',
    aKnownStringLiteral = <error>'invalid'</error>,
}

genericAlias = <error><error descr="Type mismatch. Required: 'string' Found: 'table'">{
    parameterOrNumber = <error>'owner'</error>
}</error></error>

genericAlias = 'a string'
genericAlias = <error>1</error>
genericAlias = <error><error><error descr="Type mismatch. Required: 'string' Found: 'table'">{}</error></error></error>

---@type GenericAlias<"different">
local aDifferentGenericAlias =
<error>genericAlias</error>
