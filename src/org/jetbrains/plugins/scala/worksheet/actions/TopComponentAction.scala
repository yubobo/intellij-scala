package org.jetbrains.plugins.scala
package worksheet.actions

import javax.swing.{Icon, JPanel}

import com.intellij.openapi.actionSystem._
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.application.{ApplicationManager, ModalityState}
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.keymap.{KeymapManager, KeymapUtil}
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import org.jetbrains.plugins.scala.lang.psi.api.ScalaFile
import org.jetbrains.plugins.scala.worksheet.ui.WorksheetUiConstructor

/**
 * User: Dmitry Naydanov
 * Date: 2/17/14
 */
trait TopComponentAction extends TopComponentDisplayable {
  this: AnAction =>
  
  def shortcutId: Option[String] = None
  
  def genericText: String = ScalaBundle message bundleKey
  
  def bundleKey: String 
  
  def actionIcon: Icon
  
  def getActionButton: ActionButton = {
    val button = new ActionButton(this, getTemplatePresentation, ActionPlaces.EDITOR_TOOLBAR,
      ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE)
    button setToolTipText genericText
    button
  }
  
  override def init(panel: JPanel) {
    val presentation = getTemplatePresentation

    presentation setIcon actionIcon
    presentation setEnabled true

    val text = shortcutId flatMap {
      case id =>
        KeymapManager.getInstance.getActiveKeymap.getShortcuts(id).headOption map {
          case shortcut =>
            genericText + (" (" + KeymapUtil.getShortcutText(shortcut) + ")")
        }
    } getOrElse genericText

    presentation setText text

    val actionButton = getActionButton
    WorksheetUiConstructor.fixUnboundMaxSize(actionButton)

    ApplicationManager.getApplication.invokeAndWait(new Runnable {
      override def run() {
        panel.add(actionButton, 0)
        actionButton.setEnabled(true)
      }
    }, ModalityState.any())
  }

  protected def updateInner(presentation: Presentation, project: Project) {
    if (project == null) return

    def enable() {
      presentation setEnabled true
      presentation setVisible true
    }

    def disable() {
      presentation setEnabled false
      presentation setVisible false
    }

    try {
      val editor = FileEditorManager.getInstance(project).getSelectedTextEditor

      extensions.inReadAction {
        PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument) match {
          case sf: ScalaFile if sf.isWorksheetFile => enable()
          case _ => disable()
        }
      }

    } catch {
      case _: Exception => disable()
    }
  }
}
