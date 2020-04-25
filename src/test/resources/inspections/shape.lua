---@shape Shape1
---@field a number

---@type Shape1
local shape1

---@class NotShape1
---@field a number
local NotShape1

---@type NotShape1
local notShape1

shape1 = {a = 1}
notShape1 = <error descr="Type mismatch. Required: 'NotShape1' Found: 'table'">{a = 1}</error>

shape1 = notShape1
notShape1 = <error descr="Type mismatch. Required: 'NotShape1' Found: 'Shape1'">shape1</error>

shape1 = {a = <error descr="Type mismatch. Required: 'number' Found: '\"not a number\"'">"not a number"</error>}
shape1 = {a = 1, extraMember = "whatever"}

---@shape Shape2 : Shape1
---@field b string

---@type Shape2
local shape2

shape2 = <error descr="Type mismatch. Required: 'Shape2' Found: 'Shape1'">shape1</error>
shape1 = shape2

shape2 = <error descr="Type mismatch. Missing member: 'b' of: 'Shape2'">{a = 2}</error>
shape2 = {a = 2, b = "some string"}
shape2 = {a = 2, b = <error descr="Type mismatch. Required: 'string' Found: '2'">2</error>}

---@shape GenericShape<T>
---@field a T

---@type number
local aNumber

---@type string
local aString

---@type GenericShape<number>
local genericNumberShape

genericNumberShape = {a = aNumber}
genericNumberShape = {a = <error descr="Type mismatch. Required: 'number' Found: 'string'">aString</error>}

---@type GenericShape<string>
local genericStringShape

genericStringShape = {a = <error descr="Type mismatch. Required: 'string' Found: 'number'">aNumber</error>}
genericStringShape = {a = aString}

---@shape DerivedGenericShape<T> : GenericShape<T>
---@field b T

---@type DerivedGenericShape<number>
local derivedGenericShape = {a = 1, b = 2}

local notALiteral = {a = aString}

derivedGenericShape = <error descr="Type mismatch. Missing member: 'b' of: 'DerivedGenericShape<number>'">{a = aNumber}</error>
derivedGenericShape = {a = 1, b = <error descr="Type mismatch. Required: 'number' Found: 'string'">aString</error>}
derivedGenericShape = <error descr="Type mismatch. Required: 'DerivedGenericShape<number>' Found: 'table'">notALiteral</error>

---@param shape DerivedGenericShape<string>
local function takesAShape(shape)
end

takesAShape({a = aString, b = aString})
takesAShape({a = <error descr="Type mismatch. Required: 'string' Found: 'number'">aNumber</error>, b = <error descr="Type mismatch. Required: 'string' Found: 'number'">aNumber</error>})

---@shape NestedShape
---@field shape1 Shape1
---@field shape2 Shape2

---@type NestedShape
local nestedShape = {
    shape1 = {a = aNumber},
    shape2 = {a = aNumber, b = aString}
}

nestedShape = {
    shape1 = {a = 1},
    shape2 = (shape2)
}

nestedShape = {
    shape1 = {a = <error descr="Type mismatch. Required: 'number' Found: 'string'">aString</error>},
    shape2 = <error descr="Type mismatch. Missing member: 'b' of: 'Shape2'">{a = aNumber}</error>
}

local notShape2 = {a = 1, b = 10}

---@type NestedShape
local nestedShape3 = {
    shape1 = {a = <error descr="Type mismatch. Required: 'number' Found: 'string'">aString</error>, b = aString},
    shape2 = {a = 1, b = <error descr="Type mismatch. Required: 'string' Found: '1'">1</error>}
}

---@return NestedShape
local function returnNestedSahpe()
    return {
        shape1 = {a = <error descr="Type mismatch. Required: 'number' Found: 'table'">{}</error>},
        shape2 = {a = aNumber, b = <error descr="Type mismatch. Required: 'string' Found: 'number'">aNumber</error>}
    }
end

---@shape ShapeWithOptionalField
---@field requiredField number
---@field optional number|nil

---@type ShapeWithOptionalField
local shapeWithOptionalField = {
    requiredField = 1
}

shapeWithOptionalField = {
    requiredField = 1,
    optional = <error descr="Type mismatch. Required: 'nil|number' Found: 'string'">aString</error>
}
