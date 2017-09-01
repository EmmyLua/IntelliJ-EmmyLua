// This is a generated file. Not intended for manual editing.
package com.tang.intellij.lua.comment.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.tang.intellij.lua.lang.LuaParserDefinition;
import com.tang.intellij.lua.comment.psi.impl.*;

public interface LuaDocTypes {

  IElementType ACCESS_MODIFIER = LuaParserDefinition.createDocType("ACCESS_MODIFIER");
  IElementType ARR_TY = LuaParserDefinition.createDocType("ARR_TY");
  IElementType CLASS_DEF = LuaParserDefinition.createDocType("CLASS_DEF");
  IElementType CLASS_NAME_REF = LuaParserDefinition.createDocType("CLASS_NAME_REF");
  IElementType COMMENT_STRING = LuaParserDefinition.createDocType("COMMENT_STRING");
  IElementType FIELD_DEF = LuaParserDefinition.createDocType("FIELD_DEF");
  IElementType FUNCTION_PARAM = LuaParserDefinition.createDocType("FUNCTION_PARAM");
  IElementType FUNCTION_TY = LuaParserDefinition.createDocType("FUNCTION_TY");
  IElementType GENERAL_TY = LuaParserDefinition.createDocType("GENERAL_TY");
  IElementType GENERIC_TY = LuaParserDefinition.createDocType("GENERIC_TY");
  IElementType LAN_DEF = LuaParserDefinition.createDocType("LAN_DEF");
  IElementType PARAM_DEF = LuaParserDefinition.createDocType("PARAM_DEF");
  IElementType PARAM_NAME_REF = LuaParserDefinition.createDocType("PARAM_NAME_REF");
  IElementType RETURN_DEF = LuaParserDefinition.createDocType("RETURN_DEF");
  IElementType SIGNATURE_DEF = LuaParserDefinition.createDocType("SIGNATURE_DEF");
  IElementType TAG_DEF = LuaParserDefinition.createDocType("TAG_DEF");
  IElementType TAG_VALUE = LuaParserDefinition.createDocType("TAG_VALUE");
  IElementType TY = LuaParserDefinition.createDocType("TY");
  IElementType TYPE_DEF = LuaParserDefinition.createDocType("TYPE_DEF");
  IElementType TYPE_LIST = LuaParserDefinition.createDocType("TYPE_LIST");
  IElementType TYPE_SET = LuaParserDefinition.createDocType("TYPE_SET");

  IElementType ARR = new LuaDocTokenType("[]");
  IElementType AT = new LuaDocTokenType("@");
  IElementType CLASS = new LuaDocTokenType("class");
  IElementType COMMA = new LuaDocTokenType(",");
  IElementType DASHES = new LuaDocTokenType("DASHES");
  IElementType EQ = new LuaDocTokenType("=");
  IElementType EXTENDS = new LuaDocTokenType(":");
  IElementType FIELD = new LuaDocTokenType("field");
  IElementType FUN = new LuaDocTokenType("fun");
  IElementType GT = new LuaDocTokenType(">");
  IElementType ID = new LuaDocTokenType("ID");
  IElementType LANGUAGE = new LuaDocTokenType("language");
  IElementType LPAREN = new LuaDocTokenType("(");
  IElementType LT = new LuaDocTokenType("<");
  IElementType OPTIONAL = new LuaDocTokenType("optional");
  IElementType OR = new LuaDocTokenType("|");
  IElementType PROTECTED = new LuaDocTokenType("protected");
  IElementType PUBLIC = new LuaDocTokenType("public");
  IElementType RPAREN = new LuaDocTokenType(")");
  IElementType SHARP = new LuaDocTokenType("#");
  IElementType SIGNATURE = new LuaDocTokenType("signature");
  IElementType STRING = new LuaDocTokenType("STRING");
  IElementType STRING_BEGIN = new LuaDocTokenType("STRING_BEGIN");
  IElementType TAG_NAME = new LuaDocTokenType("TAG_NAME");
  IElementType TAG_PARAM = new LuaDocTokenType("param");
  IElementType TAG_RETURN = new LuaDocTokenType("return");
  IElementType TYPE = new LuaDocTokenType("type");

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
      else if (type == GENERIC_TY) {
        return new LuaDocGenericTyImpl(node);
      }
      else if (type == LAN_DEF) {
        return new LuaDocLanDefImpl(node);
      }
      else if (type == PARAM_DEF) {
        return new LuaDocParamDefImpl(node);
      }
      else if (type == PARAM_NAME_REF) {
        return new LuaDocParamNameRefImpl(node);
      }
      else if (type == RETURN_DEF) {
        return new LuaDocReturnDefImpl(node);
      }
      else if (type == SIGNATURE_DEF) {
        return new LuaDocSignatureDefImpl(node);
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
      else if (type == TYPE_SET) {
        return new LuaDocTypeSetImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
