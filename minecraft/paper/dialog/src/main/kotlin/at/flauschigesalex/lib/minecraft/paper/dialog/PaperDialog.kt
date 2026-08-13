package at.flauschigesalex.lib.minecraft.paper.dialog

import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

@Suppress("unused")
abstract class PaperDialog {
    abstract val titleSupplier: (Player) -> Component
    
    typealias DialogBuilder = DialogBase.Builder
    typealias DialogConsumer = DialogBuilder.() -> Unit

    context(player: Player)
    protected open fun inputs(): List<DialogInput> = emptyList()
    
    context(player: Player)
    protected open fun body(): List<DialogBody> = emptyList()
    
    context(player: Player)
    protected abstract fun type(): DialogType

    protected open fun createDialog(player: Player) = Dialog.create { factory ->
        context(player) {
            val builder = DialogBase.builder(titleSupplier(player))
                .inputs(this.inputs())
                .body(this.body())

            factory.empty().type(this.type()).base(builder.build())
        }
    }
    
    open fun open(player: Player) {
        val dialog = this.createDialog(player)
        player.showDialog(dialog)
    }
}