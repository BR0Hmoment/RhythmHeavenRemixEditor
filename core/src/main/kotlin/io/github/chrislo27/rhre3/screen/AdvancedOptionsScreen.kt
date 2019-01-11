package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.TrueCheckbox
import io.github.chrislo27.rhre3.util.modding.ModdingUtils
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextLabel


class AdvancedOptionsScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, AdvancedOptionsScreen>(main) {

    override val stage: GenericStage<AdvancedOptionsScreen>
    private val preferences: Preferences
        get() = main.preferences
    private var didChangeSettings: Boolean = false

    private val moddingGameLabel: TextLabel<AdvancedOptionsScreen>

    init {
        val palette = main.uiPalette
        stage = GenericStage(main.uiPalette, null, main.defaultCamera)

        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_adv_opts"))
        stage.titleLabel.isLocalizationKey = false
        stage.titleLabel.text = "Advanced Options"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("info")
        }

        val centre = stage.centreStage
        val padding = 0.025f
        val buttonWidth = 0.4f
        val buttonHeight = 0.1f
        val fontScale = 0.75f

        // Advanced Options setting
        centre.elements += object : TrueCheckbox<AdvancedOptionsScreen>(palette, centre, centre) {
            override val checkLabelPortion: Float = 0.1f
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                preferences.putBoolean(PreferenceKeys.SETTINGS_ADVANCED_OPTIONS, checked).flush()
                didChangeSettings = true
            }
        }.apply {
            this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_ADVANCED_OPTIONS, true)

            this.checkLabel.location.set(screenWidth = checkLabelPortion)
            this.textLabel.location.set(screenX = checkLabelPortion * 1.25f, screenWidth = 1f - checkLabelPortion * 1.25f)

            this.textLabel.apply {
                this.fontScaleMultiplier = fontScale
                this.isLocalizationKey = false
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "Advanced Options"
            }

            this.location.set(screenX = padding,
                              screenY = padding * 7 + buttonHeight * 6,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
        }
        moddingGameLabel = TextLabel(palette, centre, centre).apply {
            this.isLocalizationKey = false
            this.text = ""
            this.textWrapping = false
            this.fontScaleMultiplier = 0.9f
            this.textAlign = Align.top or Align.center
            this.location.set(screenX = padding,
                              screenY = padding,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight * 4 + padding * 5)
        }
        centre.elements += moddingGameLabel
        // Modding game reference
        centre.elements += object : Button<AdvancedOptionsScreen>(palette, centre, centre) {
            private fun updateText() {
                textLabel.text = "[LIGHT_GRAY]Modding utilities with reference to:[]\n${Localization[ModdingUtils.currentGame.localization]}"
                updateModdingLabel()
            }

            private fun persist() {
                ModdingUtils.currentGame = ModdingUtils.Game.VALUES[index]
                preferences.putString(PreferenceKeys.ADVOPT_REF_RH_GAME, ModdingUtils.currentGame.id).flush()
                didChangeSettings = true
            }

            private var index: Int = run {
                val default = ModdingUtils.Game.DEFAULT_GAME
                val pref = preferences.getString(PreferenceKeys.ADVOPT_REF_RH_GAME, default.id)
                val values = ModdingUtils.Game.VALUES
                values.indexOf(values.find { it.id == pref } ?: default).coerceIn(0, values.size - 1)
            }

            private val textLabel: TextLabel<AdvancedOptionsScreen>
                get() = labels.first() as TextLabel

            override fun render(screen: AdvancedOptionsScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                if (textLabel.text.isEmpty()) {
                    updateText()
                }
                super.render(screen, batch, shapeRenderer)
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                index++
                if (index >= ModdingUtils.Game.VALUES.size)
                    index = 0

                persist()
                updateText()
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                index--
                if (index < 0)
                    index = ModdingUtils.Game.VALUES.size - 1

                persist()
                updateText()
            }

            init {
                Localization.listeners += {
                    updateText()
                }
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = ""
                this.textWrapping = false
                this.fontScaleMultiplier = fontScale
            })

            this.location.set(screenX = padding,
                              screenY = padding * 6 + buttonHeight * 4,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight * 2)
        }

        updateModdingLabel()
    }

    private fun updateModdingLabel() {
        val game = ModdingUtils.currentGame
        moddingGameLabel.text = "1 ♩ (quarter note) = ${game.beatsToTickflowString(1f)} rest units"
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        }
    }

    override fun hide() {
        super.hide()

        // Analytics
        if (didChangeSettings) {
            val map: Map<String, *> = preferences.get()
            AnalyticsHandler.track("Exit Advanced Options",
                                   mapOf(
                                           "settings" to PreferenceKeys.allAdvOptsKeys.associate {
                                               it.replace("advOpt_", "") to (map[it] ?: "null")
                                           } + ("advancedOptions" to map[PreferenceKeys.SETTINGS_ADVANCED_OPTIONS])
                                        ))
        }

        didChangeSettings = false
    }

}