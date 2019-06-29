package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.Tool
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class PresentationModeButton(val editor: Editor, val editorStage: EditorStage, palette: UIPalette,
                             parent: UIElement<EditorScreen>,
                             stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    init {
        addLabel(ImageLabel(palette, this, stage).apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_presentation"))
        })
    }

    override var tooltipText: String?
        set(_) {}
        get() {
            return Localization["editor.presentationMode.info"]
        }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        val stage = editorStage
        val visible = !stage.presentationModeStage.visible
        stage.elements.filterIsInstance<Stage<*>>().forEach {
            it.visible = !visible
        }
        stage.presentationModeStage.visible = visible
        stage.tapalongStage.visible = false
        stage.playalongStage.visible = false
        stage.paneLikeStages.forEach { it.visible = false }
        stage.buttonBarStage.visible = true
        stage.messageBarStage.visible = !visible
        stage.subtitleStage.visible = true
        if (visible) {
            editor.currentTool = Tool.SELECTION
            editor.remix.recomputeCachedData()
        }
        stage.updateSelected()
        editor.updateMessageLabel()
    }
}