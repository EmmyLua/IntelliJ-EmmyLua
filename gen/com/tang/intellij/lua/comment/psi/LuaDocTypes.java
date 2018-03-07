// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.tang.intellij.lua.lang.LuaParserDefinitionKt;
import com.tang.intellij.lua.comment.psi.impl.*;

public interface LuaDocTypes {

  IElementType ACCESS_MODIFIER = LuaParserDefinitionKt.createDocType("ACCESS_MODIFIER");
  IElementType ARR_TY = LuaParserDefinitionKt.createDocType("ARR_TY");
  IElementType CLASS_DEF = LuaParserDefinitionKt.createDocType("CLASS_DEF");
  IElementType CLASS_NAME_REF = LuaParserDefinitionKt.createDocType("CLASS_NAME_REF");
  IElementType COMMENT_STRING = LuaParserDefinitionKt.createDocType("COMMENT_STRING");
  IElementType FIELD_DEF = LuaParserDefinitionKt.createDocType("FIELD_DEF");
  IElementType FUNCTION_PARAM = LuaParserDefinitionKt.createDocType("FUNCTION_PARAM");
  IElementType FUNCTION_TY = LuaParserDefinitionKt.createDocType("FUNCTION_TY");
  IElementType GENERAL_TY = LuaParserDefinitionKt.createDocType("GENERAL_TY");
  IElementType GENERIC_DEF = LuaParserDefinitionKt.createDocType("GENERIC_DEF");
  IElementType GENERIC_TY = LuaParserDefinitionKt.createDocType("GENERIC_TY");
  IElementType LAN_DEF = LuaParserDefinitionKt.createDocType("LAN_DEF");
  IElementType OVERLOAD_DEF = LuaParserDefinitionKt.createDocType("OVERLOAD_DEF");
  IElementType PARAM_DEF = LuaParserDefinitionKt.createDocType("PARAM_DEF");
  IElementType PARAM_NAME_REF = LuaParserDefinitionKt.createDocType("PARAM_NAME_REF");
  IElementType PAR_TY = LuaParserDefinitionKt.createDocType("PAR_TY");
  IElementType RETURN_DEF = LuaParserDefinitionKt.createDocType("RETURN_DEF");
  IElementType SEE_REF_TAG = LuaParserDefinitionKt.createDocType("SEE_REF_TAG");
  IElementType TABLE_DEF = LuaParserDefinitionKt.createDocType("TABLE_DEF");
  IElementType TABLE_FIELD = LuaParserDefinitionKt.createDocType("TABLE_FIELD");
  IElementType TABLE_TY = LuaParserDefinitionKt.createDocType("TABLE_TY");
  IElementType TAG_DEF = LuaParserDefinitionKt.createDocType("TAG_DEF");
  IElementType TAG_VALUE = LuaParserDefinitionKt.createDocType("TAG_VALUE");
  IElementType TY = LuaParserDefinitionKt.createDocType("TY");
  IElementType TYPE_DEF = LuaParserDefinitionKt.createDocType("TYPE_DEF");
  IElementType TYPE_LIST = LuaParserDefinitionKt.createDocType("TYPE_LIST");
  IElementType UNION_TY = LuaParserDefinitionKt.createDocType("UNION_TY");

