package io.github.chrislo27.rhre3.playalong

import com.badlogic.gdx.controllers.PovDirection
import com.badlogic.gdx.controllers.mappings.Xbox
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName


data class ControllerMapping(var inUse: Boolean, val name: String,
                             var buttonA: ControllerInput = ControllerInput.None,
                             var buttonB: ControllerInput = ControllerInput.None,
                             var buttonLeft: ControllerInput = ControllerInput.None,
                             var buttonRight: ControllerInput = ControllerInput.None,
                             var buttonUp: ControllerInput = ControllerInput.None,
                             var buttonDown: ControllerInput = ControllerInput.None) {

    companion object {
        val INVALID = ControllerMapping(false, "<none>")
        val XBOX = ControllerMapping(false, "XBOX something",
                                     buttonA = ControllerInput.Button(Xbox.B), buttonB = ControllerInput.Button(Xbox.A),
                                     buttonLeft = if (Xbox.DPAD_LEFT == -1) ControllerInput.Pov(Xbox.DPAD_LEFT, PovDirection.west) else ControllerInput.Button(Xbox.DPAD_LEFT),
                                     buttonRight = if (Xbox.DPAD_RIGHT == -1) ControllerInput.Pov(Xbox.DPAD_RIGHT, PovDirection.east) else ControllerInput.Button(Xbox.DPAD_RIGHT),
                                     buttonUp = if (Xbox.DPAD_UP == -1) ControllerInput.Pov(Xbox.DPAD_UP, PovDirection.north) else ControllerInput.Button(Xbox.DPAD_UP),
                                     buttonDown = if (Xbox.DPAD_DOWN == -1) ControllerInput.Pov(Xbox.DPAD_DOWN, PovDirection.south) else ControllerInput.Button(Xbox.DPAD_DOWN))
    }

}

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(ControllerInput.None::class),
        JsonSubTypes.Type(ControllerInput.Button::class),
        JsonSubTypes.Type(ControllerInput.Pov::class)
             )
sealed class ControllerInput {
    @JsonTypeName("none")
    object None : ControllerInput() {
        override fun toString(): String {
            return "ControllerInput.None"
        }
    }
    @JsonTypeName("button")
    class Button(val code: Int) : ControllerInput() {
        override fun toString(): String {
            return "ControllerInput.Button[code=$code]"
        }
    }
    @JsonTypeName("pov")
    class Pov(val povCode: Int, val direction: PovDirection) : ControllerInput() {
        override fun toString(): String {
            return "ControllerInput.Pov[povCode=$povCode, dir=$direction]"
        }
    }
//    class Axis(val axisCode: Int, val range: ClosedRange<Float>) : ControllerInput() // Not implemented
}