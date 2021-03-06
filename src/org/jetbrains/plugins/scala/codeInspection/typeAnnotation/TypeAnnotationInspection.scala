package org.jetbrains.plugins.scala
package codeInspection.typeAnnotation

import com.intellij.application.options.CodeStyleSchemesConfigurable
import com.intellij.codeInspection.{LocalQuickFixBase, ProblemDescriptor, ProblemsHolder}
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.scala.codeInsight.intention.types.{AddOnlyStrategy, ToggleTypeAnnotation}
import org.jetbrains.plugins.scala.codeInspection.{AbstractFixOnPsiElement, AbstractInspection}
import org.jetbrains.plugins.scala.lang.formatting.settings._
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil
import org.jetbrains.plugins.scala.lang.psi.api.base.ScLiteral
import org.jetbrains.plugins.scala.lang.psi.api.expr._
import org.jetbrains.plugins.scala.lang.psi.api.statements.{ScFunctionDefinition, ScPatternDefinition, ScVariableDefinition}
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScMember
import org.jetbrains.plugins.scala.project.ProjectExt

/**
 * Pavel Fatin
 */
class TypeAnnotationInspection extends AbstractInspection {
  def actionFor(holder: ProblemsHolder): PartialFunction[PsiElement, Unit] = {
    case value: ScPatternDefinition if value.isSimple && !value.hasExplicitType =>
      val settings = ScalaCodeStyleSettings.getInstance(holder.getProject)

      inspect(value.bindings.head,
        kindOf(value) + " value",
        value.declaredElements.headOption.map(ScalaPsiUtil.superValsSignatures(_, withSelfType = true)).exists(_.nonEmpty),
        value.expr.exists(isSimple),
        requirementForProperty(value, settings),
        settings.OVERRIDING_PROPERTY_TYPE_ANNOTATION,
        settings.SIMPLE_PROPERTY_TYPE_ANNOTATION,
        holder)

    case variable: ScVariableDefinition if variable.isSimple && !variable.hasExplicitType =>
      val settings = ScalaCodeStyleSettings.getInstance(holder.getProject)

      inspect(variable.bindings.head,
        kindOf(variable) + " variable",
        variable.declaredElements.headOption.map(ScalaPsiUtil.superValsSignatures(_, withSelfType = true)).exists(_.nonEmpty),
        variable.expr.exists(isSimple),
        requirementForProperty(variable, settings),
        settings.OVERRIDING_PROPERTY_TYPE_ANNOTATION,
        settings.SIMPLE_PROPERTY_TYPE_ANNOTATION,
        holder)

    case method: ScFunctionDefinition if method.hasAssign && !method.hasExplicitType && !method.isSecondaryConstructor =>
      val settings = ScalaCodeStyleSettings.getInstance(holder.getProject)

      inspect(method.nameId,
        kindOf(method) + " method",
        method.superSignaturesIncludingSelfType.nonEmpty,
        method.body.exists(isSimple),
        requirementForMethod(method, settings),
        settings.OVERRIDING_METHOD_TYPE_ANNOTATION,
        settings.SIMPLE_METHOD_TYPE_ANNOTATION,
        holder)
  }

  private def isSimple(exp: ScExpression): Boolean = {
    exp match {
      case _: ScLiteral => true
      case _: ScNewTemplateDefinition => true
      case ref: ScReferenceExpression if ref.refName == "???" => true
      case ref: ScReferenceExpression if ref.refName(0).isUpper => true //heuristic for objects
      case call: ScMethodCall => call.getInvokedExpr match {
        case ref: ScReferenceExpression if ref.refName(0).isUpper => true //heuristic for case classes
        case _ => false
      }
      case _: ScThrowStmt => true
      case _ => false
    }
  }

  private def requirementForProperty(property: ScMember, settings: ScalaCodeStyleSettings): Int = {
    if (property.isLocal) {
      settings.LOCAL_PROPERTY_TYPE_ANNOTATION
    } else {
      if (property.isPrivate) settings.PRIVATE_PROPERTY_TYPE_ANNOTATION
      else if (property.isProtected) settings.PROTECTED_PROPERTY_TYPE_ANNOTATION
      else settings.PUBLIC_PROPERTY_TYPE_ANNOTATION
    }
  }

  private def requirementForMethod(method: ScFunctionDefinition, settings: ScalaCodeStyleSettings): Int = {
    if (method.isLocal) {
      settings.LOCAL_METHOD_TYPE_ANNOTATION
    } else {
      if (method.isPrivate) settings.PRIVATE_METHOD_TYPE_ANNOTATION
      else if (method.isProtected) settings.PROTECTED_METHOD_TYPE_ANNOTATION
      else settings.PUBLIC_METHOD_TYPE_ANNOTATION
    }
  }

  private def kindOf(member: ScMember) = if (member.isLocal) "Local" else {
    if (member.isPrivate) "Private" else if (member.isProtected) "Protected" else "Public"
  }

  private def inspect(element: PsiElement,
                      name: String,
                      isOverriding: Boolean,
                      isSimple: Boolean,
                      requirement: Int,
                      overridingPolicy: Int,
                      simplePolicy: Int,
                      holder: ProblemsHolder) {
    if (requirement == TypeAnnotationRequirement.Required.ordinal &&
            (!isSimple || simplePolicy == TypeAnnotationPolicy.Regular.ordinal) &&
            (overridingPolicy == TypeAnnotationPolicy.Regular.ordinal || !isOverriding)) {
      holder.registerProblem(element, s"$name requires an explicit type annotation (according to Code Style settings)",
        new AddTypeAnnotationQuickFix(element),
        new LearnWhyQuickFix(),
        new ModifyCodeStyleQuickFix())
    }
  }

  private class AddTypeAnnotationQuickFix(element: PsiElement) extends AbstractFixOnPsiElement("Add type annotation", element) {
    def doApplyFix(project: Project): Unit = {
      val elem = getElement
      ToggleTypeAnnotation.complete(AddOnlyStrategy.withoutEditor, elem)(project.typeSystem)
    }
  }

  private class LearnWhyQuickFix extends LocalQuickFixBase("Learn Why...") {
    def applyFix(project: Project, problemDescriptor: ProblemDescriptor) {
      DesktopUtils.browse("http://blog.jetbrains.com/scala/2016/07/11/beyond-code-style/")
    }
  }

  private class ModifyCodeStyleQuickFix extends LocalQuickFixBase("Modify Code Style...") {
    def applyFix(project: Project, problemDescriptor: ProblemDescriptor): Unit = {
      // TODO Select the Scala / Type Annotations page
      ShowSettingsUtil.getInstance().showSettingsDialog(project, classOf[CodeStyleSchemesConfigurable])
    }
  }
}
