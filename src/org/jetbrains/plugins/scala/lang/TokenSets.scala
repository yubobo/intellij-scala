package org.jetbrains.plugins.scala
package lang

/**
  * @author ilyas
  */

import com.intellij.psi.tree.TokenSet
import org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes._
import org.jetbrains.plugins.scala.lang.parser.ScalaElementTypes._
import org.jetbrains.plugins.scala.lang.scaladoc.parser.ScalaDocElementTypes._
import org.jetbrains.plugins.scala.util.MemberElementTypesExtension

object TokenSets {
  val WHITESPACE_OR_COMMENT_SET = TokenSet.create(tWHITE_SPACE_IN_LINE, tLINE_COMMENT, tBLOCK_COMMENT, tDOC_COMMENT, SCALA_DOC_COMMENT)

  val MODIFIERS = TokenSet.create(kCASE, kABSTRACT, kLAZY,
    kIMPLICIT, kFINAL, kOVERRIDE, kPROTECTED,
    kPRIVATE, kSEALED)

  val TYPE_DEFINITIONS_SET = TokenSet.create(objectDefinition, classDefinition, traitDefinition)

  private val EXPR1_SET: TokenSet = TokenSet.create(IF_STMT,
    FOR_STMT,
    WHILE_STMT,
    DO_STMT,
    TRY_STMT,
    TRY_BLOCK,
    CATCH_BLOCK,
    FINALLY_BLOCK,
    RETURN_STMT,
    THROW_STMT,
    ASSIGN_STMT,
    MATCH_STMT,
    TYPED_EXPR_STMT,
    POSTFIX_EXPR,
    INFIX_EXPR,
    PLACEHOLDER_EXPR,
    PREFIX_EXPR)

  val EXPRESSION_SET = TokenSet.orSet(EXPR1_SET,
    TokenSet.create(LITERAL,
      STRING_LITERAL,
      BOOLEAN_LITERAL,
      PREFIX_EXPR,
      PREFIX,
      POSTFIX_EXPR,
      INFIX_EXPR,
      PLACEHOLDER_EXPR,
      EXPR1,
      FUNCTION_EXPR,
      AN_FUN,
      GENERATOR,
      ENUMERATOR,
      ENUMERATORS,
      EXPRS,
      ARG_EXPRS,
      BLOCK_EXPR,
      ERROR_STMT,
      BLOCK,
      PARENT_EXPR,
      METHOD_CALL,
      REFERENCE_EXPRESSION,
      THIS_REFERENCE,
      SUPER_REFERENCE,
      GENERIC_CALL))

  val ID_SET = TokenSet.create(tIDENTIFIER, tUNDER)

  val SELF_TYPE_ID = TokenSet.create(kTHIS, tIDENTIFIER, tUNDER)

  val ALIASES_SET = TokenSet.create(TYPE_DECLARATION, TYPE_DEFINITION)

  val FUNCTIONS = TokenSet.create(FUNCTION_DECLARATION, FUNCTION_DEFINITION, MACRO_DEFINITION)

  val VALUES = TokenSet.create(VALUE_DECLARATION, PATTERN_DEFINITION)

  val VARIABLES = TokenSet.create(VARIABLE_DECLARATION, VARIABLE_DEFINITION)

  val TEMPLATE_PARENTS = TokenSet.create(CLASS_PARENTS, TRAIT_PARENTS)

  val MEMBERS = TokenSet.orSet(TokenSet.orSet(FUNCTIONS, TokenSet.orSet(
    ALIASES_SET, TokenSet.orSet(
      TYPE_DEFINITIONS_SET, TokenSet.orSet(
        VALUES, TokenSet.orSet(
          VARIABLES, TokenSet.create(PRIMARY_CONSTRUCTOR)
        )
      )
    )
  )), MemberElementTypesExtension.getAllElementTypes
  )

  val DECLARED_ELEMENTS_HOLDER = TokenSet.orSet(FUNCTIONS, TokenSet.orSet(VALUES, VARIABLES))

  val PARAMETERS = TokenSet.create(PARAM, CLASS_PARAM)

  val TYPE_ELEMENTS_TOKEN_SET = TokenSet.create(
    SIMPLE_TYPE, TYPE, TYPE_IN_PARENTHESIS, TYPE_GENERIC_CALL, INFIX_TYPE, TUPLE_TYPE,
    EXISTENTIAL_TYPE, COMPOUND_TYPE, ANNOT_TYPE, WILDCARD_TYPE, TYPE_PROJECTION, TYPE_VARIABLE
  )
}