  IElementType ARR = new LuaDocTokenType("[]");
  IElementType AT = new LuaDocTokenType("@");
  IElementType COMMA = new LuaDocTokenType(",");
  IElementType DASHES = new LuaDocTokenType("DASHES");
  IElementType EQ = new LuaDocTokenType("=");
  IElementType EXTENDS = new LuaDocTokenType(":");
  IElementType FUN = new LuaDocTokenType("fun");
  IElementType GT = new LuaDocTokenType(">");
  IElementType ID = new LuaDocTokenType("ID");
  IElementType LCURLY = new LuaDocTokenType("{");
  IElementType LPAREN = new LuaDocTokenType("(");
  IElementType LT = new LuaDocTokenType("<");
  IElementType OPTIONAL = new LuaDocTokenType("optional");
  IElementType OR = new LuaDocTokenType("|");
  IElementType PRIVATE = new LuaDocTokenType("PRIVATE");
  IElementType PROTECTED = new LuaDocTokenType("PROTECTED");
  IElementType PUBLIC = new LuaDocTokenType("PUBLIC");
  IElementType RCURLY = new LuaDocTokenType("}");
  IElementType RPAREN = new LuaDocTokenType(")");
  IElementType SHARP = new LuaDocTokenType("#");
  IElementType STRING = new LuaDocTokenType("STRING");
  IElementType STRING_BEGIN = new LuaDocTokenType("STRING_BEGIN");
  IElementType TAG_CLASS = new LuaDocTokenType("class");
  IElementType TAG_FIELD = new LuaDocTokenType("field");
  IElementType TAG_GENERIC = new LuaDocTokenType("generic");
  IElementType TAG_LANGUAGE = new LuaDocTokenType("language");
  IElementType TAG_MODULE = new LuaDocTokenType("module");
  IElementType TAG_NAME = new LuaDocTokenType("TAG_NAME");
  IElementType TAG_OVERLOAD = new LuaDocTokenType("overload");
  IElementType TAG_PARAM = new LuaDocTokenType("param");
  IElementType TAG_PRIVATE = new LuaDocTokenType("private");
  IElementType TAG_PROTECTED = new LuaDocTokenType("protected");
  IElementType TAG_PUBLIC = new LuaDocTokenType("public");
  IElementType TAG_RETURN = new LuaDocTokenType("return");
  IElementType TAG_SEE = new LuaDocTokenType("see");
  IElementType TAG_TYPE = new LuaDocTokenType("type");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == ACCESS_MODIFIER) {
        return new LuaDocAccessModifierImpl(node);
      }
      else if (type == ARR_TY) {
        return new LuaDocArrTyImpl(node);
      }
      else if (type == CLASS_DEF) {
        return new LuaDocClassDefImpl(node);
      }
      else if (type == CLASS_NAME_REF) {
        return new LuaDocClassNameRefImpl(node);
      }
      else if (type == COMMENT_STRING) {
        return new LuaDocCommentStringImpl(node);
      }
      else if (type == FIELD_DEF) {
        return new LuaDocFieldDefImpl(node);
      }
      else if (type == FUNCTION_PARAM) {
        return new LuaDocFunctionParamImpl(node);
      }
      else if (type == FUNCTION_TY) {
        return new LuaDocFunctionTyImpl(node);
      }
      else if (type == GENERAL_TY) {
        return new LuaDocGeneralTyImpl(node);
      }
      else if (type == GENERIC_DEF) {
        return new LuaDocGenericDefImpl(node);
      }
      else if (type == GENERIC_TY) {
        return new LuaDocGenericTyImpl(node);
      }
      else if (type == LAN_DEF) {
        return new LuaDocLanDefImpl(node);
      }
      else if (type == OVERLOAD_DEF) {
        return new LuaDocOverloadDefImpl(node);
      }
      else if (type == PARAM_DEF) {
        return new LuaDocParamDefImpl(node);
      }
      else if (type == PARAM_NAME_REF) {
        return new LuaDocParamNameRefImpl(node);
      }
      else if (type == PAR_TY) {
        return new LuaDocParTyImpl(node);
      }
      else if (type == RETURN_DEF) {
        return new LuaDocReturnDefImpl(node);
      }
      else if (type == SEE_REF_TAG) {
        return new LuaDocSeeRefTagImpl(node);
      }
      else if (type == TABLE_DEF) {
        return new LuaDocTableDefImpl(node);
      }
      else if (type == TABLE_FIELD) {
        return new LuaDocTableFieldImpl(node);
      }
      else if (type == TABLE_TY) {
        return new LuaDocTableTyImpl(node);
      }
      else if (type == TAG_DEF) {
        return new LuaDocTagDefImpl(node);
      }
      else if (type == TAG_VALUE) {
        return new LuaDocTagValueImpl(node);
      }
      else if (type == TYPE_DEF) {
        return new LuaDocTypeDefImpl(node);
      }
      else if (type == TYPE_LIST) {
        return new LuaDocTypeListImpl(node);
      }
      else if (type == UNION_TY) {
        return new LuaDocUnionTyImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
